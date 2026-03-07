package com.studyhub.controller;

import com.studyhub.service.CourseManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CourseManagementService courseManagementService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredCourses", courseManagementService.getFeaturedCourses());
        model.addAttribute("totalCourses", courseManagementService.countPublishedCourses());
        model.addAttribute("totalStudents", courseManagementService.countStudents());
        model.addAttribute("totalCategories", courseManagementService.countCategories());
        return "home";
    }
}
