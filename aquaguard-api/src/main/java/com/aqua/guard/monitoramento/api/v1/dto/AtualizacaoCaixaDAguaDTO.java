package com.aqua.guard.monitoramento.api.v1.dto;

import com.aqua.guard.monitoramento.core.enums.FrequenciaAtualizacao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AtualizacaoCaixaDAguaDTO(
        String nome,
        @Positive
        BigDecimal capacidade,
        @Positive
        BigDecimal metaDiaria,
        @Positive
        BigDecimal metaSemanal,
        @Positive
        BigDecimal metaMensal,
        FrequenciaAtualizacao frequenciaAtualizacao,
        @Positive(message = "O limite do alerta deve ser um número positivo.")
        @Max(value = 100, message = "O limite do alerta não pode ser maior que 100.")
        Integer limiteAlertaPercentual
) {}