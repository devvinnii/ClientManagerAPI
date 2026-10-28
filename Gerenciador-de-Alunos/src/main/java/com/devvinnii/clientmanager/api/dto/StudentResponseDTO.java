package com.devvinnii.clientmanager.api.dto;

public record StudentResponseDTO(
        Long id,
        String name,
        String email,
        String cpf,
        String phone,
        String photoUrl,
        boolean active
) {
}
