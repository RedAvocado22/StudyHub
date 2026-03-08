package com.studyhub.service;

import com.studyhub.dto.CourseCardDTO;
import com.studyhub.enums.UserRole;
import com.studyhub.model.Course;
import com.studyhub.repository.CourseRepository;
import com.studyhub.repository.SettingRepository;
import com.studyhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseManagementService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SettingRepository settingRepository;

    public List<CourseCardDTO> getFeaturedCourses() {
        return courseRepository.findTop6ByPublishedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toCourseCardDTO)
                .toList();
    }

    public List<CourseCardDTO> getAllActiveCoursesSortedByName() {
        return courseRepository.findByPublishedTrueOrderByTitleAsc()
                .stream()
                .map(this::toCourseCardDTO)
                .toList();
    }

    public List<CourseCardDTO> searchActiveCoursesSortedByName(String keyword) {
        if (!hasText(keyword)) {
            return getAllActiveCoursesSortedByName();
        }

        String normalizedKeyword = keyword.trim();
        return courseRepository
                .findByPublishedTrueAndTitleContainingIgnoreCaseOrPublishedTrueAndDescriptionContainingIgnoreCaseOrderByTitleAsc(
                        normalizedKeyword,
                        normalizedKeyword
                )
                .stream()
                .map(this::toCourseCardDTO)
                .toList();
    }

    public long countPublishedCourses() {
        return courseRepository.countByPublishedTrue();
    }

    public long countStudents() {
        return userRepository.countByRole(UserRole.MEMBER);
    }

    public long countCategories() {
        return settingRepository.countByType_Name("Course Category");
    }

    private CourseCardDTO toCourseCardDTO(Course course) {
        return new CourseCardDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPrice(),
                course.getLevel(),
                course.getDurationHours(),
                course.getCategory() != null ? course.getCategory().getName() : null
        );
    }
}
