package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistroDispositivoDTO(
        @NotBlank String pushToken
) {}