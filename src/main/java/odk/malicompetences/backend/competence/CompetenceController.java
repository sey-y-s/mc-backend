package odk.malicompetences.backend.competence;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/competences")
public class CompetenceController {

    private final CompetenceService competenceService;

    public CompetenceController(CompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    @GetMapping
    public List<CompetenceResponse> trouverToutes() {
        return competenceService.trouverToutes();
    }

    @GetMapping("/{id}")
    public CompetenceResponse trouverParId(
            @PathVariable UUID id
    ) {
        return competenceService.trouverParId(id);
    }
}