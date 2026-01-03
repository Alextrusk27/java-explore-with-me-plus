package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleSpringValidationExceptions(final Exception e) {
        List<String> errors = switch (e) {
            case ConstraintViolationException cve -> cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            case MethodArgumentNotValidException mnv -> mnv.getBindingResult().getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            case MethodArgumentTypeMismatchException mtm -> {
                String typeName = mtm.getRequiredType() != null ?
                        mtm.getRequiredType().getName() : "unknown";
                yield List.of("Parameter '%s' should be of type %s".formatted(mtm.getName(), typeName));
            }
            default -> List.of(e.getMessage());
        };
        log.info("Spring validation exception: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Spring validation Failed", errors,
                        stackTraceToString(e)));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleValidationException(final ValidationException e) {
        log.info("Validation exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Validation Failed",
                        Collections.singletonList(e.getMessage()), stackTraceToString(e)));

    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(final ConflictException e) {
        log.info("Conflict exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT, "Validation Failed",
                        Collections.singletonList(e.getMessage()), stackTraceToString(e)));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException e) {
        log.info("NotFound exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "Validation Failed",
                        Collections.singletonList(e.getMessage()), stackTraceToString(e)));
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleException(final Exception e) {
        log.info("Exception: {}", e.getMessage());
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ApiError.of(status, e.getMessage(),
                        Collections.singletonList(e.getMessage()), stackTraceToString(e)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException ex) {
        log.info("Missing request parameter: {}", ex.getParameterName());
        String error = "Обязательный параметр '%s' отсутствует".formatted(ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Validation Failed",
                        Collections.singletonList(error), stackTraceToString(ex)));
    }

    private String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
