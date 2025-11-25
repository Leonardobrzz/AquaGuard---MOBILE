package com.aqua.guard.monitoramento.api.v1.dto;

import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.entity.LeituraVolume;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DetalhamentoCompletoCaixaDAguaDTO(
        UUID id,
        String nome,
        BigDecimal capacidade,
        BigDecimal metaDiaria,
        BigDecimal metaSemanal,
        BigDecimal metaMensal,
        BigDecimal volumeAtual,
        LocalDateTime dataUltimaLeitura
) {
    public DetalhamentoCompletoCaixaDAguaDTO(CaixaDAgua caixa, LeituraVolume ultimaLeitura) {
        this(
                caixa.getId(),
                caixa.getNome(),
                caixa.getCapacidade(),
                caixa.getMetaDiaria(),
                caixa.getMetaSemanal(),
                caixa.getMetaMensal(),
                // Lógica para lidar com o caso de ainda não haver leitura
                ultimaLeitura != null ? ultimaLeitura.getVolumeLitros() : BigDecimal.ZERO,
                ultimaLeitura != null ? ultimaLeitura.getDataHoraLeitura() : null
        );
    }
}