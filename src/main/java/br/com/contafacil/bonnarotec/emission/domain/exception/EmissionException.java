package br.com.contafacil.bonnarotec.emission.domain.exception;

public class EmissionException extends RuntimeException {
    public EmissionException(String message) {
        super(message);
    }

    public EmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
