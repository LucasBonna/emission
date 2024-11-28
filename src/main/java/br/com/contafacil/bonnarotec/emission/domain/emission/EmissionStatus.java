package br.com.contafacil.bonnarotec.emission.domain.emission;

import lombok.Getter;

@Getter
public enum EmissionStatus {
    FINISHED,
    PROCESSING,
    EXCEPTION,
    FAILED;
}
