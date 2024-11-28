package br.com.contafacil.bonnarotec.emission.service;

import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionEntity;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNRERequest;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientDTO;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.user.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GNREService {

    List<GNREEmissionEntity> issueGNRE(GNRERequest request, UserDTO user, ClientDTO client);
    Page<GNREEmissionEntity> findGNREs(UUID clientId, EmissionStatus status, LocalDate startDate, LocalDate endDate, boolean includeDeleted, PageRequest pageRequest);

}
