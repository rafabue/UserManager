package com.nayax.usermanager.exception;

/**
 * Lançada quando um recurso solicitado por identificador não existe. Traduzida em HTTP 404
 * pelo {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
