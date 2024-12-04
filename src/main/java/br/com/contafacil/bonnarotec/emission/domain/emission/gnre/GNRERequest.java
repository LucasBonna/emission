package br.com.contafacil.bonnarotec.emission.domain.emission.gnre;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record GNRERequest(
        @NotNull @NotEmpty List<MultipartFile> xmlFiles
) {}
