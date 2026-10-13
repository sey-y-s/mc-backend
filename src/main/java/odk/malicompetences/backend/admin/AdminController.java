package odk.malicompetences.backend.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.evaluateur.EvaluateurService;
import odk.malicompetences.backend.evaluateur.EvaluateurCentreResponse;
import odk.malicompetences.backend.evaluateur.DesignationEvaluateurRequest;
import org.springframework.security.core.Authentication;
import odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository;
import odk.malicompetences.backend.opportunite.OpportuniteResponse;
import odk.malicompetences.backend.opportunite.OpportuniteService;
import odk.malicompetences.backend.organisation.OrganisationResponse;
import odk.malicompetences.backend.organisation.OrganisationService;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.testnumerique.Question;
import odk.malicompetences.backend.testnumerique.QuestionRepository;
import odk.malicompetences.backend.testnumerique.TestNumerique;
import odk.malicompetences.backend.testnumerique.TestNumeriqueRepository;
import odk.malicompetences.backend.testnumerique.PropositionReponse;
import odk.malicompetences.backend.testnumerique.PropositionReponseRepository;
import odk.malicompetences.backend.testnumerique.TypeQuestion;
import odk.malicompetences.backend.validation.ValidationResponse;
import odk.malicompetences.backend.validation.ValidationService;
import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Transactional
public class AdminController {

    private final CitoyenRepository citoyenRepository;
    private final OrganisationService organisationService;
    private final ValidationService validationService;
    private final TestNumeriqueRepository testRepository;
    private final QuestionRepository questionRepository;
    private final OpportuniteService opportuniteService;
    private final BesoinRepository besoinRepository;
    private final DemandeMiseEnRelationRepository demandeRepository;
    private final CentreRepository centreRepository;
    private final CompetenceRepository competenceRepository;
    private final PropositionReponseRepository propositionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EvaluateurService evaluateurService;

    public AdminController(
            CitoyenRepository citoyenRepository,
            OrganisationService organisationService,
            ValidationService validationService,
            TestNumeriqueRepository testRepository,
            QuestionRepository questionRepository,
            OpportuniteService opportuniteService,
            BesoinRepository besoinRepository,
            DemandeMiseEnRelationRepository demandeRepository,
            CentreRepository centreRepository,
            CompetenceRepository competenceRepository,
            PropositionReponseRepository propositionRepository,
            UtilisateurRepository utilisateurRepository,
            EvaluateurService evaluateurService
    ) {
        this.citoyenRepository = citoyenRepository;
        this.organisationService = organisationService;
        this.validationService = validationService;
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.opportuniteService = opportuniteService;
        this.besoinRepository = besoinRepository;
        this.demandeRepository = demandeRepository;
        this.centreRepository = centreRepository;
        this.competenceRepository = competenceRepository;
        this.propositionRepository = propositionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.evaluateurService = evaluateurService;
    }



