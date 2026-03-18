package com.studyhub.dto;

import com.studyhub.enums.LessonContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDTO {
    private Long id;
    private String title;
    private LessonContentType contentType;
    private String contentUrl;
    private String contentText;
    private Integer durationMinutes;
    private Integer order;
    private boolean preview;
    private boolean active;
}
