package com.studyhub.service;

import com.studyhub.dto.*;
import com.studyhub.enums.UserRole;
import com.studyhub.model.Course;
import com.studyhub.model.Setting;
import com.studyhub.model.User;
import com.studyhub.repository.CourseRepository;
import com.studyhub.repository.EnrollmentRepository;
import com.studyhub.repository.SettingRepository;
import com.studyhub.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final EnrollmentRepository enrollmentRepository;
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
        return searchActiveCoursesSortedByName(keyword, null);
    }

    public List<CourseCardDTO> searchActiveCoursesSortedByName(String keyword, Long categoryId) {
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("published")));
            if (hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return courseRepository.findAll(spec, Sort.by("title").ascending())
                .stream()
                .map(this::toCourseCardDTO)
                .toList();
    }

    public long countPublishedCourses() {
        return courseRepository.countByPublishedTrue();
    }

    public long countPublishedCoursesByManager(Long managerId) {
        return courseRepository.countByPublishedTrueAndManager_Id(managerId);
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

    @Transactional
    public void updateCourse(Long id, CourseUpdateDTO dto, UserRole callerRole) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setLevel(dto.getLevel());
        course.setDurationHours(dto.getDurationHours());

        if (callerRole != UserRole.MANAGER) {
            course.setPrice(dto.getPrice());
            course.setPublished(dto.isPublished());
        }

        if (dto.getCategoryId() != null) {
            Setting category = settingRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
            course.setCategory(category);
        } else {
            course.setCategory(null);
        }

        if (callerRole != UserRole.MANAGER && dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new NoSuchElementException("Manager not found"));
            course.setManager(manager);
        }

        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (enrollmentRepository.existsByCourse_Id(id)) {
            throw new IllegalStateException("Cannot delete a course that has enrollment records.");
        }
        courseRepository.deleteById(id);
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
