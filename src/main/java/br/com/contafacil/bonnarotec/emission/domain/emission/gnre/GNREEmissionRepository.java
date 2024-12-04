package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GNREEmissionRepository extends JpaRepository<GNREEmissionEntity, UUID>, JpaSpecificationExecutor<GNREEmissionEntity> {
    Optional<GNREEmissionEntity> findByChaveNotaAndStatus(String chaveNota, EmissionStatus status);
}
