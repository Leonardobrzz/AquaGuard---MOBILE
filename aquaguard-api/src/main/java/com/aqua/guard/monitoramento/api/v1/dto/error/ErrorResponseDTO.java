package com.aqua.guard.monitoramento.api.v1.dto.error;

import java.time.Instant;

public record ErrorResponseDTO(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path
) {}