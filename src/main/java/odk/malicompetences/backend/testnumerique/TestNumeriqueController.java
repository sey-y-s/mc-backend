package odk.malicompetences.backend.testnumerique;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public List<PropositionReponse> propositions(@PathVariable UUID questionId) {
        return service.trouverPropositions(questionId);
    }

    @PostMapping("/{id}/resultats")
    public ResultatTestResponse soumettre(@PathVariable UUID id,
                                          @Valid @RequestBody ResultatTestRequest request) {
        return ResultatTestResponse.from(service.soumettre(id, request.citoyenId(), request.reponses()));
    }

    @GetMapping("/resultats/citoyen/{citoyenId}")
    public List<ResultatTestResponse> resultats(@PathVariable UUID citoyenId) {
        return resultatRepository.findByCitoyenIdOrderByDatePassageDesc(citoyenId)
                .stream().map(ResultatTestResponse::from).toList();
    }
}
