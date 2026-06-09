package com.routedraft.repository;

import com.routedraft.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JpaRepository<Entity타입, PK타입>을 상속받음.
 * 이 자체로 영속성 레이어의 Component 스캔 대상이 되며, 데이터베이스 조작 메서드를 획득함.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
}
