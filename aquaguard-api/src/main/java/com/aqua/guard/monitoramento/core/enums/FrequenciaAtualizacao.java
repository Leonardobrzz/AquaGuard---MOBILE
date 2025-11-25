package com.aqua.guard.monitoramento.core.enums;

import lombok.Getter;

@Getter
public enum FrequenciaAtualizacao {
    A_CADA_30_MINUTOS(30),
    A_CADA_1_HORA(60),
    A_CADA_5_HORAS(300),
    A_CADA_10_HORAS(600),
    A_CADA_1_DIA(1440),
    A_CADA_1_SEMANA(10080);

    private final int minutos;

    FrequenciaAtualizacao(int minutos) {
        this.minutos = minutos;
    }
}