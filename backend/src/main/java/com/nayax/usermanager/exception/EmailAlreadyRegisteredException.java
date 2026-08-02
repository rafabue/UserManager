package com.nayax.usermanager.exception;

/**
 * Lança quando o e-mail informado já pertence a outro usuário. Traduzida para HTTP 409 por
 * {@link GlobalExceptionHandler}.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String email) {
        super("E-mail já cadastrado: " + email);
    }
}
