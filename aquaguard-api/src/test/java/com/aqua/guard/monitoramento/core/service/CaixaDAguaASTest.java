package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.api.v1.dto.AnaliseCaixaDAguaDTO;
import com.aqua.guard.monitoramento.api.v1.dto.PareamentoDispositivoDTO;
import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.entity.LeituraVolume;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.persistence.CaixaDAguaEC;
import com.aqua.guard.monitoramento.core.persistence.LeituraVolumeEC;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários para CaixaDAguaAS - Análise de Consumo")
class CaixaDAguaASTest {

    @Mock
    private CaixaDAguaEC caixaDAguaEC;

    @Mock
    private LeituraVolumeEC leituraRepository;

    @InjectMocks
    private CaixaDAguaAS caixaDAguaAS;

    private Usuario usuarioAutenticado;
    private CaixaDAgua caixaDAgua;
    private UUID idCaixa;
    private UUID idUsuario;

    @BeforeEach
    void setUp() {
        idUsuario = UUID.randomUUID();
        idCaixa = UUID.randomUUID();

        usuarioAutenticado = new Usuario();
        ReflectionTestUtils.setField(usuarioAutenticado, "id", idUsuario);

        PareamentoDispositivoDTO dadosPareamento = new PareamentoDispositivoDTO(
                "Caixa de Teste", new BigDecimal("1000"), "SERIAL123"
        );
        caixaDAgua = new CaixaDAgua(dadosPareamento, usuarioAutenticado);
        ReflectionTestUtils.setField(caixaDAgua, "id", idCaixa);

    }

    @Test
    void deveRetornarDadosDeAnaliseCorretosParaPeriodoValido() {
        LocalDateTime inicio = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 7, 3, 0, 0); // 2 dias de duração

