package odk.malicompetences.backend.citoyen;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/citoyens")
public class CitoyenController {

    private final CitoyenService citoyenService;

    public CitoyenController(CitoyenService citoyenService) {
        this.citoyenService = citoyenService;
    }

    @PostMapping("/utilisateur/{utilisateurId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CitoyenResponse creer(
            @PathVariable UUID utilisateurId,
            @Valid @RequestBody CitoyenRequest request
    ) {
        return citoyenService.creer(utilisateurId, request);
    }

    @GetMapping("/{id}")
    public CitoyenResponse trouverParId(
            @PathVariable UUID id
    ) {
        return citoyenService.trouverParId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitoyenResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody CitoyenRequest request) {

        return ResponseEntity.ok(citoyenService.modifier(id, request));
    }
}