package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.entity.LeituraVolume;
import com.aqua.guard.monitoramento.core.persistence.LeituraVolumeEC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeituraAS {

    @Autowired
    private LeituraVolumeEC leituraVolumeEC;

    @Lazy
    @Autowired
    private CaixaDAguaAS caixaDAguaAS;

    @Autowired
    private PushNotificationAS pushNotificationAS;

    @Autowired
    private EmailAS emailAS;

    @Transactional
    public void salvarLeitura(CaixaDAgua caixaAutenticada, BigDecimal volumeLitros) {
        if (volumeLitros.compareTo(caixaAutenticada.getCapacidade()) > 0) {
            throw new IllegalArgumentException("Volume recebido (" + volumeLitros + ") é maior que a capacidade da caixa (" + caixaAutenticada.getCapacidade() + ").");
        }

        var novaLeitura = new LeituraVolume(caixaAutenticada, volumeLitros);
        leituraVolumeEC.save(novaLeitura);

        verificarNivelEAlertar(caixaAutenticada, volumeLitros);
    }

    @Transactional
    public void excluirTodasPorCaixas(List<CaixaDAgua> caixas) {
        List<LeituraVolume> leiturasParaExcluir = caixas.stream()
                .flatMap(caixa -> caixa.getLeituras().stream())
                .toList();

        if (!leiturasParaExcluir.isEmpty()) {
            leituraVolumeEC.deleteAllInBatch(leiturasParaExcluir);
        }
    }

    private void verificarNivelEAlertar(CaixaDAgua caixa, BigDecimal volumeAtual) {
        Integer limiteAlerta = caixa.getLimiteAlertaPercentual();
        BigDecimal capacidade = caixa.getCapacidade();

        if (capacidade == null || capacidade.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal percentualAtual = volumeAtual.divide(capacidade, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        if (percentualAtual.compareTo(new BigDecimal(limiteAlerta)) < 0) {
            boolean podeEnviarAlerta;
            LocalDateTime ultimoAlerta = caixa.getDataUltimoAlertaNivelBaixo();

            if (ultimoAlerta == null) {
                podeEnviarAlerta = true;
            } else {
                long horasDesdeUltimoAlerta = ChronoUnit.HOURS.between(ultimoAlerta, LocalDateTime.now());
                podeEnviarAlerta = horasDesdeUltimoAlerta >= 6;
            }

            if (podeEnviarAlerta) {
                String titulo = "Alerta de Nível Baixo!";
                String corpoPush = String.format("Atenção! O nível da sua %s está em %.0f%%.", caixa.getNome(), percentualAtual);

                pushNotificationAS.enviarNotificacao(caixa.getUsuario(), titulo, corpoPush);
                emailAS.enviarAlertaNivelBaixo(caixa.getUsuario(), caixa.getNome(), percentualAtual);

                caixaDAguaAS.registrarEnvioAlertaNivelBaixo(caixa);
            }
        }
    }

}