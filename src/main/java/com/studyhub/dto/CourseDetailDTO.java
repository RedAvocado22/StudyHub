package com.studyhub.dto;

import com.studyhub.enums.CourseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private CourseLevel level;
    private Integer durationHours;
    private boolean published;
    private String categoryName;
    private Long managerId;
    private String managerName;
    private String managerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
