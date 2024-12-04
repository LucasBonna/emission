package br.com.contafacil.bonnarotec.emission.service;

import br.com.contafacil.bonnarotec.emission.domain.xml.XMLProcessResult;
import org.springframework.web.multipart.MultipartFile;

public interface XMLService {
    
    XMLProcessResult validateAndProcessGNREXml(MultipartFile file) throws Exception;
    
    boolean isValidXml(MultipartFile file);
}
