package br.com.contafacil.bonnarotec.emission.client;

import br.com.contafacil.shared.bonnarotec.toolslib.domain.file.FileEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(url = "${spring.storage.service.url}", name = "storage-service")
public interface StorageClient {

    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FileEntity uploadFile(@RequestPart("file") MultipartFile file);

}
