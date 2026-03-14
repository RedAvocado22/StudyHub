package com.studyhub.repository;

import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.model.Enrollment;
import com.studyhub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = "course")
    List<Enrollment> findByUserOrderByEnrolledAtDesc(User user);


    Optional<Enrollment> findById(Long id);

    @Query(
            value = """
        SELECT e FROM Enrollment e
        JOIN FETCH e.course c
        LEFT JOIN FETCH e.user u
        WHERE (:userId IS NULL OR u.id = :userId)
          AND (:courseId IS NULL OR c.id = :courseId)
          AND (:status   IS NULL OR e.status = :status)
          AND (
               :keyword IS NULL
            OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(e.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.title)    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """,
            countQuery = """
        SELECT COUNT(e) FROM Enrollment e
        JOIN e.course c
        LEFT JOIN e.user u
        WHERE (:userId IS NULL OR u.id = :userId)
          AND (:courseId IS NULL OR c.id = :courseId)
          AND (:status   IS NULL OR e.status = :status)
          AND (
               :keyword IS NULL
            OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(e.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.title)    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """
    )
    Page<Enrollment> findByFilters(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("status") EnrollmentStatus status,
            @Param("keyword")  String keyword,
            Pageable pageable
    );
}
