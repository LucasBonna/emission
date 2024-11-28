package br.com.contafacil.bonnarotec.emission.repository;

import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
}
