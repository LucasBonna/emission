package br.com.contafacil.bonnarotec.emission.controller;

import br.com.contafacil.bonnarotec.emission.domain.emission.EmissionStatus;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionEntity;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNREEmissionResult;
import br.com.contafacil.bonnarotec.emission.domain.emission.gnre.GNRERequest;
import br.com.contafacil.bonnarotec.emission.service.GNREService;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.client.ClientDTO;
import br.com.contafacil.shared.bonnarotec.toolslib.domain.user.UserDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@Tag(name = "GNRE", description = "Rotas de emissao de GNRE")
@RequestMapping("/emission/gnre")
public class GNREController {

    private final GNREService gnreService;
    private final ObjectMapper objectMapper;

    public GNREController(GNREService gnreService, ObjectMapper objectMapper) {
        this.gnreService = gnreService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Emitir GNRE",
            description = "Realiza a emissao de uma GNRE"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GNREEmissionResult> issueGNRE(
       @Valid @ModelAttribute GNRERequest request,
       @Parameter(hidden = true)
       @RequestHeader(value = "X-Client") String clientJSON,
       @Parameter(hidden = true)
       @RequestHeader(value = "X-User") String userJSON
    ) {
        try {
            ClientDTO client = objectMapper.readValue(clientJSON, ClientDTO.class);
            UserDTO user = objectMapper.readValue(userJSON, UserDTO.class);

            GNREEmissionResult emissions = gnreService.issueGNRE(request, user, client);

            return new ResponseEntity<>(emissions, HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            System.out.println("error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(
            summary = "Listar Emissões GNRE",
            description = "Lista todas as emissões de GNRE com paginação e filtros"
    )
    @GetMapping
    public ResponseEntity<Page<GNREEmissionEntity>> listGNREs(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) EmissionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<GNREEmissionEntity> emissions = gnreService.findGNREs(clientId, status, startDate, endDate, includeDeleted, pageRequest);
        return ResponseEntity.ok(emissions);
    }
}
