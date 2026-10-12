package odk.malicompetences.backend.testnumerique;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/tests-numeriques")
public class TestNumeriqueController {

    private final TestNumeriqueService service;
    private final ResultatTestRepository resultatRepository;

    public TestNumeriqueController(TestNumeriqueService service, ResultatTestRepository resultatRepository) {
        this.service = service;
        this.resultatRepository = resultatRepository;
    }

    @GetMapping("/competence/{competenceId}")
    public List<TestNumerique> parCompetence(@PathVariable UUID competenceId) {
        return service.trouverPourCompetence(competenceId);
    }

    @GetMapping("/{id}")
    public TestNumerique parId(@PathVariable UUID id) {
        return service.trouver(id);
    }

    @GetMapping("/{id}/questions")
    public List<Question> questions(@PathVariable UUID id) {
        return service.trouverQuestions(id);
    }

    @GetMapping("/questions/{questionId}/propositions")
    public List<PropositionPubliqueResponse> propositions(@PathVariable UUID questionId) {
        return service.trouverPropositions(questionId).stream()
                .map(PropositionPubliqueResponse::from)
                .toList();
    }

    @PreAuthorize("hasAnyRole('CITOYEN', 'ADMIN', 'SUPER_ADMIN')")

    @PostMapping("/{id}/resultats")
    public ResultatTestResponse soumettre(@PathVariable UUID id,
                                          @Valid @RequestBody ResultatTestRequest request, Authentication authentication) {
        boolean administrateur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!administrateur && !authentication.getName().equals(request.citoyenId().toString())) {
            throw new AccessDeniedException("Vous ne pouvez soumettre un test que pour votre propre compte.");
        }
        return ResultatTestResponse.from(service.soumettre(id, request.citoyenId(), request.reponses()));
    }

    @GetMapping("/resultats/citoyen/{citoyenId}")
    public List<ResultatTestResponse> resultats(@PathVariable UUID citoyenId, Authentication authentication) {
        boolean administrateur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!administrateur && !authentication.getName().equals(citoyenId.toString())) {
            throw new AccessDeniedException("Vous ne pouvez consulter que vos propres résultats.");
        }
        return resultatRepository.findByCitoyenIdOrderByDatePassageDesc(citoyenId)
                .stream().map(ResultatTestResponse::from).toList();
    }
}