    @PatchMapping("/centres/{centreId}/administrateur/{utilisateurId}")
    public java.util.Map<String, Object> definirAdministrateurCentre(
            @PathVariable UUID centreId, @PathVariable UUID utilisateurId) {
        Centre centre = centreRepository.findById(centreId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Centre introuvable"));
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Utilisateur introuvable"));
        utilisateur.setRole(Role.CENTRE_ADMIN);
        utilisateurRepository.save(utilisateur);
        centre.setAdministrateur(utilisateur);
        centreRepository.save(centre);
        return java.util.Map.of("centreId", centreId, "administrateurId", utilisateurId, "role", "CENTRE_ADMIN");
    }

    @PostMapping("/evaluateurs")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluateurCentreResponse designerEvaluateur(
            @Valid @RequestBody DesignationEvaluateurRequest request,
            Authentication authentication) {
        return evaluateurService.designer(request, authentication.getName());
    }

    @GetMapping("/centres/{centreId}/evaluateurs")
    public List<EvaluateurCentreResponse> evaluateursDuCentre(@PathVariable UUID centreId, Authentication authentication) {
        return evaluateurService.listerPourCentre(centreId, authentication.getName());
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(
                citoyenRepository.count(),
                organisationService.listerToutes().size(),
                validationService.listerToutes().size(),
                testRepository.count(),
                besoinRepository.count(),
                demandeRepository.count()
        );
    }

    @GetMapping("/citoyens")
    public List<CitoyenAdminResponse> citoyens() {
        return citoyenRepository.findAll().stream()
                .map(citoyen -> new CitoyenAdminResponse(
                        citoyen.getId(),
                        citoyen.getNom(),
                        citoyen.getPrenom(),
                        citoyen.getDisponibilite() == null ? null : citoyen.getDisponibilite().name()))
                .toList();
    }

    @GetMapping("/organisations")
    public List<OrganisationAdminResponse> organisations() {
        return organisationService.listerToutes().stream()
                .map(item -> new OrganisationAdminResponse(
                        item.id(), item.utilisateurId(), item.utilisateurEmail(),
                        item.nom(), item.description(),
                        item.statut() == null ? null : item.statut().name()))
                .toList();
    }

    @PatchMapping("/organisations/{id}/statut")
    public OrganisationAdminResponse modifierStatutOrganisation(
            @PathVariable UUID id,
            @RequestBody ModifierStatutOrganisationRequest request
    ) {
        if (request.statut() == null) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        OrganisationResponse updated = organisationService.modifierStatut(id, request.statut());
        return new OrganisationAdminResponse(
                updated.id(), updated.utilisateurId(), updated.utilisateurEmail(),
                updated.nom(), updated.description(),
                updated.statut() == null ? null : updated.statut().name());
    }

    @GetMapping("/validations")
    public List<ValidationAdminResponse> validations() {
        return validationService.listerToutes().stream()
                .map(item -> new ValidationAdminResponse(
                        item.id(),
                        item.type().name(),
                        "Compétence " + item.citoyenCompetenceId(),
                        item.statut().name()))
                .toList();
    }

    @PatchMapping("/validations/{id}/statut")
    public ValidationResponse modifierStatutValidation(
            @PathVariable UUID id,
            @RequestBody ModifierStatutValidationRequest request
    ) {
        if (request.statut() == null) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        return validationService.modifierStatut(id, request.statut(), request.commentaire());
    }

    @GetMapping("/tests-numeriques")
    public List<TestAdminResponse> testsNumeriques() {
        return testRepository.findAll().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @PostMapping("/tests-numeriques")
    @ResponseStatus(HttpStatus.CREATED)
    public TestAdminResponse creerTest(@Valid @RequestBody TestAdminRequest request) {
        if (request.competenceId() == null || request.titre() == null || request.titre().isBlank()) {
            throw new IllegalArgumentException("Une compétence et un titre sont obligatoires.");
        }
        validerQuestionsQcm(request.questions());
        TestNumerique test = new TestNumerique();
        test.setCompetence(competenceRepository.findById(request.competenceId())
                .orElseThrow(() -> new IllegalArgumentException("Compétence introuvable.")));
        test.setTitre(request.titre());
        test.setDescription(request.description());
        test.setActif(Boolean.TRUE.equals(request.actif()));
        return enregistrerTest(test, request.questions());
    }

    @PutMapping("/tests-numeriques/{id}")
    public TestAdminResponse modifierTest(
            @PathVariable UUID id,
            @Valid @RequestBody TestAdminRequest request
    ) {
        TestNumerique test = testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test numérique introuvable."));
        if (request.questions() != null) {
            validerQuestionsQcm(request.questions());
        }
        if (request.competenceId() != null) {
            test.setCompetence(competenceRepository.findById(request.competenceId())
                    .orElseThrow(() -> new IllegalArgumentException("Compétence introuvable.")));
        }
        if (request.titre() != null) test.setTitre(request.titre());
        if (request.description() != null) test.setDescription(request.description());
        if (request.actif() != null) test.setActif(request.actif());
        test = testRepository.save(test);
        if (request.questions() != null) {
            remplacerQuestions(test, request.questions());
        }
        return toAdminResponse(test);
    }

    @DeleteMapping("/tests-numeriques/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiverTest(@PathVariable UUID id) {
        TestNumerique test = testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test numérique introuvable."));
        test.setActif(false);
        testRepository.save(test);
    }

    @GetMapping("/opportunites")
    public List<OpportuniteResponse> opportunites() {
        return opportuniteService.listerToutes();
    }

    @GetMapping("/centres")
    public List<CentreAdminResponse> centres() {
        return centreRepository.findAll().stream()
                .map(centre -> new CentreAdminResponse(
                        centre.getId(), centre.getNom(), centre.getDescription(),
                        centre.getAdresse(), centre.getTelephone()))
                .toList();
    }

    @GetMapping("/competences")
    public List<CompetenceAdminResponse> competences() {
        return competenceRepository.findAll().stream()
                .map(competence -> new CompetenceAdminResponse(
                        competence.getId(),
                        competence.getNom(),
                        competence.getDescription(),
                        competence.getMetier().getNom(),
                        competence.getMetier().getSecteurActivite().getNom()))
                .toList();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return utilisateurRepository.findAll().stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getTelephone(),
                        user.getRole().name(),
                        user.isActif(),
                        user.getDateCreation()))
                .toList();
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdminUserResponse modifierRole(
            @PathVariable UUID id,
            @RequestBody ModifierRoleRequest request
    ) {
        if (request.role() == null) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        user.setRole(request.role());
        Utilisateur saved = utilisateurRepository.save(user);
        return new AdminUserResponse(
                saved.getId(), saved.getEmail(), saved.getTelephone(),
                saved.getRole().name(), saved.isActif(), saved.getDateCreation());
    }

    public record AdminDashboardResponse(
            long citoyens,
            long organisations,
            long validations,
            long tests,
            long besoins,
            long misesEnRelation
    ) {}

    public record CitoyenAdminResponse(UUID id, String nom, String prenom, String disponibilite) {}

    public record OrganisationAdminResponse(
            UUID id, UUID utilisateurId, String email, String nom, String description, String statut
    ) {}

    public record ValidationAdminResponse(UUID id, String type, String libelle, String statut) {}

    public record TestAdminResponse(
            UUID id, UUID competenceId, String titre, String description, String statut,
            List<QuestionAdminResponse> questions
    ) {}

    public record QuestionAdminResponse(
            UUID id, String libelle, String type, Integer ordre, List<PropositionAdminResponse> propositions
    ) {}

    public record CentreAdminResponse(
            UUID id, String nom, String description, String adresse, String telephone
    ) {}

    public record CompetenceAdminResponse(
            UUID id, String nom, String description, String metierNom, String secteurNom
    ) {}

    public record ModifierRoleRequest(Role role) {}
    public record ModifierStatutOrganisationRequest(StatutOrganisation statut) {}
    public record ModifierStatutValidationRequest(StatutValidation statut, String commentaire) {}

    public record AdminUserResponse(
            UUID id, String email, String telephone, String role, boolean actif,
            java.time.LocalDateTime dateCreation
    ) {}

    public record TestAdminRequest(
            UUID competenceId,
            String titre,
            String description,
            Boolean actif,
            List<@Valid QuestionAdminRequest> questions
    ) {}

    public record QuestionAdminRequest(
            @NotBlank String texte,
            @NotNull TypeQuestion type,
            @NotNull Integer ordre,
            List<@Valid PropositionAdminRequest> propositions
    ) {}

    public record PropositionAdminRequest(@NotBlank String texte, boolean correcte) {}

    private TestAdminResponse enregistrerTest(TestNumerique test, List<QuestionAdminRequest> questions) {
        TestNumerique saved = testRepository.save(test);
        if (questions != null) remplacerQuestions(saved, questions);
        return toAdminResponse(saved);
    }

    private void remplacerQuestions(TestNumerique test, List<QuestionAdminRequest> questions) {
        List<Question> anciennes = questionRepository.findByTestIdOrderByOrdreAsc(test.getId());
        for (Question question : anciennes) {
            propositionRepository.deleteAll(propositionRepository.findByQuestionId(question.getId()));
        }
        questionRepository.deleteAll(anciennes);

        for (QuestionAdminRequest request : questions) {
            Question question = new Question();
            question.setTest(test);
            question.setTexte(request.texte());
            question.setType(request.type());
            question.setOrdre(request.ordre());
            Question saved = questionRepository.save(question);

            if (request.propositions() != null) {
                List<PropositionReponse> propositions = request.propositions().stream()
                        .map(proposition -> {
                            PropositionReponse entity = new PropositionReponse();
                            entity.setQuestion(saved);
                            entity.setTexte(proposition.texte());
                            entity.setCorrecte(proposition.correcte());
                            return entity;
                        })
                        .toList();
                propositionRepository.saveAll(propositions);
            }
        }
    }

    private void validerQuestionsQcm(List<QuestionAdminRequest> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Ajoutez au moins une question à choix multiples.");
        }
        for (QuestionAdminRequest question : questions) {
            if (question == null || question.type() != TypeQuestion.CHOIX_MULTIPLE) {
                throw new IllegalArgumentException("Les tests numériques acceptent uniquement les QCM à réponses multiples.");
            }
            if (question.propositions() == null || question.propositions().size() < 2) {
                throw new IllegalArgumentException("Chaque question doit proposer au moins deux réponses.");
            }
            if (question.propositions().stream().anyMatch(
                    proposition -> proposition == null || proposition.texte() == null || proposition.texte().isBlank())) {
                throw new IllegalArgumentException("Les propositions ne peuvent pas être vides.");
            }
            if (question.propositions().stream().noneMatch(PropositionAdminRequest::correcte)) {
                throw new IllegalArgumentException("Chaque question doit avoir au moins une réponse correcte.");
            }
        }
    }

    private TestAdminResponse toAdminResponse(TestNumerique test) {
        List<QuestionAdminResponse> questions = questionRepository
                .findByTestIdOrderByOrdreAsc(test.getId()).stream()
                .map(question -> new QuestionAdminResponse(
                        question.getId(),
                        question.getTexte(),
                        question.getType().name(),
                        question.getOrdre(),
                        propositionRepository.findByQuestionId(question.getId()).stream()
                                .map(proposition -> new PropositionAdminResponse(
                                        proposition.getId(),
                                        proposition.getTexte(),
                                        proposition.isCorrecte()))
                                .toList()))
                .toList();
        return new TestAdminResponse(
                test.getId(), test.getCompetence().getId(), test.getTitre(), test.getDescription(),
                test.isActif() ? "PUBLIQUE" : "BROUILLON", questions);
    }

    public record PropositionAdminResponse(UUID id, String libelle, boolean estCorrecte) {}
}
