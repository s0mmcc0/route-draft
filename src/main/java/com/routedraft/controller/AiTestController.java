package com.routedraft.controller;

import com.routedraft.service.OpenAiService;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * AI 통신 테스트 API
     * 요청 경로: http://localhost:8080/api/v1/ai/test?message=질문내용
     */
    @GetMapping("/test")
    public String testAiCall(@RequestParam(value = "message", defaultValue = "안녕하세요, 반갑습니다.") String message) {
        return openAiService.callAi(message);
    }
}
