package br.com.contafacil.bonnarotec.emission.domain.tecnospeed;

import lombok.Data;

@Data
public class GNREResponse {
    private String numRecibo;
    private String situacao;
    private String motivo;
    private String ufFavorecida;
    private String receita;
    private boolean isError;

    public static GNREResponse fromString(String response) {
        GNREResponse gnreResponse = new GNREResponse();
        String[] parts = response.split(",");

        if (parts[0].equals("EXCEPTION")) {
            gnreResponse.setError(true);
            gnreResponse.setSituacao("EXCEPTION");
            gnreResponse.setMotivo(parts[2]); // Mensagem de erro
            return gnreResponse;
        }

        gnreResponse.setError(false);
        gnreResponse.setNumRecibo(parts[0]);
        gnreResponse.setSituacao(parts[1]);
        gnreResponse.setMotivo(parts[2]);
        if (parts.length > 3) {
            gnreResponse.setUfFavorecida(parts[3]);
        }
        if (parts.length > 4) {
            gnreResponse.setReceita(parts[4]);
        }

        return gnreResponse;
    }
}
