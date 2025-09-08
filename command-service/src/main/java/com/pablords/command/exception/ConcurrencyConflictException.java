package com.pablords.command.exception;

/**
 * Exceção de domínio para o ControllerAdvice traduzir p/ HTTP 409 (Conflict) ou
 * 503 (Retry-After).
 */
public class ConcurrencyConflictException extends RuntimeException {
	public ConcurrencyConflictException(String message) {
		super(message);
	}
}