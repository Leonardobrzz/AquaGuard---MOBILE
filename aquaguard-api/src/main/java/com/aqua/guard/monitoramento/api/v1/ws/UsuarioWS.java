package com.aqua.guard.monitoramento.api.v1.ws;

import com.aqua.guard.monitoramento.api.v1.dto.*;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.service.DispositivoUsuarioAS;
import com.aqua.guard.monitoramento.core.service.UsuarioAS;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UsuarioWS {

    @Autowired
    private UsuarioAS usuarioAS;

    @Autowired
    private DispositivoUsuarioAS dispositivoUsuarioAS;

    @GetMapping("/me")
    public ResponseEntity<DetalhamentoUsuarioDTO> getMyProfile(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        var dadosUsuario = new DetalhamentoUsuarioDTO(usuarioAutenticado);
        return ResponseEntity.ok(dadosUsuario);
    }

    @PutMapping("/me/profile")
    @Transactional
    public ResponseEntity<DetalhamentoUsuarioDTO> atualizarPerfil(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody @Valid AtualizacaoPerfilDTO dados) {
        var usuarioAtualizado = usuarioAS.atualizarPerfil(usuarioAutenticado, dados);
        return ResponseEntity.ok(new DetalhamentoUsuarioDTO(usuarioAtualizado));
    }

    @PutMapping("/me/password")
    @Transactional
    public ResponseEntity<Void> alterarSenha(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody @Valid AlteracaoSenhaDTO dados) {
        usuarioAS.alterarSenha(usuarioAutenticado, dados);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/change-email")
    @Transactional
    public ResponseEntity<Void> iniciarAlteracaoDeEmail(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody @Valid AlteracaoEmailDTO dados) {
        usuarioAS.iniciarTrocaDeEmail(usuarioAutenticado, dados);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/me/verify-email-change")
    @Transactional
    public ResponseEntity<Void> verificarTrocaDeEmail(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody @Valid VerificacaoTrocaEmailDTO dados) {

        usuarioAS.verificarEConfirmarTrocaDeEmail(usuarioAutenticado, dados.codigo());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<Void> excluirMinhaConta(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        usuarioAS.excluirConta(usuarioAutenticado);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/devices")
    @Transactional
    public ResponseEntity<Void> registrarDispositivo(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody @Valid RegistroDispositivoDTO dados) {

        dispositivoUsuarioAS.registrarDispositivo(usuarioAutenticado, dados.pushToken());
        return ResponseEntity.ok().build();
    }

}