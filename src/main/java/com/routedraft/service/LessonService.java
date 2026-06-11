package com.routedraft.service;

import com.routedraft.dto.LessonCreateRequest;
import com.routedraft.dto.LessonResponse;
import com.routedraft.entity.Lesson;
import com.routedraft.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {
    private final LessonRepository lessonRepository;

    @Transactional
    public void saveLesson(LessonCreateRequest request, LessonResponse response) {
        Lesson lesson = new Lesson();
        
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
        
        if (response.learningObjectives() != null) {
            lesson.setLearningObjectives(String.join(", ", response.learningObjectives()));
        }
        
        if (response.environmentSetup() != null) {
            lesson.setGroupingGuide(response.environmentSetup().grouping());
            lesson.setMaterials(response.environmentSetup().materialsNeeded());
        }
        
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

        lessonRepository.save(lesson);
    }

    /**
     * 수업 지도안 전체 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }
}
