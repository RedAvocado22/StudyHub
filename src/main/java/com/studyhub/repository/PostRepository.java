package com.studyhub.repository;

import com.studyhub.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
            """)
    List<Post> filterPosts(@Param("title") String title,
                           @Param("category") String category,
                           @Param("author") String author);
}
