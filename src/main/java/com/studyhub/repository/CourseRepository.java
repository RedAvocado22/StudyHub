package com.studyhub.repository;

import com.studyhub.model.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

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

    long countByPublishedTrueAndManager_Id(Long managerId);

    @EntityGraph(attributePaths = {"category", "manager"})
    Optional<Course> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"chapters", "chapters.lessons", "manager"})
    Optional<Course> findWithChaptersAndLessonsById(Long id);
}
