package com.studyhub.controller;

import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.CourseManagementService;
import com.studyhub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EnrollmentService enrollmentService;
    private final CourseManagementService courseManagementService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal StudyHubUserDetails principal, Model model) {
        model.addAttribute("featuredCourses", courseManagementService.getFeaturedCourses());
        model.addAttribute("totalCourses", courseManagementService.countPublishedCourses());
        model.addAttribute("totalStudents", courseManagementService.countStudents());
        model.addAttribute("totalCategories", courseManagementService.countCategories());

        Set<Long> enrolledCourseIds = new HashSet<>();
        if (principal != null) {
            enrolledCourseIds = enrollmentService.findEnrolledCourseIdsByUser(
                    principal.getUser().getId());
        }
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        return "home";
    }
}