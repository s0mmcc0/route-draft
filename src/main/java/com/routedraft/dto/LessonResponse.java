package com.routedraft.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LessonResponse(
    @JsonProperty("lesson_title") String lessonTitle,
    @JsonProperty("learning_objectives") List<String> learningObjectives,
    @JsonProperty("environment_setup") EnvironmentSetup environmentSetup,
    @JsonProperty("lesson_flow") LessonFlow lessonFlow,
    @JsonProperty("student_activity_sheet") String studentActivitySheet,
    @JsonProperty("motivation_assets") MotivationAssets motivationAssets,
    @JsonProperty("advanced_learning") AdvancedLearning advancedLearning,
    @JsonProperty("remedial_assignment") RemedialAssignment remedialAssignment
) {
    public record EnvironmentSetup(
        String grouping,
        @JsonProperty("materials_needed") String materialsNeeded
    ) {}

    public record LessonFlow(
        List<FlowStep> introduction,
        List<FlowStep> development,
        List<FlowStep> conclusion
    ) {}

    public record FlowStep(
        @JsonProperty("step_name") String stepName,
        String duration,
        @JsonProperty("teacher_activity") String teacherActivity,
        @JsonProperty("student_activity") String studentActivity,
        String notes
    ) {}

    public record MotivationAssets(
        @JsonProperty("recommended_keywords") List<String> recommendedKeywords,
        @JsonProperty("education_channel_sources") List<ChannelLinkAsset> educationChannelSources,
        @JsonProperty("news_channel_sources") List<ChannelLinkAsset> newsChannelSources,
        @JsonProperty("real_world_story") String realWorldStory
    ) {}

    public record ChannelLinkAsset(
        @JsonProperty("channel_name") String channelName,
        @JsonProperty("video_title") String videoTitle,
        String url,
        @JsonProperty("video_id") String videoId,
        @JsonProperty("thumbnail_url") String thumbnailUrl
    ) {}

    public record AdvancedLearning(
        String topic,
        String description,
        String activity
    ) {}

    public record RemedialAssignment(
        @JsonProperty("target_difficulty") String targetDifficulty,
        @JsonProperty("assignment_content") String assignmentContent,
        @JsonProperty("guide_for_teacher") String guideForTeacher
    ) {}
}
