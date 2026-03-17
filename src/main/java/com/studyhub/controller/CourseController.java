package com.studyhub.controller;

import com.studyhub.service.CourseManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

import java.util.HashSet;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class CourseController {

    private final CourseManagementService courseManagementService;

    @GetMapping("/courses")
    public String publicCourses(@RequestParam(required = false) String keyword,
                                Model model,
                                Principal principal) {


        model.addAttribute("keyword", keyword);
        model.addAttribute("courses", courseManagementService.searchActiveCoursesSortedByName(keyword));


        Set<Long> enrolledCourseIds = new HashSet<>();
        if (principal != null) {
            enrolledCourseIds = courseManagementService.getEnrolledCourseIds(principal.getName());
        }
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        return "courses/list";
    }
}
