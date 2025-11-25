package com.aqua.guard.monitoramento.api.v1.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnaliseCaixaDAguaDTO(
        BigDecimal consumoMedio,
        BigDecimal picoDeConsumo,
        String previsaoEsvaziamento,
        List<PontoGrafico> pontosDoGrafico
) {

    public record PontoGrafico(
            java.time.LocalDateTime data,
            java.math.BigDecimal volume
    ) {}
}