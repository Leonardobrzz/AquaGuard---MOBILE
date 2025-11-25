package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PareamentoDispositivoDTO(
        @NotBlank String nomeCaixa,
        @NotNull @Positive BigDecimal capacidade,
        @NotBlank String serialNumberDispositivo
) {}