package com.devvinnii.clientmanager.api.dto;

import com.devvinnii.clientmanager.api.model.Weekday;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ClassScheduleRequestDTO(
        @NotNull(message = "Dia da semana é obrigatório") Weekday dayOfWeek,
        @NotNull(message = "Horário inicial é obrigatório") LocalTime startTime,
        @NotNull(message = "Horário final é obrigatório") LocalTime endTime
) {
}
