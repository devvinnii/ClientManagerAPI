package com.devvinnii.clientmanager.api.dto;

import com.devvinnii.clientmanager.api.model.Weekday;

import java.time.LocalTime;

public record ClassScheduleResponseDTO(
        Long id,
        Weekday dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
