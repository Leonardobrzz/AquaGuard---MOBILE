package com.aqua.guard.monitoramento.api.v1.dto;

import com.aqua.guard.monitoramento.core.entity.Usuario;

import java.util.UUID;

public record DetalhamentoUsuarioDTO(
        UUID id,
        String nome,
        String sobrenome,
        String nomeCompleto,
        String email,
        String telefone
) {

    public DetalhamentoUsuarioDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getTelefone()
        );
    }
}