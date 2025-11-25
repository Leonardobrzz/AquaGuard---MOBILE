package com.aqua.guard.monitoramento.api.v1.ws;

import com.aqua.guard.monitoramento.api.v1.dto.*;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.aqua.guard.monitoramento.core.service.TokenAS;
import com.aqua.guard.monitoramento.core.service.UsuarioAS;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthWS {

    @Autowired
    private UsuarioAS usuarioAS;

    @Autowired
    private AuthenticationManager manager;
    

    @Autowired
    private TokenAS tokenAS;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<DetalhamentoUsuarioDTO> register(
            @RequestBody @Valid CadastroUsuarioDTO dados,
            UriComponentsBuilder uriBuilder
    ) {
        Usuario novoUsuario = usuarioAS.registrarNovoUsuario(dados);
        var uri = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(novoUsuario.getId()).toUri();

        return ResponseEntity.created(uri).body(new DetalhamentoUsuarioDTO(novoUsuario));
    }

    @PostMapping("/resend-code")
    @Transactional
    public ResponseEntity<Void> reenviarCodigo(@RequestBody @Valid ReenvioCodigoDTO dados) {
        usuarioAS.reenviarCodigoDeVerificacao(dados.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<Void> verificarCodigo(@RequestBody @Valid VerificacaoDTO dados) {
        usuarioAS.verificarCodigo(dados.email(), dados.codigo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<JWTTokenDTO> login(@RequestBody @Valid AutenticacaoDTO dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);
        var tokenJWT = tokenAS.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new JWTTokenDTO(tokenJWT));
    }

    @PostMapping("/solicitar-reset-senha")
    @Transactional
    public ResponseEntity<Void> solicitarResetSenha(@RequestBody @Valid SolicitacaoResetSenhaDTO dados) {
        usuarioAS.solicitarResetDeSenha(dados.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-senha")
    @Transactional
    public ResponseEntity<Void> resetarSenha(@RequestBody @Valid ResetSenhaDTO dados) {
        usuarioAS.resetarSenha(dados.email(), dados.codigo(), dados.novaSenha());
        return ResponseEntity.ok().build();
    }
}