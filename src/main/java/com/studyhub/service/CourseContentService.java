package com.studyhub.service;

import com.studyhub.dto.ChapterDTO;
import com.studyhub.dto.LessonDTO;
import com.studyhub.model.Chapter;
import com.studyhub.model.Course;
import com.studyhub.model.Lesson;
import com.studyhub.repository.ChapterRepository;
import com.studyhub.repository.CourseRepository;
import com.studyhub.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseContentService {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;

    public List<ChapterDTO> getChaptersByCourseId(Long courseId) {
        return chapterRepository.findByCourseIdOrderByOrderAsc(courseId)
                .stream()
                .map(chapter -> {
                    List<LessonDTO> lessons = lessonRepository.findByChapterIdOrderByOrderAsc(chapter.getId())
                            .stream()
                            .map(this::toLessonDTO)
                            .toList();
                    return toChapterDTO(chapter, lessons);
                })
                .toList();
    }

    public List<LessonDTO> getLessonsByChapterId(Long chapterId) {
        return lessonRepository.findByChapterIdOrderByOrderAsc(chapterId)
                .stream()
                .map(this::toLessonDTO)
                .toList();
    }

    @Transactional
    public ChapterDTO addChapter(Long courseId, ChapterDTO dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        Chapter chapter = Chapter.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .order(dto.getOrder() != null ? dto.getOrder() : getNextChapterOrder(courseId))
                .course(course)
                .active(true)
                .build();

        return toChapterDTO(chapterRepository.save(chapter));
    }

    @Transactional
    public void updateChapter(Long id, ChapterDTO dto) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setOrder(dto.getOrder());
        chapter.setActive(dto.isActive());

        chapterRepository.save(chapter);
    }

    @Transactional
    public void deleteChapter(Long id) {
        chapterRepository.deleteById(id);
    }

    @Transactional
    public LessonDTO addLesson(Long chapterId, LessonDTO dto) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        Lesson lesson = Lesson.builder()
                .title(dto.getTitle())
                .contentType(dto.getContentType())
                .contentUrl(dto.getContentUrl())
                .contentText(dto.getContentText())
                .durationMinutes(dto.getDurationMinutes())
                .order(dto.getOrder() != null ? dto.getOrder() : getNextLessonOrder(chapterId))
                .chapter(chapter)
                .preview(dto.isPreview())
                .active(true)
                .build();

        return toLessonDTO(lessonRepository.save(lesson));
    }

    @Transactional
    public void updateLesson(Long id, LessonDTO dto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

        lesson.setTitle(dto.getTitle());
        lesson.setContentType(dto.getContentType());
        lesson.setContentUrl(dto.getContentUrl());
        lesson.setContentText(dto.getContentText());
        lesson.setDurationMinutes(dto.getDurationMinutes());
        lesson.setOrder(dto.getOrder());
        lesson.setPreview(dto.isPreview());
        lesson.setActive(dto.isActive());

        lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    private int getNextChapterOrder(Long courseId) {
        return chapterRepository.findByCourseIdOrderByOrderAsc(courseId).size() + 1;
    }

    private int getNextLessonOrder(Long chapterId) {
        return lessonRepository.findByChapterIdOrderByOrderAsc(chapterId).size() + 1;
    }

    private ChapterDTO toChapterDTO(Chapter c) {
        return toChapterDTO(c, null);
    }

    private ChapterDTO toChapterDTO(Chapter c, List<LessonDTO> lessons) {
        return ChapterDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .order(c.getOrder())
                .active(c.isActive())
                .lessons(lessons)
                .build();
    }

    private LessonDTO toLessonDTO(Lesson l) {
        return LessonDTO.builder()
                .id(l.getId())
                .title(l.getTitle())
                .contentType(l.getContentType())
                .contentUrl(l.getContentUrl())
                .contentText(l.getContentText())
                .durationMinutes(l.getDurationMinutes())
                .order(l.getOrder())
                .preview(l.isPreview())
                .active(l.isActive())
                .build();
    }
}
