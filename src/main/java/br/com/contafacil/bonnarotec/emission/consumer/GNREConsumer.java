package br.com.contafacil.bonnarotec.emission.consumer;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.contafacil.bonnarotec.emission.client.ClientServiceClient;
import br.com.contafacil.bonnarotec.emission.client.StorageClient;
import br.com.contafacil.bonnarotec.emission.client.TecnospeedClient;
import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionEntity;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionRepository;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREQueueMessage;
import br.com.contafacil.bonnarotec.emission.domain.tecnospeed.GNREResponse;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientEntity;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.file.FileEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GNREConsumer {

    private final TecnospeedClient tecnospeedClient;
    private final StorageClient storageClient;
    private final GNREEmissionRepository gnreEmissionRepository;
    private final ClientServiceClient clientServiceClient;

    @Value("${tecnospeed.auth}")
    private String tecnospeedAuth;

    public GNREConsumer(
            TecnospeedClient tecnospeedClient,
            StorageClient storageClient,
            GNREEmissionRepository gnreEmissionRepository,
            ClientServiceClient clientServiceClient) {
        this.tecnospeedClient = tecnospeedClient;
        this.storageClient = storageClient;
        this.gnreEmissionRepository = gnreEmissionRepository;
        this.clientServiceClient = clientServiceClient;
    }

    @KafkaListener(topics = "gnre-queue", groupId = "gnre-group")
    @Transactional
    public void consume(GNREQueueMessage gnreMessage) {
        try {
            log.info("Processando GNRE para emissão ID: {}, cliente ID: {}", 
                gnreMessage.getEmission().getId(),
                gnreMessage.getEmission().getClientId());
            
            GNREEmissionEntity emission = gnreMessage.getEmission();
            ClientEntity client;
            try {
                client = clientServiceClient.findById(emission.getClientId());
            } catch (Exception e) {
                log.error("Erro ao chamar client-service: ", e);
                updateEmissionStatus(emission, EmissionStatus.FAILED, "Erro ao buscar dados do cliente: " + e.getMessage());
                return;
            }
            
            if (client == null || client.getCnpj() == null) {
                updateEmissionStatus(emission, EmissionStatus.FAILED, "Cliente não encontrado ou CNPJ não cadastrado");
                return;
            }
            String response = tecnospeedClient.enviarGNRE(
                "Basic " + tecnospeedAuth,
                "ContaFacil",
                client.getCnpj(),
                gnreMessage.getXmlContent()
            );

            GNREResponse gnreResponse = GNREResponse.fromString(response);
            
            if (gnreResponse.isError()) {
                updateEmissionStatus(emission, EmissionStatus.FAILED, gnreResponse.getMotivo());
                return;
            }

            if ("AUTORIZADA".equals(gnreResponse.getSituacao())) {
                emission.setNumeroRecibo(gnreResponse.getNumRecibo());
                
                // Gera o PDF da GNRE
                try {
                    byte[] pdfContent = tecnospeedClient.imprimirGNRE(
                        "Basic " + tecnospeedAuth,
                        "ContaFacil",
                        client.getCnpj(),
                        "0", // 0 para retornar o conteúdo binário
                        gnreResponse.getNumRecibo()
                    );

                    // Converte o PDF para MultipartFile
                    MultipartFile pdfFile = new MockMultipartFile(
                        "gnre.pdf",
                        "gnre.pdf",
                        "application/pdf",
                        pdfContent
                    );

                    // Envia o PDF para o storage
                    FileEntity uploadedPdf = storageClient.uploadFile(pdfFile);
                    emission.setPdf(uploadedPdf.getId());
                    
                    emission.setStatus(EmissionStatus.FINISHED);
                    emission.setUpdatedAt(LocalDateTime.now());
                    gnreEmissionRepository.save(emission);
                    log.info("GNRE processada com sucesso. Número do recibo: {}, PDF ID: {}", 
                        gnreResponse.getNumRecibo(), uploadedPdf.getId());
                } catch (Exception e) {
                    log.error("Erro ao gerar/salvar PDF da GNRE: ", e);
                    updateEmissionStatus(emission, EmissionStatus.EXCEPTION, 
                        "GNRE autorizada mas houve erro ao gerar PDF: " + e.getMessage());
                }
            } else {
                updateEmissionStatus(emission, EmissionStatus.FAILED, 
                    String.format("Situação: %s, Motivo: %s", gnreResponse.getSituacao(), gnreResponse.getMotivo()));
            }

        } catch (Exception e) {
            log.error("Erro ao processar GNRE: ", e);
            updateEmissionStatus(gnreMessage.getEmission(), EmissionStatus.EXCEPTION, 
                "Erro interno ao processar GNRE: " + e.getMessage());
        }
    }

    private void updateEmissionStatus(GNREEmissionEntity emission, EmissionStatus status, String message) {
        emission.setStatus(status);
        emission.setMessage(message);
        emission.setUpdatedAt(LocalDateTime.now());
        gnreEmissionRepository.save(emission);
        log.error("Falha no processamento da GNRE: {}", message);
    }
}
