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
public class CourseListDTO {
    private Long id;
    private String title;
    private String categoryName;
    private String managerName;
    private BigDecimal price;
    private CourseLevel level;
    private boolean published;
    private Integer durationHours;
    private LocalDateTime createdAt;
}
