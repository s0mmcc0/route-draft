package com.routedraft.controller;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.service.OpenAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiTestController {

    private final OpenAiService openAiService;

    public AiTestController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping("/lesson")
    public LessonResponse generateLesson(@RequestBody LessonCreateRequest request) {
        return openAiService.createLessonPlan(request);
    }
}
