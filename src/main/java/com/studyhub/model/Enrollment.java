package com.studyhub.model;

import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // The user who registered (can be null for guest)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // The person who will actually learn (may differ from user)
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String mobile;

    @Column(columnDefinition = "TEXT")
    private String enrollReason;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(columnDefinition = "TEXT")
    private String rejectedNotes;

    // Progress percentage (0.00 - 100.00)
    @Column(precision = 5, scale = 2)
    private BigDecimal progress;

    private LocalDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (status == null) status = EnrollmentStatus.PENDING;
        if (progress == null) progress = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
