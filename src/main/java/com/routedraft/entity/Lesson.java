package com.routedraft.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    // [1. 교사 입력 조건 데이터 컬럼]
    @Column(name = "school_level")
    private String schoolLevel;

    private String grade;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String achievementStandard;

    @Column(name = "lesson_style")
    private String lessonStyle;

    private String duration; // 분 단위 저장

    @Column(name = "student_count")
    private Integer studentCount;

    @Column(columnDefinition = "TEXT")
    private String environment;

    // [2. AI 출력 결과 평탄화 컬럼]
    @Column(name = "lesson_title")
    private String lessonTitle;

    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives; // 쉼표(,)로 결합한 문자열 저장

    @Column(name = "grouping_guide", columnDefinition = "TEXT")
    private String groupingGuide;

    @Column(columnDefinition = "TEXT")
    private String materials;

    @Column(name = "intro_content", columnDefinition = "TEXT")
    private String introContent; // 도입 세부 단계 통합 텍스트

    @Column(name = "dev_content", columnDefinition = "TEXT")
    private String devContent; // 전개 세부 단계 통합 텍스트

    @Column(name = "concl_content", columnDefinition = "TEXT")
    private String conclContent; // 정리 세부 단계 통합 텍스트

    @Column(name = "activity_sheet", columnDefinition = "TEXT")
    private String activitySheet;

    // [3. 동기유발]
    @Column(name = "motivation_keywords", columnDefinition = "TEXT")
    private String motivationKeywords; // 쉼표(,)로 결합한 추천 키워드 저장

    @Column(name = "education_channels", columnDefinition = "TEXT")
    private String educationChannels; // 직렬화된 교육청 채널 정보 저장 (구분자 ;; 활용)

    @Column(name = "news_channels", columnDefinition = "TEXT")
    private String newsChannels; // 직렬화된 뉴스 채널 정보 저장 (구분자 ;; 활용)

    @Column(name = "real_world_story", columnDefinition = "TEXT")
    private String realWorldStory; // 도입부 스토리텔링 텍스트 대본

    // [4. 상위권 심화학습]
    @Column(name = "advanced_topic")
    private String advancedTopic; // 심화 주제명

    @Column(name = "advanced_description", columnDefinition = "TEXT")
    private String advancedDescription; // 심화 미션 설명

    @Column(name = "advanced_activity", columnDefinition = "TEXT")
    private String advancedActivity; // 구체적 심화 활동 내용

    // [5. 하위권 보충과제]
    @Column(name = "remedial_difficulty")
    private String remedialDifficulty; // 미도달 학생용 타겟 난이도

    @Column(name = "remedial_assignment", columnDefinition = "TEXT")
    private String remedialAssignment; // 보충 과제 문항 내용

    @Column(name = "remedial_guide", columnDefinition = "TEXT")
    private String remedialGuide; // 교사 지도 가이드 텍스트

    // [6. 유튜브 API 실시간 연동]
    @Column(name = "youtube_channel_name")
    private String youtubeChannelName;

    @Column(name = "youtube_search_url", length = 500)
    private String youtubeSearchUrl;

    @Column(name = "youtube_video_id")
    private String youtubeVideoId;

    @Column(name = "youtube_video_title", length = 500)
    private String youtubeVideoTitle;

    @Column(name = "youtube_thumbnail_url", length = 500)
    private String youtubeThumbnailUrl;
}
