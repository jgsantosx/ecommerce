package com.ecommerce.exception;

/**
 * Violação de uma regra de negócio (ex.: estoque insuficiente, carrinho vazio).
 * Mapeada para HTTP 422 pelo GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
