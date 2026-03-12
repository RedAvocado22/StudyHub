package com.studyhub.service;

import com.studyhub.dto.MyCourseDTO;
import com.studyhub.enums.EnrollmentStatus;
import com.studyhub.model.Enrollment;
import com.studyhub.model.User;
import com.studyhub.repository.EnrollmentRepository;
import com.studyhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonAccessService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public List<MyCourseDTO> getApprovedCoursesByUsername(String username) {
        User user = userRepository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return enrollmentRepository.findByUserAndStatusOrderByEnrolledAtDesc(user, EnrollmentStatus.APPROVED)
                .stream()
                .map(this::toMyCourseDto)
                .toList();
    }

    private MyCourseDTO toMyCourseDto(Enrollment enrollment) {
        return new MyCourseDTO(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getThumbnailUrl(),
                enrollment.getCourse().getPrice(),
                enrollment.getProgress(),
                enrollment.getEnrolledAt()
        );
    }
}
