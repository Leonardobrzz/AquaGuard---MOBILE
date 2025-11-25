package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlteracaoSenhaDTO(
        @NotBlank
        String senhaAtual,
        @NotBlank
        @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres.")
        String novaSenha
) {}