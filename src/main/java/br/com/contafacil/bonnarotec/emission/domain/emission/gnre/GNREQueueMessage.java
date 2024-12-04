package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GNREQueueMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private GNREEmissionEntity emission;
    private String xmlContent;
}
