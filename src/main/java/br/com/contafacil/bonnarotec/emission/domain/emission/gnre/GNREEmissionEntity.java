package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "gnre_emissions")
@DiscriminatorValue("GNRE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GNREEmissionEntity extends EmissionEntity {

    private UUID xmlFileId;

    @Column(nullable = true)
    private UUID guiaPDFFileId;

    @Column(nullable = true)
    private UUID comprovantePDFFileId;

    private BigDecimal guiaAmount;

    @Column(nullable = true)
    private String numeroRecibo;

    @Column(nullable = true)
    private String chaveGuia;
}
