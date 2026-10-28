package com.devvinnii.clientmanager.api.dto;

import jakarta.validation.constraints.NotNull;

public record StudentStatusRequestDTO(@NotNull(message = "Status ativo é obrigatório") Boolean active) {
}
