package br.com.contafacil.bonnarotec.emission.service.impl;

import br.com.contafacil.bonnarotec.emission.client.StorageClient;
import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;
import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionType;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionEntity;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionRepository;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionResult;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREQueueMessage;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNRERequest;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNRESpecification;
import br.com.contafacil.bonnarotec.emission.domain.xml.XMLProcessResult;
import br.com.contafacil.bonnarotec.emission.service.GNREService;
import br.com.contafacil.bonnarotec.emission.service.XMLService;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientDTO;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.file.FileEntity;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.user.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;
import java.util.UUID;

@Service
public class GNREServiceImpl implements GNREService {

    private static final Logger log = LoggerFactory.getLogger(GNREServiceImpl.class);
    private final StorageClient storageClient;
    private final GNREEmissionRepository gnreEmissionRepository;
    private final KafkaTemplate<String, GNREQueueMessage> kafkaTemplate;
    private final XMLService xmlService;
    private static final String GNRE_QUEUE = "gnre-queue";

    public GNREServiceImpl(StorageClient storageClient, GNREEmissionRepository gnreEmissionRepository, 
            KafkaTemplate<String, GNREQueueMessage> kafkaTemplate, XMLService xmlService) {
        this.storageClient = storageClient;
        this.gnreEmissionRepository = gnreEmissionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.xmlService = xmlService;
    }

    @Override
    @Transactional
    public GNREEmissionResult issueGNRE(GNRERequest request, UserDTO user, ClientDTO client) {
        GNREEmissionResult result = new GNREEmissionResult();

        for (MultipartFile xmlFile : request.xmlFiles()) {
            try {
                log.info("Iniciando processamento do arquivo XML: {}", xmlFile.getOriginalFilename());
                
                // Valida e processa o XML
                if (!xmlService.isValidXml(xmlFile)) {
                    log.error("Arquivo inválido: {}", xmlFile.getOriginalFilename());
                    result.addError("XML inválido", null);
                    continue;
                }

                log.info("XML válido, processando conteúdo...");
                XMLProcessResult xmlResult = xmlService.validateAndProcessGNREXml(xmlFile);

                // Verifica se já existe uma GNRE emitida para esta nota
                Optional<GNREEmissionEntity> existingEmission = gnreEmissionRepository
                    .findByChaveNotaAndStatus(xmlResult.getChaveNota(), EmissionStatus.FINISHED);
                if (existingEmission.isPresent()) {
                    log.info("GNRE já emitida para a chave de nota: {}", xmlResult.getChaveNota());
                    result.addError("GNRE já emitida para esta nota fiscal", xmlResult.getChaveNota());
                    continue;
                }

                log.info("Enviando arquivo para storage...");
                // Faz upload do arquivo original
                FileEntity uploadedFile = storageClient.uploadFile(xmlFile);
                log.info("Arquivo salvo no storage com ID: {}", uploadedFile.getId());

                // Cria a entidade de emissão
                GNREEmissionEntity gnreEmission = new GNREEmissionEntity();
                gnreEmission.setClientId(client.getId());
                gnreEmission.setChaveNota(xmlResult.getChaveNota());
                gnreEmission.setEmissionType(EmissionType.GNRE);
                gnreEmission.setXml(uploadedFile.getId());
                gnreEmission.setGuiaAmount(xmlResult.getIcmsValue());
                gnreEmission.setStatus(EmissionStatus.PROCESSING);
                gnreEmission.setCreatedAt(LocalDateTime.now());
                gnreEmission.setUpdatedAt(LocalDateTime.now());

                GNREEmissionEntity savedEmission = gnreEmissionRepository.save(gnreEmission);
                result.addSuccessfulEmission(savedEmission);
                log.info("Emissão criada com ID: {}", savedEmission.getId());

                // Cria a mensagem que será enviada para a fila
                // String xmlContent = xmlResult.getProcessedXml() != null ? 
                //     xmlResult.getProcessedXml() : new String(xmlFile.getBytes());
                String xmlContent = new String(xmlResult.getProcessedXml().getBytes());
                GNREQueueMessage queueMessage = new GNREQueueMessage(savedEmission, xmlContent);

                // Envia a mensagem completa para a fila
                kafkaTemplate.send(GNRE_QUEUE, queueMessage);
                log.info("Mensagem enviada para fila de processamento");

            } catch (Exception e) {
                log.error("Erro ao processar arquivo XML: {}", e.getMessage(), e);
                result.addError(e.getMessage(), null);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GNREEmissionEntity> findGNREs(UUID clientId, EmissionStatus status, 
            LocalDate startDate, LocalDate endDate, boolean includeDeleted, PageRequest pageRequest) {
        Specification<GNREEmissionEntity> spec = GNRESpecification.filterByParams(
            clientId, status, startDate, endDate, includeDeleted);
        return gnreEmissionRepository.findAll(spec, pageRequest);
    }
}
