package com.studyhub.service;

import com.studyhub.enums.UserRole;
import com.studyhub.model.Comment;
import com.studyhub.model.Post;
import com.studyhub.model.Setting;
import com.studyhub.model.User;
import com.studyhub.repository.CommentRepository;
import com.studyhub.repository.PostRepository;
import com.studyhub.repository.SettingRepository;
import com.studyhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PostListService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private CommentRepository commentRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAllWithRelations();
    }

    public List<Post> filterPosts(String title, String category, String author, Long authorId, org.springframework.data.domain.Sort sort) {
        return postRepository.filterPosts(title, category, author, authorId, sort);
    }

    public List<Setting> getAllCategories() {
        return settingRepository.findAllCategoriesFetch();
    }

    public List<User> getAllAuthors() {
        return userRepository.findAll();
    }

    public Setting findCategoryById(String category) {
        Long id = Long.parseLong(String.valueOf(category));
        return settingRepository.findById(id).get();
    }

    public void savePost(Post post) {
        postRepository.save(post);
    }

    @Transactional
    public void switchPostStatus(Long id, String status, Long callerId, UserRole callerRole) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
        if (callerRole == UserRole.MARKETING && !post.getAuthor().getId().equals(callerId)) {
            throw new AccessDeniedException("You can only change the status of your own posts.");
        }
        post.setStatus("Published".equals(status) ? "Hidden" : "Published");
        postRepository.save(post);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own comments.");
        }
        commentRepository.deleteById(commentId);
    }

    public Post findPostById(Long id) {
        return postRepository.findById(id).get();
    }

    public List<Post> getAllBlogs() {
        return postRepository.findByStatus("Published");
    }

    public List<Post> findBlogsByCategoryId(Long id) {
        return postRepository.findByCategoryIdAndStatus(id, "Published");
    }

    public List<Post> findBlogsByTitle(String title) {
        return postRepository.findByStatusAndTitleContainingIgnoreCase("Published", title);
    }

    public List<Comment> getCommentsByPostId(Long id) {
        return commentRepository.findCommentsWithRepliesAndUsers(id);
    }

    public Post findPostByIdFetch(Long id) {
        return postRepository.findPostWithCategory(id).get();
    }

    public void saveComment(Comment newComment) {
        commentRepository.save(newComment);
    }

    public Comment getCommentById(Long parentId) {
        return commentRepository.findById(parentId).get();
    }
}
