package com.studyhub.service;

import com.studyhub.dto.CourseDTO;
import com.studyhub.dto.EnrollmentDTO;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.model.Course;
import com.studyhub.model.Enrollment;
import com.studyhub.model.User;
import com.studyhub.repository.CourseRepository;
import com.studyhub.repository.EnrollmentRepository;
import com.studyhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public Page<EnrollmentDTO> findByFilters(Long userId, Long courseId, EnrollmentStatus status,
                                             String keyword, int page, int size, Sort sort) {
        return enrollmentRepository
                .findByFilters(userId, courseId, status, keyword, PageRequest.of(page, size, sort))
                .map(this::toDto);
    }

    public EnrollmentDTO findById(Long id) {
        return toDto(requireEnrollment(id));
    }
    public List<CourseDTO> findAllCourses() {
        return courseRepository.findAll(Sort.by(Sort.Direction.ASC, "title"))
                .stream().map(this::toCourseDto).toList();
    }

    private CourseDTO toCourseDto(Course c) {
        CourseDTO dto = new CourseDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setPrice(c.getPrice());
        dto.setLevel(c.getLevel());
        dto.setPublished(c.isPublished());
        return dto;
    }

    public List<EnrollmentDTO> findByUsername(String username) {
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return enrollmentRepository.findByUserOrderByEnrolledAtDesc(user)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void create(EnrollmentDTO dto) {
        Course course = requireCourse(dto.getCourseId());
        User user = resolveUser(dto.getUsernameOrEmail());

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);
        enrollment.setFullName(dto.getFullName());
        enrollment.setEmail(dto.getEmail());
        enrollment.setMobile(dto.getMobile());
        enrollment.setEnrollReason(dto.getEnrollReason());
        enrollment.setPaymentMethod(dto.getPaymentMethod());
        enrollment.setFee(dto.getFee() != null ? dto.getFee() : course.getPrice());
        enrollment.setStatus(dto.getStatus() != null ? dto.getStatus() : EnrollmentStatus.PENDING);
        enrollment.setRejectedNotes(dto.getRejectedNotes());
        enrollment.setProgress(dto.getProgress());
        enrollment.setCompletedAt(dto.getCompletedAt());

        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void update(Long id, EnrollmentDTO dto) {
        Enrollment enrollment = requireEnrollment(id);

        enrollment.setCourse(requireCourse(dto.getCourseId()));
        enrollment.setUser(resolveUser(dto.getUsernameOrEmail()));
        enrollment.setFullName(dto.getFullName());
        enrollment.setEmail(dto.getEmail());
        enrollment.setMobile(dto.getMobile());
        enrollment.setEnrollReason(dto.getEnrollReason());
        enrollment.setPaymentMethod(dto.getPaymentMethod());
        enrollment.setFee(dto.getFee());
        enrollment.setStatus(dto.getStatus());
        enrollment.setRejectedNotes(dto.getRejectedNotes());
        enrollment.setProgress(dto.getProgress());
        enrollment.setCompletedAt(dto.getCompletedAt());

        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void delete(Long id) {
        enrollmentRepository.deleteById(id);
    }

    @Transactional
    public void cancelByUser(Long id, String username) {
        User currentUser = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Enrollment enrollment = requireEnrollment(id);

        if (!enrollment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You are not allowed to cancel this enrollment.");
        }
        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new IllegalArgumentException("Only pending enrollments can be cancelled.");
        }

        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollment.setRejectedNotes("Cancelled by user.");
        enrollmentRepository.save(enrollment);
    }

    private Enrollment requireEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + id));
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
    }

    private User resolveUser(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) return null;
        return userRepository
                .findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElse(null);
    }

    private EnrollmentDTO toDto(Enrollment e) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(e.getId());
        dto.setCourseId(e.getCourse().getId());
        dto.setCourseTitle(e.getCourse().getTitle());
        dto.setUsernameOrEmail(e.getUser() != null ? e.getUser().getEmail() : null);
        dto.setFullName(e.getFullName());
        dto.setEmail(e.getEmail());
        dto.setMobile(e.getMobile());
        dto.setEnrollReason(e.getEnrollReason());
        dto.setPaymentMethod(e.getPaymentMethod());
        dto.setFee(e.getFee());
        dto.setStatus(e.getStatus());
        dto.setRejectedNotes(e.getRejectedNotes());
        dto.setProgress(e.getProgress());
        dto.setCompletedAt(e.getCompletedAt());
        dto.setEnrolledAt(e.getEnrolledAt());
        dto.setLastUpdated(e.getLastUpdated());
        return dto;
    }
    public Set<Long> findEnrolledCourseIdsByUser(Long userId) {
        return enrollmentRepository.findCourseIdsByUserId(userId);
    }
}