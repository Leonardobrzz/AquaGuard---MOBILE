package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioDTO(
        @NotBlank
        String nomeCompleto,
        @NotBlank
        @Email(message = "O formato do email é inválido.")
        String email,
        @NotBlank
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        String senha,
        @NotBlank
        @Pattern(regexp = "\\d{11,11}", message = "O telefone deve conter apenas números, incluindo o DDD.")
        String telefone
) {
}