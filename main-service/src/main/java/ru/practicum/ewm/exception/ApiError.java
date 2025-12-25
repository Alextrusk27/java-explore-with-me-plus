package ru.practicum.ewm.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        HttpStatus status,
        String message,
        List<String> errors,
        String stackTrace) {

    public static ApiError of(HttpStatus status, String message, List<String> errors, String stackTrace) {
        return new ApiError(
                LocalDateTime.now(),
                status,
                message,
                errors,
                stackTrace
        );
    }
}
