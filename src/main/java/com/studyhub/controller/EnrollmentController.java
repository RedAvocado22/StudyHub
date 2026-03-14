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
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @GetMapping("/me")
    public String list(@AuthenticationPrincipal StudyHubUserDetails userDetails,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) EnrollmentStatus status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "enrolledAt") String sortBy,
                       @RequestParam(defaultValue = "desc") String direction,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<EnrollmentDTO> enrollmentPage =
                enrollmentService.findByFilters(userDetails.getUser().getId(), courseId, status, keyword, page, 8, sort);

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

        return "user/my-enrollments";
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

    @PostMapping("/me/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @AuthenticationPrincipal StudyHubUserDetails userDetails,
                         RedirectAttributes ra) {
        try {
            enrollmentService.cancelByUser(id, userDetails.getUsername());
            ra.addFlashAttribute("successMessage", "Enrollment cancelled.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/enrollments/me";
    }
}
