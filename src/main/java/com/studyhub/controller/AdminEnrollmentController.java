package com.studyhub.controller;

import com.studyhub.dto.EnrollmentDTO;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.repository.CourseRepository;
import com.studyhub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/enrollments")
@RequiredArgsConstructor
public class AdminEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseRepository courseRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) EnrollmentStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<EnrollmentDTO> enrollmentPage =
                enrollmentService.findByFilters(courseId, status, keyword, page, 10);

        model.addAttribute("enrollments", enrollmentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", enrollmentPage.getTotalPages());
        model.addAttribute("courses", enrollmentService.findAllCourses());
        model.addAttribute("statuses", EnrollmentStatus.values());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin/enrollments/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dto", new EnrollmentDTO());
        model.addAttribute("courses", enrollmentService.findAllCourses());
        model.addAttribute("statuses", EnrollmentStatus.values());
        model.addAttribute("isNew", true);
        return "admin/enrollments/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("dto", enrollmentService.findById(id));
        model.addAttribute("courses", enrollmentService.findAllCourses());
        model.addAttribute("statuses", EnrollmentStatus.values());
        model.addAttribute("isNew", false);
        return "admin/enrollments/form";
    }

    @PostMapping
    public String create(@ModelAttribute EnrollmentDTO dto, RedirectAttributes ra) {
        try {
            enrollmentService.create(dto);
            ra.addFlashAttribute("successMessage", "Enrollment created successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute EnrollmentDTO dto,
                         RedirectAttributes ra) {
        try {
            enrollmentService.update(id, dto);
            ra.addFlashAttribute("successMessage", "Enrollment updated successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            enrollmentService.delete(id);
            ra.addFlashAttribute("successMessage", "Enrollment deleted.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }
}