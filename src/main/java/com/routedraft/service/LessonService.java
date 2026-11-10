package com.routedraft.service;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.entity.Lesson;
import com.routedraft.mapper.LessonMapper;
import com.routedraft.repository.LessonRepository;
import com.routedraft.service.infrastructure.YoutubeClient;
import com.routedraft.util.PdfGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {
    private final LessonRepository lessonRepository;
    private final YoutubeClient youtubeClient;
    private final PdfGenerator pdfGenerator;

    /**
     * 수업 지도안 데이터 생성
     */
    @Transactional
    public LessonResponse saveLesson(LessonCreateRequest request, LessonResponse response) {
        // 1. 교사 입력값과 AI 응답을 엔티티로 변환
        Lesson lesson = LessonMapper.toEntity(request, response);

        // 2. 동기유발 영상: YouTube 실시간 검색 결과 매핑
        String finalVideoId = null;
        String finalVideoTitle = null;
        String finalThumbnailUrl = null;
        String finalSearchUrl = null;

        LessonResponse.MotivationAssets assets = response.motivationAssets();
        if (assets != null
                && assets.educationChannelSources() != null && !assets.educationChannelSources().isEmpty()
                && assets.recommendedKeywords() != null && !assets.recommendedKeywords().isEmpty()) {

            String targetChannel = assets.educationChannelSources().get(0).channelName();
            String targetKeyword = assets.recommendedKeywords().get(0);

            var youtubeResult = youtubeClient.searchVideo(targetChannel, targetKeyword);

            if (youtubeResult != null) {
                finalVideoId = youtubeResult.id().videoId();
                finalVideoTitle = youtubeResult.snippet().title();
                finalThumbnailUrl = youtubeResult.snippet().thumbnails().highResolution().url();
                finalSearchUrl = "https://www.youtube.com/watch?v=" + finalVideoId;
            } else {
                finalSearchUrl = assets.educationChannelSources().get(0).url();
            }
        }

        lesson.setYoutubeVideoId(finalVideoId);
        lesson.setYoutubeVideoTitle(finalVideoTitle);
        lesson.setYoutubeThumbnailUrl(finalThumbnailUrl);
        lesson.setYoutubeSearchUrl(finalSearchUrl);

        // 3. 저장
        lessonRepository.save(lesson);

        // 4. 실시간 유튜브 데이터가 주입된 최종 반환용 응답 DTO 조립 및 리턴
        List<LessonResponse.ChannelLinkAsset> updatedEducationSources = new ArrayList<>();
        if (assets != null && assets.educationChannelSources() != null && !assets.educationChannelSources().isEmpty()) {
            var firstChannel = assets.educationChannelSources().get(0);

            updatedEducationSources.add(new LessonResponse.ChannelLinkAsset(
                firstChannel.channelName(),
                finalVideoTitle != null ? finalVideoTitle : firstChannel.videoTitle(),
                finalSearchUrl,
                finalVideoId,
                finalThumbnailUrl
            ));

            for (int i = 1; i < assets.educationChannelSources().size(); i++) {
                updatedEducationSources.add(assets.educationChannelSources().get(i));
            }
        }

        return new LessonResponse(
            lesson.getLessonTitle(),
            response.learningObjectives(),
            response.environmentSetup(),
            response.lessonFlow(),
            response.studentActivitySheet(),
            new LessonResponse.MotivationAssets(
                assets != null ? assets.recommendedKeywords() : null,
                updatedEducationSources,
                assets != null ? assets.newsChannelSources() : null,
                assets != null ? assets.realWorldStory() : null
            ),
            response.advancedLearning(),
            response.remedialAssignment()
        );
    }

    /**
     * 수업 지도안 전체 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    /**
     * 수업 지도안 개별(상세) 단건 조회
     */
    @Transactional(readOnly = true)
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID의 수업 지도안을 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 수업 지도안 표준 PDF 생성
     */
    public byte[] generateLessonPdf(Long id) {
        // 1. 엔티티 데이터 조회 책임만 수행
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 수업 지도안이 존재하지 않습니다. ID: " + id));

        // 2. PDF 생성 전담 레이어로 데이터 위임
        return pdfGenerator.generateLessonPlan(lesson);
    }
}