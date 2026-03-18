package com.studyhub.dto;

import com.studyhub.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    @NotBlank(message = "Không được để trống")
    @Pattern(regexp = "^[a-zA-ZÀ-ỹ\\s]*$",message = "Chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;
    @NotBlank(message = "Không được để trống")
    @Pattern(regexp = "^[a-zA-Z]*$",message = "Chỉ được phép nhập chữ cái")
    private String username;
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải gồm đúng 10 chữ số")
    private String mobile;
    private UserRole role;
}