        List<LeituraVolume> leituras = List.of(
                new LeituraVolume(1L, caixaDAgua, new BigDecimal("1000.00"), inicio),
                new LeituraVolume(2L, caixaDAgua, new BigDecimal("900.00"), inicio.plusDays(1)), // Consumo de 100L
                new LeituraVolume(3L, caixaDAgua, new BigDecimal("850.00"), fim) // Consumo de 50L
        );

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(caixaDAgua, inicio, fim))
                .thenReturn(leituras);

        AnaliseCaixaDAguaDTO resultado = caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("75.00"), resultado.consumoMedio());
        assertEquals(new BigDecimal("100.00"), resultado.picoDeConsumo());
        assertEquals("Aproximadamente 11 dias restantes.", resultado.previsaoEsvaziamento());
        assertEquals(3, resultado.pontosDoGrafico().size());
        assertEquals(new BigDecimal("1000.00"), resultado.pontosDoGrafico().get(0).volume());
    }

    @Test
    void deveRetornarPrevisaoEmDiaSingular() {
        // Volume atual: 100L. Consumo total em 2 dias: 200L. Média diária: 100L/dia.
        // Previsão: 100L / 100L/dia = 1 dia.
        LocalDateTime inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fim = inicio.plusDays(2);

        List<LeituraVolume> leituras = List.of(
                new LeituraVolume(1L, caixaDAgua, new BigDecimal("300.00"), inicio),
                new LeituraVolume(2L, caixaDAgua, new BigDecimal("100.00"), fim)
        );

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(caixaDAgua, inicio, fim))
                .thenReturn(leituras);

        AnaliseCaixaDAguaDTO resultado = caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);

        assertEquals("Aproximadamente 1 dia restante.", resultado.previsaoEsvaziamento());
    }

    @Test
    void deveRetornarPrevisaoEmHoras() {
        // Volume atual: 50L. Consumo total em 1 dia (24h): 120L. Média diária: 120L/dia.
        // Média por hora: 120 / 24 = 5L/hora.
        // Previsão: 50L / 5L/hora = 10 horas.
        LocalDateTime inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fim = inicio.plusDays(1);

        List<LeituraVolume> leituras = List.of(
                new LeituraVolume(1L, caixaDAgua, new BigDecimal("170.00"), inicio),
                new LeituraVolume(2L, caixaDAgua, new BigDecimal("50.00"), fim)
        );

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(caixaDAgua, inicio, fim))
                .thenReturn(leituras);

        AnaliseCaixaDAguaDTO resultado = caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);

        assertEquals("Menos de um dia restante (aproximadamente 10 horas).", resultado.previsaoEsvaziamento());
    }

    @Test
    void deveRetornarPrevisaoEmHoraSingular() {
        // Volume atual: 5L. Consumo total em 1 dia (24h): 120L. Média por hora: 5L/hora.
        // Previsão: 5L / 5L/hora = 1 hora.
        LocalDateTime inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fim = inicio.plusDays(1);

        List<LeituraVolume> leituras = List.of(
                new LeituraVolume(1L, caixaDAgua, new BigDecimal("125.00"), inicio),
                new LeituraVolume(2L, caixaDAgua, new BigDecimal("5.00"), fim)
        );

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(caixaDAgua, inicio, fim))
                .thenReturn(leituras);

        AnaliseCaixaDAguaDTO resultado = caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);

        assertEquals("Menos de um dia restante (aproximadamente 1 hora).", resultado.previsaoEsvaziamento());
    }

    @Test
    void deveRetornarPrevisaoMenorQueUmaHora() {
        // Volume atual: 2L. Consumo total em 1 dia (24h): 120L. Média por hora: 5L/hora.
        // Previsão: 2L / 5L/hora = 0.4 horas, que será truncado para 0.
        LocalDateTime inicio = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime fim = inicio.plusDays(1);

        List<LeituraVolume> leituras = List.of(
                new LeituraVolume(1L, caixaDAgua, new BigDecimal("122.00"), inicio),
                new LeituraVolume(2L, caixaDAgua, new BigDecimal("2.00"), fim)
        );

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(caixaDAgua, inicio, fim))
                .thenReturn(leituras);

        AnaliseCaixaDAguaDTO resultado = caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);

        assertEquals("Menos de uma hora restante.", resultado.previsaoEsvaziamento());
    }

    @Test
    void deveLancarExcecaoQuandoCaixaNaoEncontrada() {
        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        }, "Deveria lançar EntityNotFoundException");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoTemPermissao() {
        Usuario outroUsuario = new Usuario();
        ReflectionTestUtils.setField(outroUsuario, "id", UUID.randomUUID());

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));

        assertThrows(AccessDeniedException.class, () -> {
            caixaDAguaAS.analisarConsumo(idCaixa, outroUsuario, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        }, "Deveria lançar AccessDeniedException");
    }

    @Test
    void deveRetornarDadosInsuficientesQuandoNaoHaLeituras() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = inicio.plusDays(1);
        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        AnaliseCaixaDAguaDTO resultado = caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);

        assertEquals("Dados insuficientes", resultado.previsaoEsvaziamento());
        assertEquals(BigDecimal.ZERO, resultado.consumoMedio());
        assertTrue(resultado.pontosDoGrafico().isEmpty());
    }

    @Test
    void deveCalcularPrevisaoComoConsumoZeradoQuandoNaoHaConsumo() {
        LocalDateTime inicio = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 7, 3, 0, 0);

        List<LeituraVolume> leituras = List.of(
                new LeituraVolume(1L, caixaDAgua, new BigDecimal("900.00"), inicio),
                new LeituraVolume(2L, caixaDAgua, new BigDecimal("900.00"), fim)
        );

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(caixaDAgua, inicio, fim))
                .thenReturn(leituras);

        AnaliseCaixaDAguaDTO resultado = caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), resultado.consumoMedio());
        assertEquals("Consumo zerado", resultado.previsaoEsvaziamento());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando data de início for posterior à data de fim")
    void deveLancarExcecaoQuandoDataInicioForDepoisDaDataFim() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = inicio.minusDays(1);

        when(caixaDAguaEC.findById(idCaixa)).thenReturn(Optional.of(caixaDAgua));
        when(leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(any(), any(), any()))
                .thenReturn(List.of(new LeituraVolume(), new LeituraVolume()));

        assertThrows(IllegalArgumentException.class, () -> {
            caixaDAguaAS.analisarConsumo(idCaixa, usuarioAutenticado, inicio, fim);
        });
    }

}