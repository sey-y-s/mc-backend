package odk.malicompetences.backend.testnumerique;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tests-numeriques")
public class TestNumeriqueController {
    private final TestNumeriqueService service;

    public TestNumeriqueController(TestNumeriqueService service) {
        this.service = service;
    }

    @GetMapping("/competence/{competenceId}")
    public List<TestNumeriqueResponse> parCompetence(@PathVariable UUID competenceId) {
        return service.trouverPourCompetence(competenceId).stream()
                .map(test -> TestNumeriqueResponse.from(test, service.compterQuestions(test.getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public TestNumeriqueResponse parId(@PathVariable UUID id) {
        TestNumerique test = service.trouver(id);
        return TestNumeriqueResponse.from(test, service.compterQuestions(test.getId()));
    }

    @GetMapping("/{id}/questions")
    public List<QuestionPubliqueResponse> questions(@PathVariable UUID id,
            @RequestParam(required = false) UUID demandeValidationId, Authentication authentication) {
        boolean administrateur = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
        String authId = authentication == null ? null : authentication.getName();
        return service.trouverQuestions(id, demandeValidationId, authId, administrateur)
                .stream().map(QuestionPubliqueResponse::from).toList();
    }

    @PostMapping("/{id}/tentatives")
    @PreAuthorize("hasRole('CITOYEN')")
    public TentativeTestResponse soumettre(@PathVariable UUID id, @Valid @RequestBody TentativeTestRequest request,
                                           Authentication authentication) {
        return TentativeTestResponse.from(service.soumettre(id, request.demandeValidationId(),
                request.reponses(), authentication.getName()));
    }
}
