package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GNREEmissionResult {
    private List<GNREEmissionError> errors;
    private List<GNREEmissionEntity> successfulEmissions;
    private List<GNREEmissionEntity> failedEmissions;

    public GNREEmissionResult() {
        this.errors = new ArrayList<>();
        this.successfulEmissions = new ArrayList<>();
        this.failedEmissions = new ArrayList<>();
    }

    public void addError(String message, String chaveNota) {
        this.errors.add(new GNREEmissionError(message, chaveNota));
    }

    public void addSuccessfulEmission(GNREEmissionEntity emission) {
        this.successfulEmissions.add(emission);
    }

    public void addFailedEmission(GNREEmissionEntity emission) {
        this.failedEmissions.add(emission);
    }

    @Getter
    public static class GNREEmissionError {
        private final String message;
        private final String chaveNota;

        public GNREEmissionError(String message, String chaveNota) {
            this.message = message;
            this.chaveNota = chaveNota;
        }
    }
}
