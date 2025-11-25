package com.aqua.guard.monitoramento.api.v1.dto;

import jakarta.validation.constraints.Pattern;

public record AtualizacaoPerfilDTO(
        String nome,
        String sobrenome,
        @Pattern(regexp = "\\d{11,11}", message = "O telefone deve conter apenas números, incluindo o DDD.")
        String telefone,
        Boolean notificacoesEmailAtivas,
        Boolean notificacoesPushAtivas
) {}