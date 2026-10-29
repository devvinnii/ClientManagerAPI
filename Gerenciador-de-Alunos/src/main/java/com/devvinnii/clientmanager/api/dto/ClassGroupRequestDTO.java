package com.devvinnii.clientmanager.api.dto;

import com.devvinnii.clientmanager.api.model.Modality;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ClassGroupRequestDTO(
        @NotBlank(message = "Nome da turma é obrigatório")
        @Size(max = 150, message = "Nome deve possuir no máximo 150 caracteres") String name,
        @NotNull(message = "Modalidade é obrigatória") Modality modality,
        @Size(max = 2000, message = "Descrição deve possuir no máximo 2000 caracteres") String description,
        @NotBlank(message = "Professor ou responsável é obrigatório")
        @Size(max = 150, message = "Responsável deve possuir no máximo 150 caracteres") String responsible,
        @Min(value = 1, message = "Capacidade máxima deve ser maior que zero") int maxCapacity,
        @NotEmpty(message = "Informe ao menos um horário") List<@Valid ClassScheduleRequestDTO> schedules
) {
}
