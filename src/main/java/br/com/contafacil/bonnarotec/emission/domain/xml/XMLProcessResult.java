package br.com.contafacil.bonnarotec.emission.domain.xml;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class XMLProcessResult {
    private final String processedXml;
    private final BigDecimal icmsValue;
}
