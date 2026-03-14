package com.studyhub.controller;

import com.studyhub.dto.EnrollmentDTO;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/enrollments")
@RequiredArgsConstructor
public class AdminEnrollmentController {

    private final EnrollmentService enrollmentService;

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @GetMapping
    public String list(@AuthenticationPrincipal StudyHubUserDetails principal,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) EnrollmentStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "enrolledAt") String sortBy,
                       @RequestParam(defaultValue = "desc") String direction,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<EnrollmentDTO> enrollmentPage =
                enrollmentService.findByFilters(null, courseId, status, keyword, page, 10, sort);

        model.addAttribute("enrollments", enrollmentPage.getContent());
        model.addAttribute("page", enrollmentPage);
        model.addAttribute("courses", enrollmentService.findAllCourses());
        model.addAttribute("statuses", EnrollmentStatus.values());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("queryString", buildQueryString(courseId, status, keyword, sortBy, direction));
        return "admin/enrollments/list";
    }

    private String buildQueryString(Long courseId, EnrollmentStatus status, String keyword, String sortBy, String direction) {
        StringBuilder sb = new StringBuilder();
        if (courseId != null) sb.append("&courseId=").append(courseId);
        if (status != null) sb.append("&status=").append(status);
        if (keyword != null && !keyword.isBlank()) sb.append("&keyword=").append(keyword);
        if (sortBy != null) sb.append("&sortBy=").append(sortBy);
        if (direction != null) sb.append("&direction=").append(direction);
        return sb.toString();
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
