package com.studyhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyCourseDTO {
    private Long enrollmentId;
    private Long courseId;
    private String title;
    private String thumbnailUrl;
    private BigDecimal price;
    private BigDecimal progress;
    private LocalDateTime enrolledAt;
}
