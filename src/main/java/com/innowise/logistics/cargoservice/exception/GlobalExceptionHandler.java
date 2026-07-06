package com.innowise.logistics.cargoservice.exception;

import com.innowise.logistics.cargoservice.dto.response.ErrorResponse;
import com.innowise.logistics.cargoservice.util.ExceptionUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== 1. ОШИБКИ ВАЛИДАЦИИ DTO (@Valid, @NotNull, @Positive в RequestPart/RequestBody) =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String combinedErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации параметров",
                combinedErrors,
                ex
        );
    }

    // ===== 2. ОШИБКИ ВАЛИДАЦИИ ПАРАМЕТРОВ МЕТОДОВ (@PathVariable, @RequestParam в Контроллерах) =====
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Некорректные параметры HTTP-запроса",
                ex.getMessage(),
                ex
        );
    }

    // ===== 3. ОШИБКИ ДЕСЕРИАЛИЗАЦИИ / ПАРСИНГА JSON (Jackson десериализация) =====
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleParseError(HttpMessageNotReadableException ex) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Некорректный формат входящих данных",
                "Проверьте структуру передаваемого JSON. Поля идентификаторов должны содержать целые числа (Long)",
                ex
        );
    }

    // ===== 4. РЕСУРС НЕ НАЙДЕН НА ДИСКЕ / В БАЗЕ (JPA EntityNotFoundException) =====
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Запрашиваемый ресурс отсутствует",
                ex.getMessage(),
                ex
        );
    }

    // ===== 5. ТОЧЕЧНЫЕ ИСКЛЮЧЕНИЯ SPRING (ResponseStatusException из наших сервисов) =====
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String errorTitle = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

        return buildErrorResponse(
                status,
                errorTitle,
                ex.getMessage(),
                ex
        );
    }

    // ===== 6. ГЛОБАЛЬНЫЙ ПЕРЕХВАТЧИК (Защита от падения в сырой стек-трейс 500) =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка на стороне логистической платформы: " + ex.getMessage(),
                ex
        );
    }

    // =========================================================================
    // ==========   ЦЕНТРАЛИЗОВАННЫЙ СТАНДАРТ СБОРКИ HTTP-ОТВЕТОВ   ==========
    // =========================================================================
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,
                                                             String errorSummary,
                                                             String detailMessage,
                                                             Exception ex) {
        // Автоматическое логирование инцидента с полной трассировкой стека для отладки
        log.error("На падении [{}] перехвачено исключение: {}", errorSummary, ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorSummary)
                .message(detailMessage)
                .path(ExceptionUtils.getRequestPath()) // Динамический захват URI, где упало
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
