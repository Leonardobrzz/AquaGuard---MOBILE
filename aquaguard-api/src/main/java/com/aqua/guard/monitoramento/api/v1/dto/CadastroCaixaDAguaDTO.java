package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CadastroCaixaDAguaDTO(
        @NotBlank
        String nome,
        @NotBlank(message = "O ID do dispositivo é obrigatório.")
        String idDispositivo,
        @NotNull
        @Positive
        BigDecimal capacidade,
        @Positive
        BigDecimal metaDiaria,
        @Positive
        BigDecimal metaSemanal,
        @Positive
        BigDecimal metaMensal
) {
}
