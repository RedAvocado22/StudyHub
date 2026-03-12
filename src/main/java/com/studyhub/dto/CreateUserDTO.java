package com.studyhub.dto;

import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateUserDTO {
    private String fullName;
    private String email;
    private String mobile;
    private UserRole role;
    private UserStatus status;
    private String profileImageUrl;
}
