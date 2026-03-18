package com.studyhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @Pattern(regexp = "^$|^[0-9]{10,15}$", message = "Mobile must be 10–15 digits")
    private String mobile;
}
