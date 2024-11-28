package br.com.contafacil.bonnarotec.emission.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tecnospeed", url = "${tecnospeed.url}")
public interface TecnospeedClient {
    
    @PostMapping(value = "/gnre/envia", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String enviarGNRE(
        @RequestHeader("Authorization") String authorization,
        @RequestParam("Grupo") String grupo,
        @RequestParam("CNPJ") String cnpj,
        @RequestParam("Arquivo") String arquivo
    );
}
