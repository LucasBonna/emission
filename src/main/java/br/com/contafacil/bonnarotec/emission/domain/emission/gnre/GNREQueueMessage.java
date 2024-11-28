package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GNREQueueMessage {
    private GNREEmissionEntity emission;
    private String xmlContent;
}
