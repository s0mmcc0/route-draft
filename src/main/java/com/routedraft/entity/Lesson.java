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

    @Column(name = "intro_duration")
    private String introDuration;

    @Column(name = "intro_content", columnDefinition = "TEXT")
    private String introContent;

    @Column(name = "dev_duration")
    private String devDuration;

    @Column(name = "dev_content", columnDefinition = "TEXT")
    private String devContent;

    @Column(name = "concl_duration")
    private String conclDuration;

    @Column(name = "concl_content", columnDefinition = "TEXT")
    private String conclContent;

    @Column(name = "activity_sheet", columnDefinition = "TEXT")
    private String activitySheet;
}
