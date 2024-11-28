package br.com.contafacil.bonnarotec.emission.service.impl;

import br.com.contafacil.bonnarotec.emission.client.StorageClient;
import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;
import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionType;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionEntity;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionRepository;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREQueueMessage;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNRERequest;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNRESpecification;
import br.com.contafacil.bonnarotec.emission.domain.exception.EmissionException;
import br.com.contafacil.bonnarotec.emission.domain.xml.XMLProcessResult;
import br.com.contafacil.bonnarotec.emission.service.GNREService;
import br.com.contafacil.bonnarotec.emission.service.XMLService;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientDTO;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.file.FileEntity;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GNREServiceImpl implements GNREService {

    private final StorageClient storageClient;
    private final GNREEmissionRepository gnreEmissionRepository;
    private final KafkaTemplate<String, GNREQueueMessage> kafkaTemplate;
    private final XMLService xmlService;
    private static final String GNRE_QUEUE = "gnre-queue";

    @Override
    @Transactional
    public List<GNREEmissionEntity> issueGNRE(GNRERequest request, UserDTO user, ClientDTO client) {
        List<GNREEmissionEntity> emissions = new ArrayList<>();

        for (MultipartFile xmlFile : request.xmlFiles()) {
            try {
                // Valida e processa o XML
                if (!xmlService.isValidXml(xmlFile)) {
                    throw new EmissionException("Arquivo inválido: " + xmlFile.getOriginalFilename() + " não é um XML válido");
                }

                // Processa o XML e obtém o valor do ICMS
                XMLProcessResult result = xmlService.validateAndProcessGNREXml(xmlFile);
                
                // Faz upload do arquivo original
                FileEntity uploadedFile = storageClient.uploadFile(xmlFile);

                // Cria a entidade de emissão
                GNREEmissionEntity gnreEmission = new GNREEmissionEntity();
                gnreEmission.setClientId(client.getId());
                gnreEmission.setEmissionType(EmissionType.GNRE);
                gnreEmission.setXmlFileId(uploadedFile.getId());
                gnreEmission.setGuiaAmount(result.getIcmsValue());
                gnreEmission.setStatus(EmissionStatus.PROCESSING);
                gnreEmission.setCreatedAt(LocalDateTime.now());
                gnreEmission.setUpdatedAt(LocalDateTime.now());

                GNREEmissionEntity savedEmission = gnreEmissionRepository.save(gnreEmission);
                emissions.add(savedEmission);

                // Cria a mensagem que será enviada para a fila
                String xmlContent = result.getProcessedXml() != null ? result.getProcessedXml() : new String(xmlFile.getBytes());
                GNREQueueMessage queueMessage = new GNREQueueMessage(savedEmission, xmlContent);

                // Envia a mensagem completa para a fila
                kafkaTemplate.send(GNRE_QUEUE, queueMessage);

            } catch (Exception e) {
                throw new EmissionException("Erro ao processar arquivo XML: " + xmlFile.getOriginalFilename(), e);
            }
        }

        return emissions;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GNREEmissionEntity> findGNREs(UUID clientId, EmissionStatus status, LocalDate startDate, LocalDate endDate, boolean includeDeleted, PageRequest pageRequest) {
        Specification<GNREEmissionEntity> spec = GNRESpecification.filterByParams(clientId, status, startDate, endDate, includeDeleted);
        return gnreEmissionRepository.findAll(spec, pageRequest);
    }
}
