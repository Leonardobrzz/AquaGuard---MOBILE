package com.aqua.guard.monitoramento.api.v1.dto;

import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DetalhamentoCaixaDAguaDTO(
        UUID id,
        String nome,
        BigDecimal capacidade,
        BigDecimal metaDiaria,
        BigDecimal metaSemanal,
        BigDecimal metaMensal,
        LocalDateTime criadoEm
) {

    public DetalhamentoCaixaDAguaDTO(CaixaDAgua caixa) {
        this(
                caixa.getId(),
                caixa.getNome(),
                caixa.getCapacidade(),
                caixa.getMetaDiaria(),
                caixa.getMetaSemanal(),
                caixa.getMetaMensal(),
                caixa.getCriadoEm()
        );
    }
}