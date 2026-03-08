package com.studyhub.repository;

import com.studyhub.model.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"category", "manager"})
    List<Course> findTop6ByPublishedTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "manager"})
    List<Course> findByPublishedTrueOrderByTitleAsc();

    @EntityGraph(attributePaths = {"category", "manager"})
    List<Course> findByPublishedTrueAndTitleContainingIgnoreCaseOrPublishedTrueAndDescriptionContainingIgnoreCaseOrderByTitleAsc(
            String titleKeyword,
            String descriptionKeyword
    );

    long countByPublishedTrue();
}
