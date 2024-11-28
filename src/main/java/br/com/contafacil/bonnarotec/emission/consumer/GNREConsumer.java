package br.com.contafacil.bonnarotec.emission.consumer;

import br.com.contafacil.bonnarotec.emission.client.TecnospeedClient;
import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionEntity;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionRepository;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREQueueMessage;
import br.com.contafacil.bonnarotec.emission.domain.tecnospeed.GNREResponse;
import br.com.contafacil.bonnarotec.emission.repository.ClientRepository;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class GNREConsumer {

    private final TecnospeedClient tecnospeedClient;
    private final GNREEmissionRepository gnreEmissionRepository;
    private final ClientRepository clientRepository;

    @Value("${tecnospeed.auth}")
    private String tecnospeedAuth;

    @KafkaListener(topics = "gnre-queue", groupId = "gnre-group")
    @Transactional
    public void consume(GNREQueueMessage gnreMessage) {
        try {
            log.info("Processando GNRE para emissão ID: {}", gnreMessage.getEmission().getId());
            
            GNREEmissionEntity emission = gnreMessage.getEmission();
            ClientEntity client = clientRepository.findById(emission.getClientId())
                .orElse(null);
            
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
                emission.setStatus(EmissionStatus.FINISHED);
                emission.setUpdatedAt(LocalDateTime.now());
                gnreEmissionRepository.save(emission);
                log.info("GNRE processada com sucesso. Número do recibo: {}", gnreResponse.getNumRecibo());
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
        emission.setUpdatedAt(LocalDateTime.now());
        gnreEmissionRepository.save(emission);
        log.error("Falha no processamento da GNRE: {}", message);
    }
}
