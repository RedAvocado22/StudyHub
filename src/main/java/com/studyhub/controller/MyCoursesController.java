package com.studyhub.controller;

import com.studyhub.service.LessonAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/my-courses")
@RequiredArgsConstructor
public class MyCoursesController {

    private final LessonAccessService lessonAccessService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("courses", lessonAccessService.getApprovedCoursesByUsername(userDetails.getUsername()));
        return "user/my-courses";
    }

    @GetMapping("/{courseId}/lessons")
    public String viewLessons(@PathVariable Long courseId,
                              @RequestParam(required = false) Long lessonId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        model.addAttribute("viewer", lessonAccessService.getLessonViewer(userDetails.getUsername(), courseId, lessonId));
        return "user/lesson-viewer";
    }
}
