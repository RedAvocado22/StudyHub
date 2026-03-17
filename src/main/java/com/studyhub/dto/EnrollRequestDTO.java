package com.studyhub.dto;

import com.studyhub.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollRequestDTO {

    @NotNull
    private Long courseId;

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    private String mobile;

    private String enrollNote;

    @NotNull
    private PaymentMethod paymentMethod;
}