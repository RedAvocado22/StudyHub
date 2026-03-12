package com.studyhub.controller;

import com.studyhub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/my-enrollments")
@RequiredArgsConstructor
public class MyEnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("enrollments", enrollmentService.findByUsername(userDetails.getUsername()));
        return "user/my-enrollments";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes ra) {
        try {
            enrollmentService.cancelByUser(id, userDetails.getUsername());
            ra.addFlashAttribute("successMessage", "Enrollment cancelled.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-enrollments";
    }
}
