package com.studyhub.service;

import com.studyhub.dto.CourseCardDTO;
import com.studyhub.dto.CourseDetailDTO;
import com.studyhub.dto.CourseListDTO;
import com.studyhub.dto.SettingListItemDTO;
import com.studyhub.dto.UserListDTO;
import com.studyhub.enums.CourseLevel;
import com.studyhub.enums.UserRole;
import com.studyhub.model.Course;
import com.studyhub.model.Setting;
import com.studyhub.model.User;
import com.studyhub.repository.CourseRepository;
import com.studyhub.repository.SettingRepository;
import com.studyhub.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    public Page<CourseListDTO> findAll(String search, Long categoryId, Long managerId,
                                       BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Course, Setting> category = root.join("category", JoinType.LEFT);
            Join<Course, User> manager = root.join("manager", JoinType.LEFT);

            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            }
            if (categoryId != null) predicates.add(cb.equal(category.get("id"), categoryId));
            if (managerId != null) predicates.add(cb.equal(manager.get("id"), managerId));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return courseRepository.findAll(spec, pageable).map(this::toCourseListDTO);
    }

    public CourseDetailDTO findById(Long id) {
        return courseRepository.findWithDetailsById(id)
                .map(this::toCourseDetailDTO)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
    }

    public List<SettingListItemDTO> getCategories() {
        return settingRepository.findByType_Name("Course Category")
                .stream()
                .map(s -> new SettingListItemDTO(s.getId(), s.getName(), null, s.getValue(), s.getPriority(), s.getStatus()))
                .toList();
    }

    public List<UserListDTO> getManagers() {
        return userRepository.findByRole(UserRole.MANAGER)
                .stream()
                .map(u -> UserListDTO.builder().id(u.getId()).fullName(u.getFullName()).build())
                .toList();
    }

    private CourseListDTO toCourseListDTO(Course c) {
        return CourseListDTO.builder()
                .id(c.getId()).title(c.getTitle())
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : "—")
                .managerName(c.getManager() != null ? c.getManager().getFullName() : "—")
                .price(c.getPrice()).level(c.getLevel())
                .published(c.isPublished()).durationHours(c.getDurationHours())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private CourseDetailDTO toCourseDetailDTO(Course c) {
        return CourseDetailDTO.builder()
                .id(c.getId()).title(c.getTitle()).description(c.getDescription())
                .thumbnailUrl(c.getThumbnailUrl()).price(c.getPrice())
                .level(c.getLevel()).durationHours(c.getDurationHours()).published(c.isPublished())
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : "—")
                .managerId(c.getManager() != null ? c.getManager().getId() : null)
                .managerName(c.getManager() != null ? c.getManager().getFullName() : "—")
                .managerEmail(c.getManager() != null ? c.getManager().getEmail() : null)
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt())
                .build();
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
