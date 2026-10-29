package com.devvinnii.clientmanager.api.mapper;

import com.devvinnii.clientmanager.api.dto.EnrollmentResponseDTO;
import com.devvinnii.clientmanager.api.model.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    public EnrollmentResponseDTO toResponse(Enrollment enrollment) {
        return new EnrollmentResponseDTO(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getStudent().getCpf(),
                enrollment.getStudent().getPhotoUrl(),
                enrollment.getStudent().isActive(),
                enrollment.getClassGroup().getId(),
                enrollment.getClassGroup().getName(),
                enrollment.getClassGroup().getModality(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus(),
                enrollment.getCancelledAt(),
                enrollment.getUpdatedAt()
        );
    }
}
