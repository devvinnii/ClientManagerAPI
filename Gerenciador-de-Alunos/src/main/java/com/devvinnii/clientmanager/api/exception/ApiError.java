package com.devvinnii.clientmanager.api.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ApiError {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;

    public static ApiError from(HttpStatus status, String message, String path) {
        return from(status, message, path, Map.of());
    }

    public static ApiError from(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .fieldErrors(Map.copyOf(fieldErrors))
                .build();
    }
}
