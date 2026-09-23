package com.devvinnii.clientmanager.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Cliente não encontrado
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ApiError> handleClientNotFound(ClientNotFoundException ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // CPF duplicado
    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleClientAlreadyExists(ClientAlreadyExistsException ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Erro de upload de arquivo
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiError> handleFileUpload(FileUploadException ex, HttpServletRequest request) {
        LOGGER.error("Erro ao processar upload em {}", request.getRequestURI(), ex);
        ApiError error = ApiError.from(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar o arquivo enviado", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return validationError(ex.getBindingResult(), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBinding(BindException ex, HttpServletRequest request) {
        return validationError(ex.getBindingResult(), request);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.BAD_REQUEST, "Requisição inválida", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo excede o tamanho máximo permitido", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.CONFLICT, "Conflito ao persistir os dados", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledUser(DisabledException ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.FORBIDDEN, "Usuário inativo", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    private ResponseEntity<ApiError> validationError(BindingResult bindingResult, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiError error = ApiError.from(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos", request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOGGER.error("Erro inesperado em {}", request.getRequestURI(), ex);
        ApiError error = ApiError.from(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
