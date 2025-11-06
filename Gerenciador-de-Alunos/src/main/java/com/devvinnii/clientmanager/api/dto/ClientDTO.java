package com.devvinnii.clientmanager.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClientDTO {

    @NotBlank
    private String name;

    @Email
    private String email;

    @Pattern(regexp = "\\d{11}")
    private String cpf;

    @Pattern(regexp = "\\d{10,11}")
    private String phone;
}
