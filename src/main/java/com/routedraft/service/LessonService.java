package com.routedraft.service;

import com.routedraft.entity.Lesson;
import com.routedraft.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {
    private final LessonRepository lessonRepository;

    /**
     * 수업 지도안 전체 목록 조회
     */
    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }
}
