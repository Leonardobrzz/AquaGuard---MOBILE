package com.aqua.guard.monitoramento.api.v1.dto.error;

import java.time.Instant;
import java.util.List;

public record ValidationErrorDTO(
        Instant timestamp,
        Integer status,
        String error,
        String path,
        List<FieldValidationError> fieldErrors
) { public static record FieldValidationError(String field, String message) {} }