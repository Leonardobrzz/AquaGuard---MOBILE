package com.aqua.guard.monitoramento.api.v1.ws;

import com.aqua.guard.monitoramento.api.v1.dto.*;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.service.CaixaDAguaAS;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/caixas-dagua")
public class CaixaDAguaWS {

    @Autowired
    private CaixaDAguaAS caixaDAguaAS;

    @PostMapping("/parear-dispositivo")
    @Transactional
    public ResponseEntity<DetalhamentoCaixaDAguaDTO> parear(
            @RequestBody @Valid PareamentoDispositivoDTO dados,
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            UriComponentsBuilder uriBuilder
    ) {
        var novaCaixa = caixaDAguaAS.parearDispositivo(dados, usuarioAutenticado);
        var uri = uriBuilder.path("/caixas-dagua/{id}").buildAndExpand(novaCaixa.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetalhamentoCaixaDAguaDTO(novaCaixa));
    }

    @GetMapping("/gerar-serial-teste")
    public ResponseEntity<SerialTesteDTO> gerarSerialTeste() {
        String serialGerado = "TEST" + System.currentTimeMillis();
        return ResponseEntity.ok(new SerialTesteDTO(serialGerado, 
            "Serial de teste gerado para desenvolvimento. Use este número no campo 'Número de Série' ao adicionar uma nova caixa."));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<DetalhamentoCaixaDAguaDTO> atualizar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody @Valid AtualizacaoCaixaDAguaDTO dados
    ) {
        try {
            var caixaAtualizada = caixaDAguaAS.atualizarInformacoes(id, usuarioAutenticado, dados);
            return ResponseEntity.ok(new DetalhamentoCaixaDAguaDTO(caixaAtualizada));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        try {
            caixaDAguaAS.excluir(id, usuarioAutenticado);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<DetalhamentoCaixaDAguaDTO>> listar(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        var listaDto = caixaDAguaAS.listar(usuarioAutenticado);
        return ResponseEntity.ok(listaDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhamentoCompletoCaixaDAguaDTO> detalhar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        try {
            var dtoCompleto = caixaDAguaAS.detalhar(id, usuarioAutenticado);
            return ResponseEntity.ok(dtoCompleto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/analise")
    public ResponseEntity<AnaliseCaixaDAguaDTO> analisar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim
    ) {
        try {
            var dadosAnalise = caixaDAguaAS.analisarConsumo(id, usuarioAutenticado, inicio, fim);
            return ResponseEntity.ok(dadosAnalise);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @Transactional
    public ResponseEntity<DetalhamentoCaixaDAguaDTO> alternarStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        try {
            var caixaAtualizada = caixaDAguaAS.alternarStatus(id, usuarioAutenticado);
            return ResponseEntity.ok(new DetalhamentoCaixaDAguaDTO(caixaAtualizada));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/{id}/regenerar-codigo")
    @Transactional
    public ResponseEntity<CodigoAcessoDTO> regenerarCodigoAcesso(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        try {
            var novoCodigo = caixaDAguaAS.regenerarCodigoAcesso(id, usuarioAutenticado);
            return ResponseEntity.ok(new CodigoAcessoDTO(novoCodigo));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<StatusCaixaDAguaDTO> obterStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        try {
            var status = caixaDAguaAS.obterStatus(id, usuarioAutenticado);
            return ResponseEntity.ok(status);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}