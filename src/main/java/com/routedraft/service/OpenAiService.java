package com.routedraft.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class OpenAiService {
    private final RestTemplate restTemplate;
    
    // 환경변수에 등록된 API 키
    @Value("${SPRING_AI_OPENAI_API_KEY}")
    private String apiKey;

    // 스프링이 RestTemplate을 관리하도록 주입
    public OpenAiService() {
        this.restTemplate = new RestTemplate();
    }

    public String callAi(String message) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "AI 통신 중 오류 발생: " + e.getMessage();
        }
    }
}
