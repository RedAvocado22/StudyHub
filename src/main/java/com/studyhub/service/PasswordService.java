package com.studyhub.service;

import com.studyhub.dto.ChangePasswordDTO;
import com.studyhub.dto.ResetPasswordDTO;
import com.studyhub.model.User;
import com.studyhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset-expiry-minutes}")
    private int resetExpiryMinutes;

    @Transactional
    public void sendResetEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setResetPasswordToken(UUID.randomUUID().toString());
            user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(resetExpiryMinutes));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = userRepository.findByResetPasswordToken(dto.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link."));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("New passwords do not match.");
        }
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
