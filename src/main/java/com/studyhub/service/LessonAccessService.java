package com.studyhub.service;

import com.studyhub.dto.LessonViewerDTO;
import com.studyhub.dto.MyCourseDTO;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.model.*;
import com.studyhub.repository.CourseRepository;
import com.studyhub.repository.EnrollmentRepository;
import com.studyhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonAccessService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public List<MyCourseDTO> getApprovedCoursesByUsername(String username) {
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return enrollmentRepository.findByUserAndStatusOrderByEnrolledAtDesc(user, EnrollmentStatus.APPROVED)
                .stream()
                .map(this::toMyCourseDto)
                .filter(MyCourseDTO::isPublished)
                .toList();
    }

    public LessonViewerDTO getLessonViewer(String username, Long courseId, Long lessonId) {
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Course course = courseRepository.findWithChaptersAndLessonsById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));
        if (!course.isPublished()) {
            throw new IllegalArgumentException("Course is not published.");
        }

        List<LessonViewerDTO.ChapterItem> chapters = course.getChapters().stream()
                .filter(Chapter::isActive)
                .sorted(Comparator.comparing(Chapter::getOrder))
                .map(this::toChapterItem)
                .filter(chapter -> !chapter.getLessons().isEmpty())
                .toList();

        LessonViewerDTO.LessonItem activeLesson = resolveActiveLesson(chapters, lessonId);
        if (activeLesson == null) {
            throw new IllegalArgumentException("No active lessons found.");
        }

        if (!activeLesson.isPreview()) {
            if (!enrollmentRepository.existsByUserAndCourse_IdAndStatus(user, courseId, EnrollmentStatus.APPROVED)) {
                throw new IllegalArgumentException("You do not have access to this course.");
            }
        }

        Long enrollmentId = enrollmentRepository
                .findByUserAndCourse_IdAndStatus(user, courseId, EnrollmentStatus.APPROVED)
                .map(Enrollment::getId)
                .orElse(null);

        return LessonViewerDTO.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .chapters(chapters)
                .activeLesson(activeLesson)
                .enrollmentId(enrollmentId)
                .build();
    }

    private LessonViewerDTO.ChapterItem toChapterItem(Chapter chapter) {
        List<LessonViewerDTO.LessonItem> lessons = chapter.getLessons().stream()
                .filter(Lesson::isActive)
                .sorted(Comparator.comparing(Lesson::getOrder))
                .map(this::toLessonItem)
                .toList();

        return LessonViewerDTO.ChapterItem.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .lessons(lessons)
                .build();
    }

    private LessonViewerDTO.LessonItem toLessonItem(Lesson lesson) {
        return LessonViewerDTO.LessonItem.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .contentType(lesson.getContentType())
                .contentUrl(lesson.getContentUrl())
                .contentText(lesson.getContentText())
                .durationMinutes(lesson.getDurationMinutes())
                .preview(lesson.isPreview())
                .build();
    }

    private LessonViewerDTO.LessonItem resolveActiveLesson(List<LessonViewerDTO.ChapterItem> chapters, Long lessonId) {
        if (lessonId != null) {
            for (LessonViewerDTO.ChapterItem chapter : chapters) {
                for (LessonViewerDTO.LessonItem lesson : chapter.getLessons()) {
                    if (Objects.equals(lesson.getId(), lessonId)) {
                        return lesson;
                    }
                }
            }
        }

        return chapters.stream()
                .flatMap(chapter -> chapter.getLessons().stream())
                .findFirst()
                .orElse(null);
    }

    private MyCourseDTO toMyCourseDto(Enrollment enrollment) {
        return new MyCourseDTO(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getThumbnailUrl(),
                enrollment.getCourse().getPrice(),
                enrollment.getProgress(),
                enrollment.getEnrolledAt(),
                enrollment.getCourse().isPublished()
        );
    }
}
