package com.devvinnii.clientmanager.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        ApiError error = ApiError.from(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        String message = "Erro de validação: " + fieldErrors;
        ApiError error = ApiError.from(HttpStatus.UNPROCESSABLE_ENTITY, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleGeneric(RuntimeException ex, HttpServletRequest request) {
        ApiError error = ApiError.from(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
