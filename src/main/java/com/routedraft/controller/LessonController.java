package com.routedraft.controller;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.entity.Lesson;
import com.routedraft.service.AiPromptProvider;
import com.routedraft.service.LessonPromptProvider;
import com.routedraft.service.LessonService;
import com.routedraft.service.OpenAiService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class LessonController {

    private final OpenAiService openAiService;
    private final LessonService lessonService;
    private final ApplicationContext applicationContext;

    public LessonController(OpenAiService openAiService, LessonService lessonService,
            ApplicationContext applicationContext) {
        this.openAiService = openAiService;
        this.lessonService = lessonService;
        this.applicationContext = applicationContext;
    }

    /**
     * 1. 수업 설계 생성 API (POST)
     * 주소: POST http://localhost:8080/api/v1/ai/lesson
     */
    @PostMapping("/lesson")
    public ResponseEntity<LessonResponse> generateLesson(@RequestBody LessonCreateRequest request) {
        // 1. 지도안용 프롬프트 생성
        AiPromptProvider<LessonCreateRequest> promptProvider = new LessonPromptProvider(applicationContext);

        // 2. 요청 및 LessonResponse DTO 변환
        LessonResponse response = openAiService.requestAiCompletions(
                promptProvider,
                request,
                "classpath:prompts/lesson-plan.md",
                LessonResponse.class);

        // 3. DB 저장 및 반환
        lessonService.saveLesson(request, response);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 수업 지도안 전체 목록 조회 API (GET)
     * 주소: GET http://localhost:8080/api/v1/ai/lessons
     */
    @GetMapping("/lessons")
    public ResponseEntity<List<Lesson>> getAllLessons() {
        List<Lesson> lessons = lessonService.getAllLessons();
        return ResponseEntity.ok(lessons);
    }
}
