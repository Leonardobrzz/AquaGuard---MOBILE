package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.api.v1.dto.ConfiguracaoDispositivoDTO;
import com.aqua.guard.monitoramento.core.persistence.CaixaDAguaEC;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProvisionamentoAS {

    @Autowired
    private CaixaDAguaEC caixaDAguaEC;

    public ConfiguracaoDispositivoDTO obterConfiguracao(String serialNumber) {
        var caixa = caixaDAguaEC.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new EntityNotFoundException("Dispositivo não pareado."));

        int intervaloEmSegundos = caixa.getFrequenciaAtualizacao().getMinutos() * 60;

        return new ConfiguracaoDispositivoDTO(intervaloEmSegundos);
    }

}