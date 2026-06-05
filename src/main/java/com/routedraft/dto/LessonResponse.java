package com.routedraft.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LessonResponse(
    @JsonProperty("lesson_title") String lessonTitle,
    @JsonProperty("learning_objectives") List<String> learningObjectives,
    @JsonProperty("environment_setup") EnvironmentSetup environmentSetup,
    @JsonProperty("lesson_flow") LessonFlow lessonFlow,
    @JsonProperty("student_activity_sheet") String studentActivitySheet
) {
    public record EnvironmentSetup(
        String grouping,
        @JsonProperty("materials_needed") String materialsNeeded
    ) {}

    public record LessonFlow(
        StageDetail introduction,
        StageDetail development,
        StageDetail conclusion
    ) {}

    public record StageDetail(
        String duration,
        String content
    ) {}
}
