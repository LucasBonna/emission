package br.com.contafacil.bonnarotec.emission.domain.emission;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "emissions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "emission_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class EmissionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "emission_type", nullable = false, insertable = false, updatable = false)
    private EmissionType emissionType;

    @Column(nullable = false)
    private UUID clientId;

    private String message;

    @Enumerated(EnumType.STRING)
    private EmissionStatus status;

    @Column(nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;
}
