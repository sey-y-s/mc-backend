package odk.malicompetences.backend.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.audit.AuditLogService;
import odk.malicompetences.backend.audit.AuditLog;
import org.springframework.data.domain.Page;
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
import odk.malicompetences.backend.testnumerique.TypeQuestion;
import odk.malicompetences.backend.testnumerique.TentativeTestRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import odk.malicompetences.backend.validation.DemandeValidationRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.common.statut.StatutCompte;
import odk.malicompetences.backend.utilisateur.RoleEntity;
import odk.malicompetences.backend.utilisateur.RoleRepository;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.centre.CentreAdministrateur;
import odk.malicompetences.backend.centre.CentreAdministrateurRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Transactional
public class AdminController {

    private final CitoyenRepository citoyenRepository;
    private final OrganisationService organisationService;
    private final DemandeValidationRepository demandeValidationRepository;
    private final TestNumeriqueRepository testRepository;
    private final QuestionRepository questionRepository;
    private final OpportuniteService opportuniteService;
    private final BesoinRepository besoinRepository;
    private final DemandeMiseEnRelationRepository demandeRepository;
    private final CentreRepository centreRepository;
    private final CentreAdministrateurRepository centreAdministrateurRepository;
    private final RoleRepository roleRepository;
    private final CompetenceRepository competenceRepository;
    private final TentativeTestRepository tentativeTestRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogService auditLogService;

    public AdminController(
            CitoyenRepository citoyenRepository,
            OrganisationService organisationService,
            DemandeValidationRepository demandeValidationRepository,
            TestNumeriqueRepository testRepository,
            QuestionRepository questionRepository,
            OpportuniteService opportuniteService,
            BesoinRepository besoinRepository,
            DemandeMiseEnRelationRepository demandeRepository,
            CentreRepository centreRepository,
            CentreAdministrateurRepository centreAdministrateurRepository,
            RoleRepository roleRepository,
            CompetenceRepository competenceRepository,
            TentativeTestRepository tentativeTestRepository,
            UtilisateurRepository utilisateurRepository,
            AuditLogService auditLogService
    ) {
        this.citoyenRepository = citoyenRepository;
        this.organisationService = organisationService;
        this.demandeValidationRepository = demandeValidationRepository;
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.opportuniteService = opportuniteService;
        this.besoinRepository = besoinRepository;
        this.demandeRepository = demandeRepository;
        this.centreRepository = centreRepository;
        this.centreAdministrateurRepository = centreAdministrateurRepository;
        this.roleRepository = roleRepository;
        this.competenceRepository = competenceRepository;
        this.tentativeTestRepository = tentativeTestRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.auditLogService = auditLogService;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/centres/{centreId}/administrateur/{utilisateurId}")
    public java.util.Map<String, Object> definirAdministrateurCentre(
            @PathVariable UUID centreId, @PathVariable UUID utilisateurId, Authentication authentication) {
        centreRepository.findById(centreId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Centre introuvable"));
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Utilisateur introuvable"));
        if (utilisateur.getCitoyen() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT, "Le gestionnaire doit disposer d'un profil citoyen.");
        }
        centreAdministrateurRepository.findByUtilisateurId(utilisateurId)
                .filter(a -> !a.getCentreId().equals(centreId))
                .ifPresent(a -> { throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.CONFLICT, "Ce gestionnaire administre déjà un autre centre."); });
        UUID auteurId;
        try {
            auteurId = UUID.fromString(authentication.getName());
        } catch (RuntimeException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Identifiant de l'administrateur invalide.");
        }

        // Un centre dispose d'un gestionnaire principal dans ce parcours d'administration.
        centreAdministrateurRepository.findAllByCentreId(centreId).stream()
                .filter(a -> !a.getUtilisateurId().equals(utilisateurId))
                .toList().forEach(centreAdministrateurRepository::delete);
        CentreAdministrateur affectation = centreAdministrateurRepository.findByUtilisateurId(utilisateurId)
                .orElseGet(() -> new CentreAdministrateur(utilisateurId, centreId));
        affectation.setCentreId(centreId);
        affectation.setAttribueParUtilisateurId(auteurId);
        affectation.setAttribueLe(java.time.LocalDateTime.now());
        centreAdministrateurRepository.save(affectation);
        auditLogService.enregistrer("AFFECTER_GESTIONNAIRE_CENTRE", "Centre", centreId, "SUCCESS");
        return java.util.Map.of("centreId", centreId, "administrateurId", utilisateurId,
                "fonction", "GESTIONNAIRE_CENTRE");
    }

