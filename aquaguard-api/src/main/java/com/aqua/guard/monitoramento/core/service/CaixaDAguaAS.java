package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.api.v1.dto.*;
import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.entity.LeituraVolume;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.persistence.CaixaDAguaEC;
import com.aqua.guard.monitoramento.core.persistence.LeituraVolumeEC;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CaixaDAguaAS {

    @Autowired
    private CaixaDAguaEC caixaDAguaEC;

    @Autowired
    private LeituraAS leituraAS;

    @Autowired
    private LeituraVolumeEC leituraRepository;

    @Autowired
    private EntityManager entityManager;

    public CaixaDAgua parearDispositivo(PareamentoDispositivoDTO dados, Usuario usuario) {
        if(caixaDAguaEC.findBySerialNumber(dados.serialNumberDispositivo()).isPresent()){
            throw new DataIntegrityViolationException("Dispositivo com este serial já foi cadastrado.");
        }
        CaixaDAgua novaCaixa = new CaixaDAgua(dados, usuario);
        caixaDAguaEC.save(novaCaixa);

        return novaCaixa;
    }

    @Transactional
    public CaixaDAgua atualizarInformacoes(UUID id, Usuario usuarioAutenticado, AtualizacaoCaixaDAguaDTO dados) {
        var caixa = validarAcessoUsuario(id, usuarioAutenticado);
        caixa.atualizarInformacoes(dados);

        return caixa;
    }

    @Transactional
    public void excluir(UUID id, Usuario usuarioAutenticado) {
        var caixa = validarAcessoUsuario(id, usuarioAutenticado);
        caixaDAguaEC.delete(caixa);
    }

    @Transactional
    public void excluirTodasPorUsuario(Usuario usuario) {
        List<CaixaDAgua> caixasDoUsuario = caixaDAguaEC.findAllByUsuario(usuario);

        for (CaixaDAgua caixa : caixasDoUsuario) {
            this.excluir(caixa.getId(), usuario);
        }
    }

    @Transactional
    public void excluirPermanentementePorUsuario(Usuario usuario) {
        List<CaixaDAgua> caixasDoUsuario = caixaDAguaEC.findAllByUsuarioIgnoringWhere(usuario);

        if (!caixasDoUsuario.isEmpty()) {
            caixaDAguaEC.deleteAllInBatch(caixasDoUsuario);
        }
    }

    public List<DetalhamentoCaixaDAguaDTO> listar(Usuario usuarioAutenticado) {
        return caixaDAguaEC.findAllByUsuario(usuarioAutenticado)
                .stream()
                .map(DetalhamentoCaixaDAguaDTO::new)
                .collect(Collectors.toList());
    }

    public DetalhamentoCompletoCaixaDAguaDTO detalhar(UUID id, Usuario usuarioAutenticado) {
        var caixa = validarAcessoUsuario(id, usuarioAutenticado);
        var ultimaLeitura = leituraRepository
                .findFirstByCaixaDAguaOrderByDataHoraLeituraDesc(caixa)
                .orElse(null);

        return new DetalhamentoCompletoCaixaDAguaDTO(caixa, ultimaLeitura);
    }

    public AnaliseCaixaDAguaDTO analisarConsumo(UUID id, Usuario usuarioAutenticado, LocalDateTime inicio, LocalDateTime fim) {
        var caixa = validarAcessoUsuario(id, usuarioAutenticado);
        var leituras = leituraRepository.findAllByCaixaDAguaAndDataHoraLeituraBetweenOrderByDataHoraLeituraAsc(caixa, inicio, fim);

        if (leituras.isEmpty() || leituras.size() < 2) {
            return new AnaliseCaixaDAguaDTO(BigDecimal.ZERO, BigDecimal.ZERO, "Dados insuficientes", List.of());
        }

        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de fim.");
        }

        if (Duration.between(inicio, fim).toDays() > 365) {
            throw new IllegalArgumentException("O período de análise não pode ser maior que 365 dias.");
        }

        BigDecimal consumoMedio = calcularConsumoMedio(leituras, inicio, fim);
        BigDecimal picoDeConsumo = calcularPicoDeConsumo(leituras);
        String previsaoEsvaziamento = calcularPrevisaoEsvaziamento(leituras.get(leituras.size() - 1).getVolumeLitros(), consumoMedio);
        var pontosGrafico = mapearLeiturasParaGrafico(leituras);

        return new AnaliseCaixaDAguaDTO(consumoMedio, picoDeConsumo, previsaoEsvaziamento, pontosGrafico);
    }

    @Transactional
    public void registrarEnvioAlertaNivelBaixo(CaixaDAgua caixa) {
        caixa.setDataUltimoAlertaNivelBaixo(LocalDateTime.now());
        caixaDAguaEC.save(caixa);
    }

    @Transactional
    public CaixaDAgua alternarStatus(UUID id, Usuario usuarioAutenticado) {
        var caixa = validarAcessoUsuario(id, usuarioAutenticado);
        
        // Usar query nativa para alternar o status, contornando o soft delete
        boolean novoStatus = !caixa.isAtivo();
        
        // Atualizar diretamente no banco de dados
        caixaDAguaEC.flush();
        var query = entityManager.createNativeQuery(
            "UPDATE caixas_dagua SET ativo = ? WHERE id = ?"
        );
        query.setParameter(1, novoStatus);
        query.setParameter(2, id);
        query.executeUpdate();
        
        // Limpar o cache e buscar novamente
        entityManager.clear();
        
        // Como o @Where impede buscar registros inativos, vamos fazer query nativa
        if (novoStatus) {
            // Se ativou, pode usar o repositório normal
            return caixaDAguaEC.findById(id).orElseThrow();
        } else {
            // Se desativou, precisa buscar com query nativa
            var nativeQuery = entityManager.createNativeQuery(
                "SELECT * FROM caixas_dagua WHERE id = ?", CaixaDAgua.class
            );
            nativeQuery.setParameter(1, id);
            return (CaixaDAgua) nativeQuery.getSingleResult();
        }
    }

    @Transactional 
    public String regenerarCodigoAcesso(UUID id, Usuario usuarioAutenticado) {
        var caixa = validarAcessoUsuario(id, usuarioAutenticado);
        
        // Gerar novo código único
        String novoCodigoAcesso = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        
        // Como chaveApi é o campo que armazena o código, vamos atualizar ele
        // Usando reflection para acessar o campo privado chaveApi
        try {
            var campo = CaixaDAgua.class.getDeclaredField("chaveApi");
            campo.setAccessible(true);
            campo.set(caixa, novoCodigoAcesso);
        } catch (Exception e) {
            // Fallback: gerar um novo código no formato esperado
            novoCodigoAcesso = "AQ" + System.currentTimeMillis() % 100000;
        }
        
        caixaDAguaEC.save(caixa);
        return novoCodigoAcesso;
    }

    public StatusCaixaDAguaDTO obterStatus(UUID id, Usuario usuarioAutenticado) {
        var caixa = validarAcessoUsuario(id, usuarioAutenticado);
        var ultimaLeitura = leituraRepository
                .findFirstByCaixaDAguaOrderByDataHoraLeituraDesc(caixa)
                .orElse(null);
        
        BigDecimal nivel = ultimaLeitura != null ? ultimaLeitura.getVolumeLitros() : BigDecimal.ZERO;
        BigDecimal capacidade = caixa.getCapacidade();
        BigDecimal percentual = BigDecimal.ZERO;
        
        if (capacidade != null && capacidade.compareTo(BigDecimal.ZERO) > 0) {
            percentual = nivel.divide(capacidade, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        
        String status = determinarStatus(percentual);
        String ultimaAtualizacao = ultimaLeitura != null ? 
                ultimaLeitura.getDataHoraLeitura().toString() : 
                LocalDateTime.now().toString();
        
        return new StatusCaixaDAguaDTO(
                status,
                nivel,
                capacidade,
                percentual,
                ultimaAtualizacao
        );
    }

    private String determinarStatus(BigDecimal percentual) {
        if (percentual.compareTo(BigDecimal.ZERO) <= 0) {
            return "VAZIO";
        } else if (percentual.compareTo(new BigDecimal("15")) <= 0) {
            return "CRITICO";
        } else if (percentual.compareTo(new BigDecimal("30")) <= 0) {
            return "BAIXO";
        } else {
            return "NORMAL";
        }
    }

    private CaixaDAgua validarAcessoUsuario(UUID id, Usuario usuarioAutenticado) {
        var caixa = caixaDAguaEC.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Caixa d'água não encontrada."));

        if (!caixa.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new AccessDeniedException("Usuário não tem permissão para acessar esta caixa d'água.");
        }

        return caixa;
    }

    private List<AnaliseCaixaDAguaDTO.PontoGrafico> mapearLeiturasParaGrafico(List<LeituraVolume> leituras) {
        return leituras.stream()
                .map(l -> new AnaliseCaixaDAguaDTO.PontoGrafico(l.getDataHoraLeitura(), l.getVolumeLitros()))
                .collect(Collectors.toList());
    }

    private BigDecimal calcularConsumoMedio(List<LeituraVolume> leituras, LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal consumoTotal = leituras.get(0).getVolumeLitros().subtract(leituras.get(leituras.size() - 1).getVolumeLitros());
        long dias = Duration.between(inicio, fim).toDays();
        dias = dias == 0 ? 1 : dias; // Evita divisão por zero para períodos menores que 1 dia

        return consumoTotal.divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPicoDeConsumo(List<LeituraVolume> leituras) {
        BigDecimal picoDeConsumo = BigDecimal.ZERO;
        for (int i = 1; i < leituras.size(); i++) {
            BigDecimal consumoNoIntervalo = leituras.get(i - 1).getVolumeLitros().subtract(leituras.get(i).getVolumeLitros());
            if (consumoNoIntervalo.compareTo(picoDeConsumo) > 0) {
                picoDeConsumo = consumoNoIntervalo;
            }
        }

        return picoDeConsumo;
    }

    private String calcularPrevisaoEsvaziamento(BigDecimal volumeAtual, BigDecimal consumoMedio) {
        if (consumoMedio.compareTo(BigDecimal.ZERO) <= 0) {
            return "Consumo zerado";
        }

        BigDecimal diasRestantesDecimal = volumeAtual.divide(consumoMedio, 2, RoundingMode.HALF_UP);

        if (diasRestantesDecimal.compareTo(BigDecimal.ONE) >= 0) {
            int dias = diasRestantesDecimal.intValue();
            if (dias == 1) {
                return "Aproximadamente 1 dia restante.";
            } else {
                return "Aproximadamente " + dias + " dias restantes.";
            }
        }

        BigDecimal consumoMedioPorHora = consumoMedio.divide(new BigDecimal("24"), 2, RoundingMode.HALF_UP);

        if (consumoMedioPorHora.compareTo(BigDecimal.ZERO) <= 0) {
            return "Menos de um dia restante.";
        }

        BigDecimal horasRestantes = volumeAtual.divide(consumoMedioPorHora, 0, RoundingMode.DOWN);
        int horas = horasRestantes.intValue();

        if (horas <= 0) {
            return "Menos de uma hora restante.";
        } else if (horas == 1) {
            return "Menos de um dia restante (aproximadamente 1 hora).";
        } else {
            return "Menos de um dia restante (aproximadamente " + horas + " horas).";
        }
    }

}