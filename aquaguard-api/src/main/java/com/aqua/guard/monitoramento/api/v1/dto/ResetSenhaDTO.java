package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetSenhaDTO(
        @NotBlank
        @Email(message = "O formato do email é inválido.")
        String email,
        
        @NotBlank
        @Size(min = 4, max = 4, message = "O código deve ter exatamente 4 dígitos.")
        String codigo,
        
        @NotBlank
        @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres.")
        String novaSenha
) {
}
