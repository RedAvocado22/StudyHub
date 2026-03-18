package com.studyhub.controller;

import com.studyhub.dto.EnrollmentDTO;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.enums.PaymentMethod;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.CourseContentService;
import com.studyhub.service.CourseManagementService;
import com.studyhub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/courses/{id}/enroll")
    public String enrollForm(@PathVariable Long id,
                             @AuthenticationPrincipal StudyHubUserDetails principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            var course = courseManagementService.findById(id);
            if (!course.isPublished()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This course is not available.");
                return "redirect:/courses";
            }

            if (principal != null) {
                EnrollmentStatus status = enrollmentService.findStatusByCourseAndUser(id, principal.getUser().getId());
                if (status == EnrollmentStatus.APPROVED) {
                    return "redirect:/my-courses/" + id + "/lessons";
                }
                if (status == EnrollmentStatus.PENDING) {
                    redirectAttributes.addFlashAttribute("infoMessage", "You already have a pending enrollment for this course.");
                    return "redirect:/courses/" + id;
                }
            }

            EnrollmentDTO dto = new EnrollmentDTO();
            if (principal != null) {
                dto.setUsernameOrEmail(principal.getUser().getEmail());
                dto.setFullName(principal.getUser().getFullName());
                dto.setEmail(principal.getUser().getEmail());
                dto.setMobile(principal.getUser().getMobile());
            }
            dto.setCourseId(id);

            model.addAttribute("course", course);
            model.addAttribute("enrollmentDTO", dto);
            model.addAttribute("paymentMethods", PaymentMethod.values());
            return "courses/enroll";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/courses";
        }
    }

    @PostMapping("/courses/{id}/enroll")
    public String submitEnroll(@PathVariable Long id,
                               @ModelAttribute EnrollmentDTO enrollmentDTO,
                               @AuthenticationPrincipal StudyHubUserDetails principal,
                               RedirectAttributes redirectAttributes) {
        try {
            enrollmentDTO.setCourseId(id);
            enrollmentService.create(enrollmentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Enrollment submitted successfully. Please wait for approval.");
            if (principal != null) {
                return "redirect:/enrollments/me";
            }
            return "redirect:/courses/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + id + "/enroll";
        }
    }
}
