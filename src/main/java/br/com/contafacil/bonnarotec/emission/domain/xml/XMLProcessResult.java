package br.com.contafacil.bonnarotec.emission.domain.xml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class XMLProcessResult {
    private MultipartFile processedXml;
    private BigDecimal icmsValue;
    private String chaveNota;
    private String numNota;
    private String destinatario;
}