    @DeleteMapping("/centres/{centreId}/administrateur")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerAdministrateurCentre(@PathVariable UUID centreId) {
        centreRepository.findById(centreId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Centre introuvable"));
        List<CentreAdministrateur> affectations = centreAdministrateurRepository.findAllByCentreId(centreId);
        if (!affectations.isEmpty()) {
            centreAdministrateurRepository.deleteAll(affectations);
            auditLogService.enregistrer("RETIRER_GESTIONNAIRE_CENTRE", "Centre", centreId, "SUCCESS");
        }
        // Le retrait de cette fonction ne supprime aucun autre rôle du compte.
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(
                citoyenRepository.count(),
                organisationService.compterToutes(),
                demandeValidationRepository.count(),
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
                        citoyen.isDisponible()))
                .toList();
    }

    @GetMapping("/organisations")
    public List<OrganisationAdminResponse> organisations() {
        return organisationService.listerToutes().stream()
                .map(item -> new OrganisationAdminResponse(
                        item.id(), item.utilisateurId(), item.utilisateurEmail(),
                        item.nom(), item.description(), item.numeroRccm(),
                        item.statut() == null ? null : item.statut().name(), item.motifStatut()))
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
        OrganisationResponse updated = organisationService.modifierStatut(id, request.statut(), request.motif());
        auditLogService.enregistrer("ORGANISATION_STATUT_MODIFIE", "Organisation", id, "SUCCESS");
        return new OrganisationAdminResponse(
                updated.id(), updated.utilisateurId(), updated.utilisateurEmail(),
                updated.nom(), updated.description(), updated.numeroRccm(),
                updated.statut() == null ? null : updated.statut().name(), updated.motifStatut());
    }

    @GetMapping("/validations")
    public List<DemandeValidationAdminResponse> validations() {
        return demandeValidationRepository.findAll().stream()
                .map(item -> new DemandeValidationAdminResponse(
                        item.getId(), item.getMethode().getCode(),
                        "Compétence " + item.getCitoyenCompetence().getId(),
                        item.getStatut().name()))
                .toList();
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
        test.setVersion(request.version() == null || request.version().isBlank() ? "1" : request.version());
        test.setSeuilReussite(request.seuilReussite() == null ? BigDecimal.valueOf(60) : request.seuilReussite());
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
        if (tentativeTestRepository.existsByTestIdAndTermineeLeIsNotNull(id)) {
            throw new IllegalStateException("Ce test a déjà été utilisé. Créez une nouvelle version au lieu de modifier celle-ci.");
        }
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
                .map(centre -> {
                    var affectation = centreAdministrateurRepository.findAllByCentreId(centre.getId())
                            .stream().findFirst().orElse(null);
                    Utilisateur gestionnaire = affectation == null ? null
                            : utilisateurRepository.findById(affectation.getUtilisateurId()).orElse(null);
                    return new CentreAdminResponse(centre.getId(), centre.getNom(), centre.getDescription(),
                            centre.getAdresse(), centre.getTelephone(),
                            gestionnaire == null ? null : gestionnaire.getId(),
                            gestionnaire == null ? null : gestionnaire.getEmail());
                }).toList();
    }

