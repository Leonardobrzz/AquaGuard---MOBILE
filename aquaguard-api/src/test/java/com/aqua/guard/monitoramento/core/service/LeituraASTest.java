package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.persistence.LeituraVolumeEC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para LeituraAS - Lógica de Alertas")
class LeituraASTest {

    @Mock
    private LeituraVolumeEC leituraVolumeEC;
    @Mock
    private PushNotificationAS pushNotificationAS;
    @Mock
    private EmailAS emailAS;
    @Mock
    private CaixaDAguaAS caixaDAguaAS;

    @InjectMocks
    private LeituraAS leituraAS;

    private CaixaDAgua caixa;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        caixa = new CaixaDAgua();
        ReflectionTestUtils.setField(caixa, "usuario", usuario);
        ReflectionTestUtils.setField(caixa, "nome", "Caixa da Frente");
        ReflectionTestUtils.setField(caixa, "capacidade", new BigDecimal("1000"));
        ReflectionTestUtils.setField(caixa, "limiteAlertaPercentual", 20);
    }

    @Test
    void deveEnviarAlertaQuandoNivelAtingirLimite() {
        // Nível atual é 19.9% (199L de 1000L), abaixo do limite de 20%
        BigDecimal volumeAbaixoDoLimite = new BigDecimal("199.00");
        ReflectionTestUtils.setField(caixa, "dataUltimoAlertaNivelBaixo", null);

        leituraAS.salvarLeitura(caixa, volumeAbaixoDoLimite);

        verify(pushNotificationAS, times(1)).enviarNotificacao(any(Usuario.class), anyString(), anyString());
        verify(emailAS, times(1)).enviarAlertaNivelBaixo(any(Usuario.class), anyString(), any(BigDecimal.class));
        verify(caixaDAguaAS, times(1)).registrarEnvioAlertaNivelBaixo(caixa);
    }

    @Test
    void naoDeveEnviarAlertaSeJaEnviadoRecentemente() {
        BigDecimal volumeAbaixoDoLimite = new BigDecimal("180.00");
        LocalDateTime ultimoAlertaRecente = LocalDateTime.now().minusHours(2);
        ReflectionTestUtils.setField(caixa, "dataUltimoAlertaNivelBaixo", ultimoAlertaRecente);

        leituraAS.salvarLeitura(caixa, volumeAbaixoDoLimite);

        verify(pushNotificationAS, never()).enviarNotificacao(any(), any(), any());
        verify(emailAS, never()).enviarAlertaNivelBaixo(any(), any(), any());
        verify(caixaDAguaAS, never()).registrarEnvioAlertaNivelBaixo(any());
    }

    @Test
    void deveEnviarNovoAlertaAposPeriodoDeEspera() {
        BigDecimal volumeAbaixoDoLimite = new BigDecimal("150.00");
        LocalDateTime ultimoAlertaAntigo = LocalDateTime.now().minusHours(8);
        ReflectionTestUtils.setField(caixa, "dataUltimoAlertaNivelBaixo", ultimoAlertaAntigo);

        leituraAS.salvarLeitura(caixa, volumeAbaixoDoLimite);

        verify(pushNotificationAS, times(1)).enviarNotificacao(any(), any(), any());
        verify(emailAS, times(1)).enviarAlertaNivelBaixo(any(), any(), any());
        verify(caixaDAguaAS, times(1)).registrarEnvioAlertaNivelBaixo(caixa);
    }

    @Test
    void naoDeveEnviarAlertaSeNivelEstiverOk() {
        // Nível atual é 25% (250L de 1000L)
        BigDecimal volumeAcimaDoLimite = new BigDecimal("250.00");

        leituraAS.salvarLeitura(caixa, volumeAcimaDoLimite);

        verify(pushNotificationAS, never()).enviarNotificacao(any(), any(), any());
        verify(emailAS, never()).enviarAlertaNivelBaixo(any(), any(), any());
        verify(caixaDAguaAS, never()).registrarEnvioAlertaNivelBaixo(any());
    }

}