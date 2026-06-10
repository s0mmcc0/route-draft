package com.routedraft.controller;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.entity.Lesson;
import com.routedraft.service.LessonService;
import com.routedraft.service.OpenAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class LessonController {

    private final OpenAiService openAiService;
    private final LessonService lessonService;

    public LessonController(OpenAiService openAiService, LessonService lessonService) {
        this.openAiService = openAiService;
        this.lessonService = lessonService;
    }

    /**
     * 1. 수업 설계 생성 API (POST)
     * 주소: POST http://localhost:8080/api/v1/ai/lesson
     */
    @PostMapping("/lesson")
    public ResponseEntity<LessonResponse> generateLesson(@RequestBody LessonCreateRequest request) {
        LessonResponse response = openAiService.createLessonPlan(request);
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