    @GetMapping("/competences")
    public List<CompetenceAdminResponse> competences() {
        return competenceRepository.findAll().stream()
                .map(competence -> {
                    var metier = competence.getMetiers().stream()
                            .sorted(java.util.Comparator.comparing(odk.malicompetences.backend.competence.Metier::getNom))
                            .findFirst().orElse(null);
                    var secteur = metier == null ? null : metier.getSecteurActivite();
                    return new CompetenceAdminResponse(competence.getId(), competence.getNom(), competence.getDescription(),
                            metier == null ? null : metier.getNom(), secteur == null ? null : secteur.getNom());
                })
                .toList();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return utilisateurRepository.findAll().stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getTelephone(),
                        rolesAffiches(user),
                        user.getStatut() == StatutCompte.ACTIF,
                        user.getCreeLe()))
                .toList();
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdminUserResponse modifierRole(@PathVariable UUID id, @RequestBody ModifierRoleRequest request) {
        if (request.role() == null) throw new IllegalArgumentException("Le rôle est obligatoire.");
        if (request.role() == Role.CENTRE_ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fonction de gestionnaire de centre est gérée par l'affectation à un centre.");
        }
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        Role role = request.role();
        boolean attribuer = request.attribuer() == null || request.attribuer();
        if (attribuer && role == Role.CITOYEN && user.getCitoyen() == null)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Ce compte ne possède pas de profil citoyen.");
        if (attribuer && role == Role.ORGANISATION && user.getOrganisation() == null)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Ce compte ne possède pas de profil organisation.");
        if (!attribuer && role == Role.CITOYEN && user.getCitoyen() != null)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Le rôle Citoyen ne peut pas être retiré tant que le profil citoyen existe.");
        if (!attribuer && role == Role.ORGANISATION && user.getOrganisation() != null)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Le rôle Organisation ne peut pas être retiré tant que le profil organisation existe.");

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean dernierSuperAdminActif = role == Role.SUPER_ADMIN && user.getStatut() == StatutCompte.ACTIF
                && utilisateurRepository.findAll().stream().filter(u -> u.getStatut() == StatutCompte.ACTIF && possedeRole(u, Role.SUPER_ADMIN)).count() <= 1;
        if (!attribuer && role == Role.SUPER_ADMIN && dernierSuperAdminActif)
            throw new IllegalStateException("Impossible de retirer le rôle du dernier Super Admin actif.");
        if (!attribuer && role == Role.SUPER_ADMIN && authentication != null && user.getId().toString().equals(authentication.getName()))
            throw new IllegalStateException("Un Super Admin ne peut pas retirer son propre rôle.");

        RoleEntity roleEntity = roleRepository.findByCode(role.name())
                .orElseThrow(() -> new IllegalStateException("Rôle absent du référentiel : " + role.name()));
        if (attribuer && !possedeRole(user, role)) user.getRoles().add(roleEntity);
        if (!attribuer) user.getRoles().removeIf(r -> role.name().equals(r.getCode()));
        Utilisateur saved = utilisateurRepository.save(user);
        auditLogService.enregistrer("UTILISATEUR_ROLE_" + (attribuer ? "AJOUTE:" : "RETIRE:") + role, "Utilisateur", id, "SUCCESS");
        return toAdminUserResponse(saved);
    }

    private boolean possedeRole(Utilisateur user, Role role) {
        return user.getRoles() != null && user.getRoles().stream().anyMatch(r -> role.name().equals(r.getCode()));
    }

    private String rolesAffiches(Utilisateur user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) return "";
        return user.getRoles().stream().map(RoleEntity::getCode).sorted()
                .collect(java.util.stream.Collectors.joining(","));
    }

    private AdminUserResponse toAdminUserResponse(Utilisateur user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getTelephone(),
                rolesAffiches(user), user.getStatut() == StatutCompte.ACTIF, user.getCreeLe());
    }

    @PutMapping("/users/{id}/statut")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdminUserResponse modifierStatutUtilisateur(
            @PathVariable UUID id,
            @Valid @RequestBody ModifierStatutUtilisateurRequest request
    ) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        if (request.actif() == null) throw new IllegalArgumentException("Le champ actif est obligatoire.");
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && user.getId().toString().equals(authentication.getName())
                && possedeRole(user, Role.SUPER_ADMIN) && !request.actif()) {
            throw new IllegalStateException("Vous ne pouvez pas désactiver votre propre compte Super Admin.");
        }
        if (user.getStatut() == StatutCompte.ACTIF && !request.actif() && possedeRole(user, Role.SUPER_ADMIN)
                && utilisateurRepository.findAll().stream().filter(u -> possedeRole(u, Role.SUPER_ADMIN) && u.getStatut() == StatutCompte.ACTIF).count() <= 1) {
            throw new IllegalStateException("Impossible de désactiver le dernier Super Admin actif.");
        }
        user.setStatut(request.actif() ? StatutCompte.ACTIF : StatutCompte.SUSPENDU);
        Utilisateur saved = utilisateurRepository.save(user);
        auditLogService.enregistrer("UTILISATEUR_STATUT_MODIFIE:" + (saved.getStatut() == StatutCompte.ACTIF), "Utilisateur", id, "SUCCESS");
        return new AdminUserResponse(saved.getId(), saved.getEmail(), saved.getTelephone(),
                rolesAffiches(saved), saved.getStatut() == StatutCompte.ACTIF, saved.getCreeLe());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<AuditLog> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return auditLogService.lister(page, size);
    }

    public record ModifierStatutUtilisateurRequest(@NotNull Boolean actif) {}

    public record AdminDashboardResponse(
            long citoyens,
            long organisations,
            long validations,
            long tests,
            long besoins,
            long misesEnRelation
    ) {}

    public record CitoyenAdminResponse(UUID id, String nom, String prenom, boolean disponibilite) {}

    public record OrganisationAdminResponse(
            UUID id, UUID utilisateurId, String email, String nom, String description,
            String numeroRccm, String statut, String motifStatut
    ) {}

    public record DemandeValidationAdminResponse(UUID id, String type, String libelle, String statut) {}

    public record TestAdminResponse(
            UUID id, UUID competenceId, String titre, String description, String version,
            java.math.BigDecimal seuilReussite, String statut, List<QuestionAdminResponse> questions
    ) {}

    public record QuestionAdminResponse(
            UUID id, String libelle, String type, Integer ordre, java.math.BigDecimal ponderation,
            com.fasterxml.jackson.databind.JsonNode options, com.fasterxml.jackson.databind.JsonNode reponseAttendue
    ) {}

    public record CentreAdminResponse(
            UUID id, String nom, String description, String adresse, String telephone,
            UUID administrateurId, String administrateurEmail
    ) {}

    public record CompetenceAdminResponse(UUID id, String nom, String description, String metierNom, String secteurNom) {}
    public record ModifierRoleRequest(Role role, Boolean attribuer) {}
    public record ModifierStatutOrganisationRequest(@NotNull StatutOrganisation statut, @jakarta.validation.constraints.Size(max = 2000) String motif) {}
    public record AdminUserResponse(UUID id, String email, String telephone, String role, boolean actif, java.time.LocalDateTime dateCreation) {}

    public record TestAdminRequest(
            UUID competenceId, String titre, String description, Boolean actif, String version,
            java.math.BigDecimal seuilReussite, List<@Valid QuestionAdminRequest> questions
    ) {}

    public record QuestionAdminRequest(
            @NotBlank String texte, @NotNull TypeQuestion type, @NotNull Integer ordre,
            java.math.BigDecimal ponderation, com.fasterxml.jackson.databind.JsonNode options,
            @NotNull com.fasterxml.jackson.databind.JsonNode reponseAttendue
    ) {}

    private TestAdminResponse enregistrerTest(TestNumerique test, List<QuestionAdminRequest> questions) {
        TestNumerique saved = testRepository.save(test);
        if (questions != null) remplacerQuestions(saved, questions);
        return toAdminResponse(saved);
    }

    private void remplacerQuestions(TestNumerique test, List<QuestionAdminRequest> requests) {
        List<Question> anciennes = questionRepository.findByTestIdOrderByOrdreAsc(test.getId());
        questionRepository.deleteAll(anciennes);
        for (QuestionAdminRequest request : requests) {
            if (request.options() != null && !request.options().isArray()) throw new IllegalArgumentException("Les options doivent être un tableau JSON.");
            if (request.reponseAttendue() == null || request.reponseAttendue().isNull()) throw new IllegalArgumentException("La réponse attendue est obligatoire.");
            Question question = new Question();
            question.setTest(test); question.setTexte(request.texte()); question.setType(request.type()); question.setOrdre(request.ordre());
            question.setPonderation(request.ponderation() == null ? BigDecimal.ONE : request.ponderation());
            question.setOptionsJson(request.options()); question.setReponseAttendueJson(request.reponseAttendue());
            if (question.getPonderation().signum() <= 0) throw new IllegalArgumentException("La pondération doit être positive.");
            if (request.type() == TypeQuestion.TEXTE_LIBRE && !request.reponseAttendue().path("correctionManuelle").asBoolean(false)) throw new IllegalArgumentException("Une question libre doit être marquée pour correction manuelle.");
            if (request.type() != TypeQuestion.TEXTE_LIBRE && (request.options() == null || request.options().isEmpty() || !request.reponseAttendue().has("correct"))) throw new IllegalArgumentException("Une question à choix doit définir ses options et les identifiants des réponses correctes.");
            questionRepository.save(question);
        }
    }

    private void validerQuestionsQcm(List<QuestionAdminRequest> questions) {
        if (questions == null || questions.isEmpty()) throw new IllegalArgumentException("Ajoutez au moins une question au test.");
        for (QuestionAdminRequest q : questions) {
            if (q == null || q.texte() == null || q.texte().isBlank() || q.type() == null || q.ordre() == null || q.reponseAttendue() == null)
                throw new IllegalArgumentException("Chaque question doit avoir un énoncé, un type, un ordre et une réponse attendue.");
        }
    }

    private TestAdminResponse toAdminResponse(TestNumerique test) {
        List<QuestionAdminResponse> questions = questionRepository.findByTestIdOrderByOrdreAsc(test.getId()).stream()
                .map(q -> new QuestionAdminResponse(q.getId(), q.getTexte(), q.getType().name(), q.getOrdre(), q.getPonderation(), q.getOptionsJson(), q.getReponseAttendueJson())).toList();
        return new TestAdminResponse(test.getId(), test.getCompetence().getId(), test.getTitre(), test.getDescription(), test.getVersion(), test.getSeuilReussite(), test.isActif() ? "ACTIF" : "INACTIF", questions);
    }

}
