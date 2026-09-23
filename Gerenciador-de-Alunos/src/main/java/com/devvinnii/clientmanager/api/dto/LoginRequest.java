package com.devvinnii.clientmanager.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Usuário é obrigatório") String username,
        @NotBlank(message = "Senha é obrigatória") String password
) {
}
