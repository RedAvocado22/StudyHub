package com.studyhub.controller;

import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.CourseContentService;
import com.studyhub.service.CourseManagementService;
import com.studyhub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class CourseController {

    private final CourseManagementService courseManagementService;
    private final CourseContentService courseContentService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/courses")
    public String publicCourses(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Long categoryId,
                                Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("categories", courseManagementService.getCategories());
        model.addAttribute("courses", courseManagementService.searchActiveCoursesSortedByName(keyword, categoryId));
        return "courses/list";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id,
                               @AuthenticationPrincipal StudyHubUserDetails principal,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            var course = courseManagementService.findById(id);
            if (!course.isPublished()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This course is not available.");
                return "redirect:/courses";
            }
            var chapters = courseContentService.getChaptersByCourseId(id);

            EnrollmentStatus enrollmentStatus = null;
            if (principal != null) {
                enrollmentStatus = enrollmentService.findStatusByCourseAndUser(id, principal.getUser().getId());
            }

            model.addAttribute("course", course);
            model.addAttribute("chapters", chapters);
            model.addAttribute("enrollmentStatus", enrollmentStatus);
            return "courses/detail";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/courses";
        }
    }

}
