package com.devvinnii.clientmanager.api.dto;

public record ClientResponseDTO(
        Long id,
        String name,
        String email,
        String cpf,
        String phone,
        String photoUrl
) {
}
