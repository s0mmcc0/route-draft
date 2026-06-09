package com.routedraft.dto;

public record LessonCreateRequest (
    String schoolLevel,          // 학교급 (초등/중등/고등)
    String grade,                // 학년
    String subject,              // 과목명 및 단원명 (주제 키워드)
    String achievementStandard,  // 성취기준
    String lessonStyle,          // 수업 스타일 (활동/토론/강의/게임 기반 등)
    int duration,                // 수업 시간 (분 단위 숫자, 예: 40)
    int studentCount,            // 학생 수 (학생 수 숫자, 예: 24)
    String environment           // 기자재 구비 환경 (1인 1태블릿/스마트기기 없음 등)
) {}
