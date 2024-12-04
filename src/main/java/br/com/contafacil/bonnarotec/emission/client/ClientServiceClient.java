package br.com.contafacil.bonnarotec.emission.client;

import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(url = "${registry.url}", name = "registry-service")
public interface ClientServiceClient {
    
    @GetMapping("/api/v1/clients/{id}")
    ClientEntity findById(@PathVariable("id") UUID id);
}
