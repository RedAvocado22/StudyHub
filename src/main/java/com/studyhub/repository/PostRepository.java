package com.studyhub.repository;

import com.studyhub.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
            SELECT DISTINCT p 
            FROM Post p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.comments
            """)
    List<Post> findAllWithRelations();

    @Query("""
            SELECT DISTINCT p
            FROM Post p
            LEFT JOIN FETCH p.comments
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.author
            WHERE
            (:title IS NULL OR p.title LIKE %:title%)
            AND (:category IS NULL OR p.category.name = :category)
            AND (:author IS NULL OR p.author.fullName = :author)
            AND (:authorId IS NULL OR p.author.id = :authorId)
            """)
    List<Post> filterPosts(@Param("title") String title,
                           @Param("category") String category,
                           @Param("author") String author,
                           @Param("authorId") Long authorId,
                           org.springframework.data.domain.Sort sort);

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.author
            WHERE p.status = :status AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Post> findByStatusAndTitleContainingIgnoreCase(@Param("status") String status, @Param("keyword") String keyword);

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.author
            WHERE p.status = :status
            """)
    List<Post> findByStatus(@Param("status") String status);

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.author
            WHERE p.category.id = :categoryId AND p.status = :status
            """)
    List<Post> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") String status);

    @Query("""
            SELECT p FROM Post p 
            LEFT JOIN FETCH p.category 
            WHERE p.id = :id
            """)
    Optional<Post> findPostWithCategory(@Param("id") Long id);
}
