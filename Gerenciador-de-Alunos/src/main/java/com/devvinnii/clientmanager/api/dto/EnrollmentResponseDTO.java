package com.devvinnii.clientmanager.api.dto;

import com.devvinnii.clientmanager.api.model.EnrollmentStatus;
import com.devvinnii.clientmanager.api.model.Modality;

import java.time.Instant;

public record EnrollmentResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        String studentCpf,
        String studentPhotoUrl,
        boolean studentActive,
        Long classGroupId,
        String classGroupName,
        Modality modality,
        Instant enrolledAt,
        EnrollmentStatus status,
        Instant cancelledAt,
        Instant updatedAt
) {
}
