package com.studyhub.controller;

import com.studyhub.model.Comment;
import com.studyhub.model.Post;
import com.studyhub.model.Setting;
import com.studyhub.model.User;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.PostListService;
import com.studyhub.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/blogs")
public class BlogsController {
    PostListService postListService;
    UserProfileService userProfileService;
    @Autowired
    BlogsController(PostListService postListService,UserProfileService userProfileService) {
        this.postListService = postListService;
        this.userProfileService = userProfileService;
    }

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @GetMapping
    public String getBlogLists(@AuthenticationPrincipal StudyHubUserDetails userDetails,
                               Model model,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) String title) {
        User user=userProfileService.getUserById(userDetails.getUser().getId());
        List<Setting> categories=postListService.getAllCategories();
        if(!model.containsAttribute("blogs")) {
            List<Post> blogs;
            if (categoryId != null) {
                blogs = postListService.findBlogsByCategoryId(categoryId);
            } else if (title != null && !title.isEmpty()) {
                blogs = postListService.findBlogsByTitle(title);
            } else {
                blogs = postListService.getAllBlogs();
            }
            model.addAttribute("blogs", blogs);
        }
        model.addAttribute("user",user);
        model.addAttribute("categories",categories);
        return "blogs";
    }
    @GetMapping("/blogs-category/{id}")
    public String searchBlogsByCategory(RedirectAttributes ra,
                                        @PathVariable("id") Long id) {
        List<Post> blogs=postListService.findBlogsByCategoryId(id);
        ra.addFlashAttribute("blogs",blogs);
        return "redirect:/blogs?categoryId=" + id;
    }
    @PostMapping("/blogs-search")
    public String searchBlogByTitle(RedirectAttributes ra,
                                    @RequestParam(name = "title") String title) {
        List<Post> blogs=postListService.findBlogsByTitle(title);
        ra.addFlashAttribute("blogs",blogs);
        return "redirect:/blogs?title=" + title;
    }
    @GetMapping("/detail")
    public String blogDetail(@AuthenticationPrincipal StudyHubUserDetails userDetails,
                             Model model,
                             @RequestParam(name = "id") Long id) {
        User user=userDetails.getUser();
        Post post=postListService.findPostByIdFetch(id);
        List<Comment> comments=postListService.getCommentsByPostId(id);
        List<Setting> categories=postListService.getAllCategories();
        model.addAttribute("user",user);
        model.addAttribute("post",post);
        model.addAttribute("comments",comments);
        model.addAttribute("categories",categories);
        return "blogDetail";
    }
    @PostMapping("/comment")
    public String postComment(@AuthenticationPrincipal StudyHubUserDetails userDetails,
                              @RequestParam("postId") Long postId,
                              @RequestParam("comment") String commentContent,
                              @RequestParam(name="parentId",required=false) Long parentId,
                              RedirectAttributes ra) {

        // 1. Lấy thông tin bài viết và người dùng
        Post post = postListService.findPostById(postId);
        User user = userDetails.getUser(); // Hoặc lấy từ DB nếu cần object managed
        // 2. Tạo đối tượng Comment mới
        Comment.CommentBuilder commentBuilder  = Comment.builder()
                .comment(commentContent)
                .post(post)
                .user(user);
        if (parentId != null) {
            Comment parentComment = postListService.getCommentById(parentId);
            commentBuilder.parent(parentComment);
        }
        postListService.saveComment(commentBuilder.build());

        return "redirect:/blogs/detail?id=" + postId;
    }

}
