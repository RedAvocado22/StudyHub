package com.studyhub.controller;

import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.CourseManagementService;
import com.studyhub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseManagementService courseManagementService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal StudyHubUserDetails principal, Model model) {
        model.addAttribute("featuredCourses", courseManagementService.getFeaturedCourses());
        model.addAttribute("totalCourses", courseManagementService.countPublishedCourses());
        model.addAttribute("totalStudents", courseManagementService.countStudents());
        model.addAttribute("totalCategories", courseManagementService.countCategories());

        if (principal != null) {
            Long userId = principal.getUser().getId();
            model.addAttribute("approvedCourseIds", enrollmentService.getCourseIdsByStatus(userId, EnrollmentStatus.APPROVED));
            model.addAttribute("pendingCourseIds", enrollmentService.getCourseIdsByStatus(userId, EnrollmentStatus.PENDING));
        } else {
            model.addAttribute("approvedCourseIds", Collections.emptySet());
            model.addAttribute("pendingCourseIds", Collections.emptySet());
        }

        return "home";
    }
}
