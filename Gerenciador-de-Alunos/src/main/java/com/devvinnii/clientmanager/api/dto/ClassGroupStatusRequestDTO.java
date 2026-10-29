package com.devvinnii.clientmanager.api.dto;

import jakarta.validation.constraints.NotNull;

public record ClassGroupStatusRequestDTO(
        @NotNull(message = "Status é obrigatório") Boolean active
) {
}
