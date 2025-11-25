package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitacaoResetSenhaDTO(
        @NotBlank
        @Email(message = "O formato do email é inválido.")
        String email
) {
}
