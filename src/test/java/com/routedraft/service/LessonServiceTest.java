package com.routedraft.service;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.dto.LessonResponse.*;
import com.routedraft.dto.external.YoutubeSearchResponse.*;
import com.routedraft.entity.Lesson;
import com.routedraft.repository.LessonRepository;
import com.routedraft.service.infrastructure.YoutubeClient;
import com.routedraft.util.PdfGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * given: 가짜 YouTube가 어떤 값을 돌려줄지 정하기
 * when: saveLesson() 실행
 * then: DB에 저장하려던 Lesson을 가로채서(ArgumentCaptor) 값이 맞는지 확인
 */

/**
 * 리팩토링(#12) 전 LessonService.saveLesson()의 현재 동작을 고정하는 테스트.
 * 리팩토링 후에도 이 테스트가 그대로 통과하면 동작이 바뀌지 않았다는 근거가 된다.
 */
@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    LessonRepository lessonRepository;

    @Mock
    YoutubeClient youtubeClient;

    @Mock
    PdfGenerator pdfGenerator;

    @InjectMocks
    LessonService lessonService;

    @Test
    @DisplayName("교사 입력값과 AI 응답이 Lesson 엔티티에 매핑되어 저장된다")
    void saveLesson_mapsRequestAndAiResponseToEntity() {
        // given
        when(youtubeClient.searchVideo(anyString(), anyString())).thenReturn(null);

        // when
        lessonService.saveLesson(request(), aiResponse());

        // then
        Lesson saved = captureSavedLesson();

        // 1. 교사 입력값
        assertThat(saved.getSchoolLevel()).isEqualTo("중학교");
        assertThat(saved.getGrade()).isEqualTo("2학년");
        assertThat(saved.getSubject()).isEqualTo("정보 - 제어 구조");
        assertThat(saved.getDuration()).isEqualTo("45");
        assertThat(saved.getStudentCount()).isEqualTo(24);
        assertThat(saved.getIsPublic()).isFalse();
        assertThat(saved.getUser()).isNull();

        // 2. AI 응답 평탄화
        assertThat(saved.getLessonTitle()).isEqualTo("반복 구조로 그림 그리기");
        assertThat(saved.getLearningObjectives()).isEqualTo("목표 1, 목표 2");
        assertThat(saved.getGroupingGuide()).isEqualTo("4인 1모둠");
        assertThat(saved.getMaterials()).isEqualTo("PC, 활동지");
        assertThat(saved.getIntroContent())
                .isEqualTo("[동기유발 (5분)]\n- 교사: 영상 제시\n- 학생: 영상 시청\n- 유의점: 집중 유도");
        assertThat(saved.getMotivationKeywords()).isEqualTo("반복 구조 코딩 교육, 반복문");
        assertThat(saved.getEducationChannels())
                .isEqualTo("EBS 소프트웨어|AI 추천 영상|https://www.youtube.com/@EBSsw/search?query=반복");
        assertThat(saved.getAdvancedTopic()).isEqualTo("중첩 반복");
        assertThat(saved.getRemedialAssignment()).isEqualTo("반복 횟수 바꾸기");
        assertThat(saved.getActivitySheet()).isEqualTo("활동지 내용");
    }

    @Test
    @DisplayName("유튜브 검색에 성공하면 실제 영상 정보가 저장되고 첫 번째 교육 채널에 반영된다")
    void saveLesson_youtubeFound_overridesFirstChannel() {
        // given
        when(youtubeClient.searchVideo("EBS 소프트웨어", "반복 구조 코딩 교육"))
                .thenReturn(youtubeItem("abc123", "실제 영상 제목", "https://img/abc123.jpg"));

        // when
        LessonResponse result = lessonService.saveLesson(request(), aiResponse());

        // then: 엔티티
        Lesson saved = captureSavedLesson();
        assertThat(saved.getYoutubeVideoId()).isEqualTo("abc123");
        assertThat(saved.getYoutubeVideoTitle()).isEqualTo("실제 영상 제목");
        assertThat(saved.getYoutubeThumbnailUrl()).isEqualTo("https://img/abc123.jpg");
        assertThat(saved.getYoutubeSearchUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123");

        // then: 반환 응답
        ChannelLinkAsset first = result.motivationAssets().educationChannelSources().get(0);
        assertThat(first.channelName()).isEqualTo("EBS 소프트웨어");
        assertThat(first.videoTitle()).isEqualTo("실제 영상 제목");
        assertThat(first.url()).isEqualTo("https://www.youtube.com/watch?v=abc123");
        assertThat(first.videoId()).isEqualTo("abc123");
    }

    @Test
    @DisplayName("유튜브 검색 결과가 없으면 AI가 제안한 채널 URL을 그대로 사용한다")
    void saveLesson_youtubeNotFound_fallsBackToAiUrl() {
        // given
        when(youtubeClient.searchVideo(anyString(), anyString())).thenReturn(null);

        // when
        LessonResponse result = lessonService.saveLesson(request(), aiResponse());

        // then
        Lesson saved = captureSavedLesson();
        assertThat(saved.getYoutubeVideoId()).isNull();
        assertThat(saved.getYoutubeSearchUrl())
                .isEqualTo("https://www.youtube.com/@EBSsw/search?query=반복");

        ChannelLinkAsset first = result.motivationAssets().educationChannelSources().get(0);
        assertThat(first.videoTitle()).isEqualTo("AI 추천 영상");
    }

    @Test
    @DisplayName("교육 채널 정보가 없으면 유튜브 API를 호출하지 않는다")
    void saveLesson_noChannels_skipsYoutube() {
        // given
        LessonResponse noChannels = withMotivation(
                new MotivationAssets(List.of("반복 구조 코딩 교육"), List.of(), List.of(), "이야기"));

        // when
        lessonService.saveLesson(request(), noChannels);

        // then
        verifyNoInteractions(youtubeClient);
        Lesson saved = captureSavedLesson();
        assertThat(saved.getYoutubeSearchUrl()).isNull();
    }

    // ===== 테스트 데이터 =====

    private Lesson captureSavedLesson() {
        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        return captor.getValue();
    }

    private LessonCreateRequest request() {
        return new LessonCreateRequest(
                "중학교", "2학년", "정보 - 제어 구조", "성취기준 예시",
                "활동 중심", "45", 24, "1인 1PC");
    }

    private LessonResponse aiResponse() {
        return withMotivation(new MotivationAssets(
                List.of("반복 구조 코딩 교육", "반복문"),
                List.of(new ChannelLinkAsset("EBS 소프트웨어", "AI 추천 영상",
                        "https://www.youtube.com/@EBSsw/search?query=반복", null, null)),
                List.of(),
                "실생활 이야기"));
    }

    private LessonResponse withMotivation(MotivationAssets assets) {
        FlowStep step = new FlowStep("동기유발", "5분", "영상 제시", "영상 시청", "집중 유도");
        return new LessonResponse(
                "반복 구조로 그림 그리기",
                List.of("목표 1", "목표 2"),
                new EnvironmentSetup("4인 1모둠", "PC, 활동지"),
                new LessonFlow(List.of(step), List.of(step), List.of(step)),
                "활동지 내용",
                assets,
                new AdvancedLearning("중첩 반복", "설명", "활동"),
                new RemedialAssignment("하", "반복 횟수 바꾸기", "가이드"));
    }

    private SearchResultItem youtubeItem(String videoId, String title, String thumbnailUrl) {
        return new SearchResultItem(
                new IdInfo(videoId),
                new SnippetInfo(title, new ThumbnailsInfo(new ThumbnailDetail(thumbnailUrl))));
    }
}