package com.devvinnii.clientmanager.api.dto;

import com.devvinnii.clientmanager.api.model.Modality;

import java.time.Instant;
import java.util.List;

public record ClassGroupResponseDTO(
        Long id,
        String name,
        Modality modality,
        String description,
        String responsible,
        int maxCapacity,
        long occupiedSpots,
        long availableSpots,
        boolean active,
        List<ClassScheduleResponseDTO> schedules,
        Instant createdAt,
        Instant updatedAt
) {
}
