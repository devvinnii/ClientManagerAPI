package com.devvinnii.clientmanager.api.dto;

import java.util.List;

public record StudentPageResponseDTO(
        List<StudentResponseDTO> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
}
