package com.studyhub.dto;

import com.studyhub.enums.LessonContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonViewerDTO {
    private Long courseId;
    private String courseTitle;
    private List<ChapterItem> chapters;
    private LessonItem activeLesson;
    private Long enrollmentId;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterItem {
        private Long id;
        private String title;
        private String description;
        private List<LessonItem> lessons = new ArrayList<>();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonItem {
        private Long id;
        private String title;
        private LessonContentType contentType;
        private String contentUrl;
        private String contentText;
        private Integer durationMinutes;
        private boolean preview;
    }
}
