package com.routedraft.service;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.entity.Lesson;
import com.routedraft.repository.LessonRepository;
import com.routedraft.service.infrastructure.YoutubeClient;
import com.routedraft.util.PdfGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        Lesson lesson = new Lesson();
        
        lesson.setUser(null); 
        lesson.setIsPublic(false);

        // 1. 요청 폼(Request) 입력 데이터 바인딩
        lesson.setSchoolLevel(request.schoolLevel());
        lesson.setGrade(request.grade());
        lesson.setSubject(request.subject());
        lesson.setAchievementStandard(request.achievementStandard());
        lesson.setLessonStyle(request.lessonStyle());
        lesson.setDuration(String.valueOf(request.duration()));
        lesson.setStudentCount(request.studentCount());
        lesson.setEnvironment(request.environment());

        // 2. OpenAI 출력 결과 데이터 평탄화 매핑
        lesson.setLessonTitle(response.lessonTitle());
        
        if (response.learningObjectives() != null) {
            lesson.setLearningObjectives(String.join(", ", response.learningObjectives()));
        }
        
        if (response.environmentSetup() != null) {
            lesson.setGroupingGuide(response.environmentSetup().grouping());
            lesson.setMaterials(response.environmentSetup().materialsNeeded());
        }
        
        if (response.lessonFlow() != null) {
            lesson.setIntroContent(convertFlowStepsToText(response.lessonFlow().introduction()));
            lesson.setDevContent(convertFlowStepsToText(response.lessonFlow().development()));
            lesson.setConclContent(convertFlowStepsToText(response.lessonFlow().conclusion()));
        }

        // 3. 동기유발 자료 및 유튜브 실시간 데이터 매핑
        String finalVideoId = null;
        String finalVideoTitle = null;
        String finalThumbnailUrl = null;
        String finalSearchUrl = null;

        LessonResponse.MotivationAssets assets = response.motivationAssets();
        if (assets != null) {
            if (assets.recommendedKeywords() != null) {
                lesson.setMotivationKeywords(String.join(", ", assets.recommendedKeywords()));
            }
            
            lesson.setEducationChannels(convertChannelsToText(assets.educationChannelSources()));
            lesson.setNewsChannels(convertChannelsToText(assets.newsChannelSources()));
            lesson.setRealWorldStory(assets.realWorldStory());

            if (assets.educationChannelSources() != null && !assets.educationChannelSources().isEmpty() 
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
        }

        lesson.setYoutubeVideoId(finalVideoId);
        lesson.setYoutubeVideoTitle(finalVideoTitle);
        lesson.setYoutubeThumbnailUrl(finalThumbnailUrl);
        lesson.setYoutubeSearchUrl(finalSearchUrl);

        if (response.advancedLearning() != null) {
            lesson.setAdvancedTopic(response.advancedLearning().topic());
            lesson.setAdvancedDescription(response.advancedLearning().description());
            lesson.setAdvancedActivity(response.advancedLearning().activity());
        }

        if (response.remedialAssignment() != null) {
            lesson.setRemedialDifficulty(response.remedialAssignment().targetDifficulty());
            lesson.setRemedialAssignment(response.remedialAssignment().assignmentContent());
            lesson.setRemedialGuide(response.remedialAssignment().guideForTeacher());
        }
        
        lesson.setActivitySheet(response.studentActivitySheet());

        lessonRepository.save(lesson);

        // 5. 실시간 유튜브 데이터가 주입된 최종 반환용 응답 DTO 조립 및 리턴
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

    private String convertFlowStepsToText(List<LessonResponse.FlowStep> steps) {
        if (steps == null || steps.isEmpty()) return "";
        return steps.stream()
                .map(step -> String.format("[%s (%s)]\n- 교사: %s\n- 학생: %s\n- 유의점: %s",
                        step.stepName(), step.duration(), step.teacherActivity(), step.studentActivity(), step.notes()))
                .collect(Collectors.joining("\n\n"));
    }

    private String convertChannelsToText(List<LessonResponse.ChannelLinkAsset> channels) {
        if (channels == null || channels.isEmpty()) return "";
        return channels.stream()
                .map(ch -> String.format("%s|%s|%s", ch.channelName(), ch.videoTitle(), ch.url()))
                .collect(Collectors.joining(";;"));
    }
}
