package com.devvinnii.clientmanager.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.devvinnii.clientmanager.api.validation.ValidCpf;
import lombok.Data;

@Data
public class StudentRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    @ValidCpf
    private String cpf;

    @Pattern(regexp = "\\d{10,11}", message = "Telefone inválido")
    private String phone;
}
