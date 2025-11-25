package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.api.v1.dto.*;
import com.aqua.guard.monitoramento.core.entity.CaixaDAgua;
import com.aqua.guard.monitoramento.core.persistence.UsuarioEC;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UsuarioAS {

    @Autowired
    private UsuarioEC repository;

    @Autowired
    private CaixaDAguaAS caixaDAguaAS;

    @Autowired
    private EmailAS emailAS;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final SecureRandom random = new SecureRandom();

    public Usuario registrarNovoUsuario(CadastroUsuarioDTO dados) {
        Optional<Usuario> usuarioOpt  = repository.findByEmailForVerification(dados.email());

        Usuario usuarioParaSalvar;
        String senhaHasheada = passwordEncoder.encode(dados.senha());

        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();
            if (usuarioExistente.isAtivo()) {
                throw new IllegalStateException("O email fornecido já está em uso.");
            }

            caixaDAguaAS.excluirPermanentementePorUsuario(usuarioExistente);
            usuarioExistente.atualizarDadosDeNovoRegistro(dados, senhaHasheada);
            usuarioParaSalvar = usuarioExistente;
        } else {
            usuarioParaSalvar = new Usuario(dados, senhaHasheada);
        }

        String codigo = String.format("%04d", random.nextInt(9999));
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(10);

        usuarioParaSalvar.definirCodigoDeVerificacao(codigo, dataExpiracao);

        // --- MUDANÇA TEMPORARIA - P/ TESTES ---
        usuarioParaSalvar.ativarConta(); // Apagar linha após testes!
        // emailAS.sendVerificationCode(usuarioParaSalvar.getEmail(), codigo); // Descomentar linha após testes!
        // --------------------------------------

        return repository.save(usuarioParaSalvar);
    }

    public void reenviarCodigoDeVerificacao(String email) {
        Usuario usuario = repository.findByEmailForVerification(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (usuario.isAtivo()) {
            throw new IllegalStateException("Esta conta já foi ativada.");
        }

        String novoCodigo = String.format("%04d", random.nextInt(9999));
        LocalDateTime novaDataExpiracao = LocalDateTime.now().plusMinutes(10); // Expira em 10 minutos

        usuario.definirCodigoDeVerificacao(novoCodigo, novaDataExpiracao);

        repository.save(usuario);
        emailAS.sendVerificationCode(usuario.getEmail(), novoCodigo);
    }

    public void verificarCodigo(String email, String codigo) {
        Usuario usuario = repository.findByEmailForVerification(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (usuario.isAtivo()) {
            throw new IllegalStateException("Este usuário já foi verificado.");
        }

        if (usuario.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Código de verificação expirado.");
        }

        if (!usuario.getVerificationCode().equals(codigo)) {
            throw new IllegalArgumentException("Código de verificação inválido.");
        }

        usuario.ativarConta();
    }

    public void excluirConta(Usuario usuario) {
        caixaDAguaAS.excluirTodasPorUsuario(usuario);
        repository.delete(usuario);
    }

    @Transactional
    public Usuario atualizarPerfil(Usuario usuario, AtualizacaoPerfilDTO dados) {
        usuario.atualizarPerfil(dados);
        return repository.save(usuario);
    }

    @Transactional
    public void alterarSenha(Usuario usuario, AlteracaoSenhaDTO dados) {
        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getPassword())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        usuario.alterarSenha(passwordEncoder.encode(dados.novaSenha()));
        repository.save(usuario);
    }

    @Transactional
    public void iniciarTrocaDeEmail(Usuario usuario, AlteracaoEmailDTO dados) {
        if (dados.novoEmail().equalsIgnoreCase(usuario.getEmail())) {
            throw new IllegalArgumentException("O novo e-mail deve ser diferente do atual.");
        }
        if (repository.findByEmail(dados.novoEmail()).isPresent()) {
            throw new IllegalStateException("O novo e-mail fornecido já está em uso por outra conta.");
        }

        String codigo = String.format("%04d", random.nextInt(9999));
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(10);

        usuario.definirCodigoDeMudancaDeEmail(dados.novoEmail(), codigo, dataExpiracao);
        repository.save(usuario);
        emailAS.sendVerificationCode(dados.novoEmail(), codigo);
    }

    public void verificarEConfirmarTrocaDeEmail(Usuario usuario, String codigo) {
        if (usuario.getEmailChangeCode() == null || usuario.getPendingEmail() == null) {
            throw new IllegalStateException("Nenhuma mudança de e-mail pendente.");
        }
        if (usuario.getEmailChangeCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Código de verificação para troca de e-mail expirado.");
        }
        if (!usuario.getEmailChangeCode().equals(codigo)) {
            throw new IllegalArgumentException("Código de verificação inválido.");
        }

        usuario.confirmarMudancaDeEmail();
        repository.save(usuario);
    }

    @Transactional
    public void solicitarResetDeSenha(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com o email fornecido."));

        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Esta conta não foi ativada. Verifique seu email para ativar a conta.");
        }

        // Gerar código de reset de 4 dígitos (consistente com o resto do projeto)
        String codigoReset = String.format("%04d", random.nextInt(9999));
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(15); // Token expira em 15 minutos

        usuario.definirTokenDeResetDeSenha(codigoReset, dataExpiracao);
        repository.save(usuario);

        // Enviar email com código
        emailAS.sendPasswordResetCode(usuario.getEmail(), codigoReset);
    }

    @Transactional
    public void resetarSenha(String email, String codigo, String novaSenha) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com o email fornecido."));

        if (!usuario.isTokenDeResetValido(codigo)) {
            throw new IllegalArgumentException("Código de reset inválido ou expirado.");
        }

        String novaSenhaHasheada = passwordEncoder.encode(novaSenha);
        usuario.resetarSenha(novaSenhaHasheada);
        repository.save(usuario);
    }

}