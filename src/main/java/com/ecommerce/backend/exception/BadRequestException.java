package com.ecommerce.backend.exception;

/**
 * Thrown when a request is well-formed but semantically invalid
 * (e.g. insufficient stock, duplicate email). Mapped to HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
