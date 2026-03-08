package com.studyhub.dto;

import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String usernameOrEmail;
    private String fullName;
    private String email;
    private String mobile;
    private String enrollReason;
    private PaymentMethod paymentMethod;
    private BigDecimal fee;
    private EnrollmentStatus status;
    private String rejectedNotes;
    private BigDecimal progress;
    private LocalDateTime completedAt;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastUpdated;
}