package com.routedraft.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.entity.Lesson;
import com.routedraft.repository.LessonRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class OpenAiService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LessonRepository lessonRepository;
    
    // 환경변수에 등록된 API 키
    @Value("${SPRING_AI_OPENAI_API_KEY}")
    private String apiKey;

    // 스프링이 RestTemplate을 관리하도록 주입
    public OpenAiService(LessonRepository lessonRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.lessonRepository = lessonRepository;
    }

    @Transactional
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
                request.studentCount(), request.environment()
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
            LessonResponse lessonResponse = objectMapper.readValue(jsonResult, LessonResponse.class);
            // [데이터 평탄화 및 영속성 컨텍스트 적재 실행]
            saveLessonEntity(request, lessonResponse);

            return lessonResponse;
            
        } catch (Exception e) {
            throw new RuntimeException("수업 설계 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 계층형 Response DTO 데이터를 2차원 엔티티 모델로 평탄화하여 DB에 영속화하는 보조 메서드
     */
    private void saveLessonEntity(LessonCreateRequest request, LessonResponse response) {
        Lesson lesson = new Lesson();
        
        // 아직 로그인 시스템 연동 전이므로 연관 User는 임시 null 처리
        lesson.setUser(null); 
        lesson.setIsPublic(false);

        // 1. 요청 폼(Request) 입력 데이터 바인딩
        lesson.setSchoolLevel(request.schoolLevel());
        lesson.setGrade(request.grade());
        lesson.setSubject(request.subject());
        lesson.setAchievementStandard(request.achievementStandard());
        lesson.setLessonStyle(request.lessonStyle());
        lesson.setDuration(String.valueOf(request.duration()));
        lesson.setStudentCount(request.studentCount());
        lesson.setEnvironment(request.environment());

        // 2. OpenAI 출력 결과 데이터 평탄화 매핑
        lesson.setLessonTitle(response.lessonTitle());
        
        // List<String>을 쉼표 단위 문자열로 가공하여 단일 텍스트 필드에 적재
        if (response.learningObjectives() != null) {
            lesson.setLearningObjectives(String.join(", ", response.learningObjectives()));
        }
        
        if (response.environmentSetup() != null) {
            lesson.setGroupingGuide(response.environmentSetup().grouping());
            lesson.setMaterials(response.environmentSetup().materialsNeeded());
        }
        
        // 중첩 트리 구조에서 개별 단계를 꺼내어 분리된 테이블 컬럼에 배치
        if (response.lessonFlow() != null) {
            if (response.lessonFlow().introduction() != null) {
                lesson.setIntroDuration(response.lessonFlow().introduction().duration());
                lesson.setIntroContent(response.lessonFlow().introduction().content());
            }
            if (response.lessonFlow().development() != null) {
                lesson.setDevDuration(response.lessonFlow().development().duration());
                lesson.setDevContent(response.lessonFlow().development().content());
            }
            if (response.lessonFlow().conclusion() != null) {
                lesson.setConclDuration(response.lessonFlow().conclusion().duration());
                lesson.setConclContent(response.lessonFlow().conclusion().content());
            }
        }
        
        lesson.setActivitySheet(response.studentActivitySheet());

        // 영속성 리포지토리를 통해 영속성 컨텍스트에 엔티티 객체 세이브 요청
        lessonRepository.save(lesson);
    }
}