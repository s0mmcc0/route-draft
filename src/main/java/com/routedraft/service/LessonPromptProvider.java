package com.routedraft.service;

import com.routedraft.dto.LessonCreateRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public class LessonPromptProvider implements AiPromptProvider<LessonCreateRequest> {
    private final ApplicationContext context;

    public LessonPromptProvider(ApplicationContext context) {
        this.context = context;
    }

    // 프롬프트 경로 지정
    @Override
    public Resource getPromptResource(String promptPath) {
        return context.getResource(promptPath);
    }

    // 지도안 요구사항에 맞는 유저 프롬프트 조립
    @Override
    public String getUserPrompt(LessonCreateRequest request) {
        return String.format(
                "학교급: %s, 학년: %s, 과목/주제: %s, 성취기준: %s, 수업스타일: %s, 수업시간: %s, 학생수: %d명, 기자재환경: %s",
                request.schoolLevel(), request.grade(), request.subject(),
                request.achievementStandard(), request.lessonStyle(), request.duration(),
                request.studentCount(), request.environment());
    }
}
