package com.ecommerce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                LocalDateTime.now(),
                errors
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiError handleMessageNotReadable(
            HttpMessageNotReadableException exception
    ) {
        return error(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiError handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ExceptionHandler(BusinessException.class)
    public ApiError handleBusinessException(BusinessException exception) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ApiError handleAuthenticationException(
            AuthenticationException exception
    ) {
        // Mensagem genérica de propósito: não revelamos se o e-mail existe.
        return error(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiError handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        log.warn("Violação de integridade: {}", exception.getMostSpecificCause().getMessage());
        return error(
                HttpStatus.CONFLICT,
                "Operação viola uma restrição de dados (registro duplicado ou em uso)"
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ApiError handleOptimisticLock(
            ObjectOptimisticLockingFailureException exception
    ) {
        return error(
                HttpStatus.CONFLICT,
                "O registro foi alterado por outra operação. Tente novamente."
        );
    }

    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiError handleMaxUploadSize(MaxUploadSizeExceededException exception) {
        return error(HttpStatus.CONTENT_TOO_LARGE, "Arquivo excede o tamanho máximo permitido");
    }

    private ApiError error(HttpStatus status, String message) {
        return new ApiError(status.value(), message, LocalDateTime.now(), null);
    }
}
