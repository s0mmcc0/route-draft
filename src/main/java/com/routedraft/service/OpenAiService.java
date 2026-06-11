package com.routedraft.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class OpenAiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("classpath:prompts/lesson-plan.md")
    private Resource lessonPlanPromptResource;

    // 스프링이 RestTemplate을 관리하도록 주입
    public OpenAiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public <REQ, RES> RES requestAiCompletions(
            AiPromptProvider<REQ> promptProvider,
            REQ request,
            String promptPath,
            Class<RES> responseType) {
                System.out.println("====== [디버깅] 현재 로드된 API Key: " + apiKey);
        String url = "https://api.openai.com/v1/chat/completions";

        try {
            // 1. 프롬프트 파일 내용 읽기
            String systemPrompt;
            try (InputStream inputStream = promptProvider.getPromptResource(promptPath).getInputStream()) {
                systemPrompt = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }

            // 2. 유저 프롬프트 가져오기
            String userPrompt = promptProvider.getUserPrompt(request);

            // 3. OpenAI 규격
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("response_format", Map.of("type", "json_object"));

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            requestBody.put("messages", messages);

            // 4. 통신 및 파싱
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String jsonResult = root.path("choices").get(0).path("message").path("content").asText();

            return objectMapper.readValue(jsonResult, responseType);

        } catch (Exception e) {
            throw new RuntimeException("AI 통신 중 오류 발생: " + e.getMessage(), e);
        }
    }
}