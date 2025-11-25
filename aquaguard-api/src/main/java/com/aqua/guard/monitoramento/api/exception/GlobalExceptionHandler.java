package com.aqua.guard.monitoramento.api.exception;

import com.aqua.guard.monitoramento.api.v1.dto.error.ErrorResponseDTO;
import com.aqua.guard.monitoramento.api.v1.dto.error.ValidationErrorDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handler para erros de validação
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationErrorDTO.FieldValidationError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        var errorDTO = new ValidationErrorDTO(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação",
                request.getRequestURI(),
                fieldErrors
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    // Handler para "Não Encontrado"
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        var errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    // Handler para "Acesso Negado"
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        var errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                "Você não tem permissão para acessar este recurso.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.FORBIDDEN);
    }

    // Handler para conflitos de estado
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        var errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflito de Estado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    // Handler genérico para qualquer outra exceção não tratada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        var errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno do Servidor",
                "Ocorreu um erro inesperado. Cheque o log da aplicação.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handler para quando falta um parâmetro na URL
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("O parâmetro obrigatório '%s' do tipo '%s' não foi encontrado.", ex.getParameterName(), ex.getParameterType());
        var errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro de Requisição Faltando",
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    // Handler para violações de constraints do banco
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        var errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflito de Dados",
                "Já existe um recurso com os dados fornecidos (ex: e-mail ou serial number já cadastrado).",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    // Handler para erros de token JWT
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        var errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Token Inválido",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.UNAUTHORIZED);
    }

}