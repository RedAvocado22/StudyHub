package com.studyhub.repository;

import com.studyhub.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
            SELECT DISTINCT c FROM Comment c 
            LEFT JOIN FETCH c.user 
            LEFT JOIN FETCH c.replies r 
            LEFT JOIN FETCH r.user 
            WHERE c.post.id = :postId 
            AND c.parent IS NULL 
            ORDER BY c.commentId DESC
            """)
    List<Comment> findCommentsWithRepliesAndUsers(@Param("postId") Long postId);
}
