package com.studyhub.service;

import com.studyhub.dto.RegisterDTO;
import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import com.studyhub.model.User;
import com.studyhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String username = generateUsername(dto.getEmail());

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .username(username)
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.MEMBER)
                .status(UserStatus.UNVERIFIED)
                .verificationToken(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification link."));

        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == UserStatus.UNVERIFIED) {
                user.setVerificationToken(UUID.randomUUID().toString());
                userRepository.save(user);
                emailService.sendVerificationEmail(user);
            }
        });
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (!userRepository.existsByUsername(base)) {
            return base;
        }
        String candidate;
        do {
            candidate = base + ThreadLocalRandom.current().nextInt(100, 9999);
        } while (userRepository.existsByUsername(candidate));
        return candidate;
    }
}
