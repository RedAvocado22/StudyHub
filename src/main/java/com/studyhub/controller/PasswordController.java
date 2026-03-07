package com.studyhub.controller;

import com.studyhub.dto.ChangePasswordDTO;
import com.studyhub.dto.ResetPasswordDTO;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 RedirectAttributes redirectAttributes) {
        passwordService.sendResetEmail(email);
        redirectAttributes.addFlashAttribute("successMessage",
                "If that email is registered, a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        ResetPasswordDTO resetDTO = new ResetPasswordDTO();
        resetDTO.setToken(token);
        model.addAttribute("resetDTO", resetDTO);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute ResetPasswordDTO resetDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            passwordService.resetPassword(resetDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password reset successfully. Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        model.addAttribute("changeDTO", new ChangePasswordDTO());
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordDTO changeDTO,
                                 BindingResult result,
                                 Model model,
                                 @AuthenticationPrincipal StudyHubUserDetails userDetails,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/change-password";
        }
        try {
            passwordService.changePassword(userDetails.getUser(), changeDTO);
            request.getSession().invalidate();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password changed. Please log in again.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/change-password";
        }
    }
}
