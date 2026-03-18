package com.studyhub.repository;

import com.studyhub.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourseIdOrderByOrderAsc(Long courseId);
}
