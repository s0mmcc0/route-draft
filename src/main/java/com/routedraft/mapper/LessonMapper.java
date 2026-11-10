package com.routedraft.mapper;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.entity.Lesson;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 교사 입력값(LessonCreateRequest)과 AI 응답(LessonResponse)을 Lesson 엔티티로 변환한다.
 * 외부 의존성이 없는 순수 변환 로직만 담당한다. (#12)
 */
public final class LessonMapper {

    private LessonMapper() {
        // 인스턴스 생성 방지 (정적 메서드만 사용)
    }

    /**
     * 교사 입력값과 AI 응답을 저장용 엔티티로 변환한다.
     * YouTube 실시간 검색 결과는 포함하지 않는다.
     */
    public static Lesson toEntity(LessonCreateRequest request, LessonResponse aiResponse) {
        Lesson lesson = new Lesson();
        lesson.setUser(null);
        lesson.setIsPublic(false);

        applyRequest(lesson, request);
        applyAiResponse(lesson, aiResponse);

        return lesson;
    }

    // 1. 교사 입력값 바인딩
    private static void applyRequest(Lesson lesson, LessonCreateRequest request) {
        lesson.setSchoolLevel(request.schoolLevel());
        lesson.setGrade(request.grade());
        lesson.setSubject(request.subject());
        lesson.setAchievementStandard(request.achievementStandard());
        lesson.setLessonStyle(request.lessonStyle());
        lesson.setDuration(String.valueOf(request.duration()));
        lesson.setStudentCount(request.studentCount());
        lesson.setEnvironment(request.environment());
    }

    // 2. AI 응답 평탄화 매핑
    private static void applyAiResponse(Lesson lesson, LessonResponse response) {
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

        LessonResponse.MotivationAssets assets = response.motivationAssets();
        if (assets != null) {
            if (assets.recommendedKeywords() != null) {
                lesson.setMotivationKeywords(String.join(", ", assets.recommendedKeywords()));
            }
            lesson.setEducationChannels(convertChannelsToText(assets.educationChannelSources()));
            lesson.setNewsChannels(convertChannelsToText(assets.newsChannelSources()));
            lesson.setRealWorldStory(assets.realWorldStory());
        }

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
    }

    private static String convertFlowStepsToText(List<LessonResponse.FlowStep> steps) {
        if (steps == null || steps.isEmpty()) return "";
        return steps.stream()
                .map(step -> String.format("[%s (%s)]\n- 교사: %s\n- 학생: %s\n- 유의점: %s",
                        step.stepName(), step.duration(), step.teacherActivity(), step.studentActivity(), step.notes()))
                .collect(Collectors.joining("\n\n"));
    }

    private static String convertChannelsToText(List<LessonResponse.ChannelLinkAsset> channels) {
        if (channels == null || channels.isEmpty()) return "";
        return channels.stream()
                .map(ch -> String.format("%s|%s|%s", ch.channelName(), ch.videoTitle(), ch.url()))
                .collect(Collectors.joining(";;"));
    }
}