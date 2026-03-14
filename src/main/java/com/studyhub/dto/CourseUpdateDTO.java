package com.studyhub.dto;

import com.studyhub.enums.CourseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateDTO {
    private String title;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private CourseLevel level;
    private Integer durationHours;
    private Long categoryId;
    private Long managerId;
    private boolean published;
}
