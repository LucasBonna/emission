package br.com.contafacil.bonnarotec.emission.domain.exception;

public class XMLProcessingException extends EmissionException {
    public XMLProcessingException(String message) {
        super(message);
    }

    public XMLProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
