package com.studyhub.controller;

import com.studyhub.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PaymentService paymentService;

    @GetMapping("/payos/return")
    public String payosReturn(RedirectAttributes ra) {
        ra.addFlashAttribute("successMessage",
                "Payment received! Your enrollment is pending admin approval.");
        return "redirect:/enrollments/me";
    }

    // Thêm webhook endpoint
    @PostMapping("/payos/webhook")
    @ResponseBody
    public ResponseEntity<String> payosWebhook(@RequestBody Map<String, Object> payload) {
        try {
            paymentService.handlePayOSWebhook(payload);
        } catch (Exception e) {
            // Luôn trả 200 để PayOS không retry
        }
        return ResponseEntity.ok("OK");
    }
    @GetMapping("/payos/cancel")
    public String payosCancel(RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Payment cancelled.");
        return "redirect:/enrollments/me";
    }

    @GetMapping("/vnpay/return")
    public String vnpayReturn(@RequestParam Map<String, String> params,
                              RedirectAttributes ra) {
        try {
            boolean success = paymentService.handleVnPayReturn(params);
            if (success) {
                ra.addFlashAttribute("successMessage",
                        "Payment received! Your enrollment is pending admin approval.");
            } else {
                ra.addFlashAttribute("errorMessage", "Payment failed or invalid signature.");
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/enrollments/me";
    }
}