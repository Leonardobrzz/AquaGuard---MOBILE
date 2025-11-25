package com.aqua.guard.monitoramento.api.v1.dto;

import java.math.BigDecimal;

public record StatusCaixaDAguaDTO(
        String status,
        BigDecimal nivel,
        BigDecimal capacidade,
        BigDecimal percentual,
        String ultimaAtualizacao
) {}
