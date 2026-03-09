package com.studyhub.dto;

import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDetailDTO {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String mobile;
    private String profileImage;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
