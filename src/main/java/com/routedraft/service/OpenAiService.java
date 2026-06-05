package com.routedraft.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class OpenAiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // 환경변수에 등록된 API 키
    @Value("${SPRING_AI_OPENAI_API_KEY}")
    private String apiKey;

    // 스프링이 RestTemplate을 관리하도록 주입
    public OpenAiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public LessonResponse createLessonPlan(LessonCreateRequest request) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 프롬프트 조립
        String systemPrompt = """
                # ROLE
                School Teacher
                
                # TASK
                Generate a structured lesson plan based on the provided conditions.
                
                # OUTPUT FORMAT
                Return ONLY a JSON object matching this schema without any conversational text or markdown code blocks:
                {
                    "lesson_title": "string",
                    "learning_objectives": ["string"],
                    "environment_setup": {
                    "grouping": "string",
                    "materials_needed": "string"
                    },
                    "lesson_flow": {
                    "introduction": { "duration": "string", "content": "string" },
                    "development": { "duration": "string", "content": "string" },
                    "conclusion": { "duration": "string", "content": "string" }
                    },
                    "student_activity_sheet": "string"
                }
                """;

        String userPrompt = String.format(
                "학교급: %s, 학년: %s, 과목/주제: %s, 성취기준: %s, 수업스타일: %s, 수업시간: %s, 학생수: %d명, 기자재환경: %s",
                request.schoolLevel(), request.grade(), request.subject(), 
                request.achievementStandard(), request.lessonStyle(), request.duration(), 
                request.classSize(), request.environment()
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        
        // JSON 응답 설정 (Response Format)
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");
        requestBody.put("response_format", responseFormat);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // OpenAI 응답에서 JSON 스트링 추출
            JsonNode root = objectMapper.readTree(response.getBody());
            String jsonResult = root.path("choices").get(0).path("message").path("content").asText();
            
            // 추출한 JSON 스트링을 LessonResponse DTO 객체로 최종 변환
            return objectMapper.readValue(jsonResult, LessonResponse.class);
            
        } catch (Exception e) {
            throw new RuntimeException("수업 설계 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
