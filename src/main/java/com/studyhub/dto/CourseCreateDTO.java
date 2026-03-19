package com.studyhub.dto;

import com.studyhub.enums.CourseLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String thumbnailUrl;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be 0 or greater")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @NotNull(message = "Level is required")
    private CourseLevel level;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    @Max(value = 9999, message = "Duration must not exceed 9999 hours")
    private Integer durationHours;

    private Long categoryId;

    @NotNull(message = "Manager is required")
    private Long managerId;
}
