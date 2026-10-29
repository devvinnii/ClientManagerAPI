package com.devvinnii.clientmanager.api.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequestDTO(
        @NotNull(message = "Aluno é obrigatório") Long studentId
) {
}
