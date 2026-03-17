package com.studyhub.controller;

import com.studyhub.dto.*;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.model.User;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.CourseManagementService;
import com.studyhub.service.EnrollmentService;
import com.studyhub.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class EnrollmentController {
    private final PaymentService paymentService;
    private final EnrollmentService enrollmentService;
    private final CourseManagementService courseManagementService;

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @GetMapping("/enrollments/me")
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

    @PostMapping("/enrollments/me/{id}/cancel")
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
    @GetMapping("/courses/{id}/enroll")
    public String enrollForm(@PathVariable Long id,
                             @AuthenticationPrincipal StudyHubUserDetails principal,
                             Model model) {
        User user = principal.getUser();
        CourseDetailDTO course = courseManagementService.findById(id);

        EnrollRequestDTO dto = new EnrollRequestDTO();
        dto.setCourseId(id);
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());

        model.addAttribute("dto", dto);
        model.addAttribute("course", course);
        return "enroll/form";
    }

    @PostMapping("/courses/{id}/enroll")
    public String submitEnroll(@PathVariable Long id,
                               @Valid @ModelAttribute EnrollRequestDTO dto,
                               BindingResult result,
                               @AuthenticationPrincipal StudyHubUserDetails principal,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("dto", dto);
            model.addAttribute("course", courseManagementService.findById(id));
            return "enroll/form";
        }

        dto.setCourseId(id);

        try {
            PaymentResultDTO paymentResult = paymentService.enroll(dto, principal.getUser());

            if (paymentResult.getRedirectUrl() != null) {
                if (paymentResult.getRedirectUrl().equals("/my-enrollments")) {
                    ra.addFlashAttribute("successMessage", "Enrolled successfully!");
                }
                return "redirect:" + paymentResult.getRedirectUrl();
            }

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) ip = request.getRemoteAddr();
            String vnPayUrl = paymentService.createVnPayUrl(paymentResult.getEnrollmentId(), ip);
            return "redirect:" + vnPayUrl;

        } catch (IllegalArgumentException e) {
            model.addAttribute("dto", dto);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("course", courseManagementService.findById(id));
            return "enroll/form";
        }
    }
}
