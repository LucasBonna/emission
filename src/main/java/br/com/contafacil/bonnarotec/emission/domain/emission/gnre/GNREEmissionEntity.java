package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "gnre_emissions")
@DiscriminatorValue("GNRE")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GNREEmissionEntity extends EmissionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    private UUID xml;

    @Column(nullable = true)
    private UUID pdf;

    @Column(nullable = true)
    private UUID comprovantePDF;

    @Column(nullable = false)
    private BigDecimal guiaAmount;

    @Column(nullable = true)
    private String numeroRecibo;

    @Column(nullable = false)
    private String chaveNota;

    @Column(nullable = true)
    private String codBarrasGuia;

    @Column(nullable = false)
    private String numNota;

    @Column(nullable = false)
    private String destinatario;
}
