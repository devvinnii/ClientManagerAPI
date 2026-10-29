package com.devvinnii.clientmanager.api.repository;

import com.devvinnii.clientmanager.api.model.Enrollment;
import com.devvinnii.clientmanager.api.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    long countByClassGroupIdAndStatus(Long classGroupId, EnrollmentStatus status);

    boolean existsByClassGroupIdAndStudentIdAndStatus(Long classGroupId, Long studentId, EnrollmentStatus status);

    @EntityGraph(attributePaths = {"student", "classGroup"})
    Optional<Enrollment> findByIdAndClassGroupId(Long id, Long classGroupId);

    @EntityGraph(attributePaths = {"student", "classGroup"})
    List<Enrollment> findByClassGroupIdOrderByEnrolledAtDesc(Long classGroupId);

    @EntityGraph(attributePaths = {"student", "classGroup"})
    List<Enrollment> findByClassGroupIdAndStatusOrderByStudentNameAsc(Long classGroupId, EnrollmentStatus status);

    @EntityGraph(attributePaths = {"student", "classGroup"})
    List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(Long studentId);
}
