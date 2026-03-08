package com.studyhub.controller;

import com.studyhub.dto.EnrollmentDTO;
import com.studyhub.model.User;
import com.studyhub.repository.UserRepository;
import com.studyhub.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/my-enrollments")
@RequiredArgsConstructor
public class MyEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = requireCurrentUser(userDetails);
        List<EnrollmentDTO> enrollments = enrollmentService.findByUser(user);
        model.addAttribute("enrollments", enrollments);
        return "user/my-enrollments";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes ra) {
        User user = requireCurrentUser(userDetails);
        try {
            enrollmentService.cancelByUser(id, user);
            ra.addFlashAttribute("successMessage", "Enrollment cancelled.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-enrollments";
    }


        private User requireCurrentUser(UserDetails userDetails) {
            String principal = userDetails.getUsername();
            return userRepository.findByEmailOrUsername(principal, principal)
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
        }
}