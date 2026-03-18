package com.studyhub.controller;

import com.studyhub.dto.RegisterDTO;
import com.studyhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDTO registerDTO,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!registerDTO.getPassword().equals(registerDTO.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "error.dto", "Passwords do not match.");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(registerDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.dto", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            authService.verifyEmail(token);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Email verified! You can now log in.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/login";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email,
                                     RedirectAttributes redirectAttributes) {
        authService.resendVerification(email);
        redirectAttributes.addFlashAttribute("successMessage",
                "If that email is registered and unverified, a new verification link has been sent.");
        return "redirect:/login";
    }
}
