package com.studyhub.service;

import com.studyhub.dto.CreateUserDTO;
import com.studyhub.dto.UserDetailDTO;
import com.studyhub.dto.UserListDTO;
import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import com.studyhub.model.User;
import com.studyhub.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public Page<UserListDTO> findAll(Long excludeId, String search, UserRole role, UserStatus status, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("id"), excludeId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }
            if (role != null) predicates.add(cb.equal(root.get("role"), role));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable).map(this::toListDTO);
    }

    public UserDetailDTO findById(Long id) {
        return userRepository.findById(id)
                .map(this::toDetailDTO)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Transactional
    public void updateStatus(Long id, UserStatus newStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setStatus(newStatus);
    }

    @Transactional
    public void updateRole(Long id, UserRole newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setRole(newRole);
    }

    public Map<String, Long> getDashboardStats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "admins", userRepository.countByRole(UserRole.ADMIN),
                "managers", userRepository.countByRole(UserRole.MANAGER),
                "marketing", userRepository.countByRole(UserRole.MARKETING),
                "members", userRepository.countByRole(UserRole.MEMBER),
                "active", userRepository.countByStatus(UserStatus.ACTIVE),
                "inactive", userRepository.countByStatus(UserStatus.INACTIVE),
                "unverified", userRepository.countByStatus(UserStatus.UNVERIFIED)
        );
    }

    @Transactional
    public User createUser(CreateUserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        String rawPassword = generatePassword();
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .username(generateUsername(dto.getEmail()))
                .password(passwordEncoder.encode(rawPassword))
                .mobile(dto.getMobile())
                .role(dto.getRole())
                .status(dto.getStatus())
                .profileImage(dto.getProfileImageUrl())
                .build();
        userRepository.save(user);
        emailService.sendNewAccountEmail(user, rawPassword);
        return user;
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (!userRepository.existsByUsername(base)) return base;
        String candidate;
        do {
            candidate = base + ThreadLocalRandom.current().nextInt(100, 9999);
        } while (userRepository.existsByUsername(candidate));
        return candidate;
    }

    private String generatePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private UserListDTO toListDTO(User u) {
        return UserListDTO.builder()
                .id(u.getId()).fullName(u.getFullName()).username(u.getUsername())
                .email(u.getEmail()).mobile(u.getMobile()).role(u.getRole())
                .status(u.getStatus()).createdAt(u.getCreatedAt())
                .build();
    }

    private UserDetailDTO toDetailDTO(User u) {
        return UserDetailDTO.builder()
                .id(u.getId()).fullName(u.getFullName()).username(u.getUsername())
                .email(u.getEmail()).mobile(u.getMobile()).profileImage(u.getProfileImage())
                .role(u.getRole()).status(u.getStatus())
                .createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt())
                .build();
    }
}
