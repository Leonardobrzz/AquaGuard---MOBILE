package com.aqua.guard.monitoramento.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "dispositivos_usuario")
@Entity(name = "DispositivoUsuario")
@Getter
@NoArgsConstructor
public class DispositivoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "push_token", nullable = false, unique = true)
    private String pushToken;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public DispositivoUsuario(Usuario usuario, String pushToken) {
        this.usuario = usuario;
        this.pushToken = pushToken;
    }

}