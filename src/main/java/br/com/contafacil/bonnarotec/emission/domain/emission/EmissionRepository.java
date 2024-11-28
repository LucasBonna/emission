package br.com.contafacil.bonnarotec.emission.domain.emission;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmissionRepository extends JpaRepository<EmissionEntity, UUID> {

}
