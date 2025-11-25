package com.aqua.guard.monitoramento.core.entity;

import com.aqua.guard.monitoramento.api.v1.dto.AtualizacaoPerfilDTO;
import com.aqua.guard.monitoramento.api.v1.dto.CadastroUsuarioDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Table(name = "usuarios")
@Entity(name = "Usuario")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@SQLDelete(sql = "UPDATE usuarios SET ativo = false WHERE id = ?")
@Where(clause = "ativo = true")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "sobrenome")
    private String sobrenome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "pending_email")
    private String pendingEmail;

    @Column(name = "email_change_code")
    private String emailChangeCode;

    @Column(name = "email_change_code_expires_at")
    private LocalDateTime emailChangeCodeExpiresAt;

    @Column(name = "senha_hash", nullable = false)
    private String senha;

    private String telefone;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    @Column(name = "notificacoes_email_ativas")
    private boolean notificacoesEmailAtivas = true;

    @Column(name = "notificacoes_push_ativas")
    private boolean notificacoesPushAtivas = true;

    private boolean ativo = false;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaixaDAgua> caixasDAgua;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DispositivoUsuario> dispositivos;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public String getNomeCompleto() {
        return this.nome + (this.sobrenome != null ? " " + this.sobrenome : "");
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.ativo;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.ativo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.ativo;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo;
    }

    public Usuario(CadastroUsuarioDTO dados, String senhaHasheada) {
        this.dividirEDefinirNomeCompleto(dados.nomeCompleto());
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.senha = senhaHasheada;
        this.caixasDAgua = new ArrayList<>();
    }

    public void atualizarDadosDeNovoRegistro(CadastroUsuarioDTO dados, String novaSenhaHasheada) {
        this.dividirEDefinirNomeCompleto(dados.nomeCompleto());
        this.telefone = dados.telefone();
        this.senha = novaSenhaHasheada;
        this.ativo = false;

        this.pendingEmail = null;
        this.emailChangeCode = null;
        this.emailChangeCodeExpiresAt = null;
        this.verificationCode = null;
        this.verificationCodeExpiresAt = null;
    }

    public void atualizarPerfil(AtualizacaoPerfilDTO dados) {
        if (dados.nome() != null && !dados.nome().isBlank()) {
            this.nome = dados.nome();
        }
        if (dados.sobrenome() != null) {
            this.sobrenome = dados.sobrenome();
        }
        if (dados.telefone() != null) {
            this.telefone = dados.telefone();
        }
        if (dados.notificacoesEmailAtivas() != null) {
            this.notificacoesEmailAtivas = dados.notificacoesEmailAtivas();
        }
        if (dados.notificacoesPushAtivas() != null) {
            this.notificacoesPushAtivas = dados.notificacoesPushAtivas();
        }
    }

    public void alterarSenha(String novaSenha) {
        this.senha = novaSenha;
    }

    public void ativarConta() {
        this.ativo = true;
        this.verificationCode = null;
        this.verificationCodeExpiresAt = null;
    }

    public void definirCodigoDeVerificacao(String codigo, LocalDateTime dataExpiracao) {
        this.verificationCode = codigo;
        this.verificationCodeExpiresAt = dataExpiracao;
    }

    public void definirCodigoDeMudancaDeEmail(String novoEmail, String codigo, LocalDateTime dataExpiracao) {
        this.pendingEmail = novoEmail;
        this.emailChangeCode = codigo;
        this.emailChangeCodeExpiresAt = dataExpiracao;
    }

    public void confirmarMudancaDeEmail() {
        if (this.pendingEmail != null) {
            this.email = this.pendingEmail;
        }
        this.pendingEmail = null;
        this.emailChangeCode = null;
        this.emailChangeCodeExpiresAt = null;
    }

    public void definirTokenDeResetDeSenha(String token, LocalDateTime dataExpiracao) {
        this.passwordResetToken = token;
        this.passwordResetTokenExpiresAt = dataExpiracao;
    }

    public void resetarSenha(String novaSenhaHasheada) {
        this.senha = novaSenhaHasheada;
        this.passwordResetToken = null;
        this.passwordResetTokenExpiresAt = null;
    }

    public boolean isTokenDeResetValido(String token) {
        return this.passwordResetToken != null &&
               this.passwordResetToken.equals(token) &&
               this.passwordResetTokenExpiresAt != null &&
               this.passwordResetTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    private void dividirEDefinirNomeCompleto(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return;
        }
        String[] partesNome = nomeCompleto.trim().split("\\s+", 2);
        this.nome = partesNome[0];
        if (partesNome.length > 1) {
            this.sobrenome = partesNome[1];
        } else {
            this.sobrenome = null;
        }
    }

}
