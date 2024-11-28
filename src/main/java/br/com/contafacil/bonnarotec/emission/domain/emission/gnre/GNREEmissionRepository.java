package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface GNREEmissionRepository extends JpaRepository<GNREEmissionEntity, UUID>, JpaSpecificationExecutor<GNREEmissionEntity> {
}
