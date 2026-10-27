package odk.malicompetences.backend.competence;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.audit.AuditLogService;
import odk.malicompetences.backend.besoin.BesoinCompetenceRepository;
import odk.malicompetences.backend.testnumerique.TestNumeriqueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/referentiel")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Transactional
public class ReferentielAdminController {
    private final SecteurActiviteRepository secteurRepository;
    private final MetierRepository metierRepository;
    private final CompetenceRepository competenceRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final BesoinCompetenceRepository besoinCompetenceRepository;
    private final TestNumeriqueRepository testRepository;
    private final AuditLogService auditLogService;

    public ReferentielAdminController(
            SecteurActiviteRepository secteurRepository,
            MetierRepository metierRepository,
            CompetenceRepository competenceRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            BesoinCompetenceRepository besoinCompetenceRepository,
            TestNumeriqueRepository testRepository,
            AuditLogService auditLogService
    ) {
        this.secteurRepository = secteurRepository;
        this.metierRepository = metierRepository;
        this.competenceRepository = competenceRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.besoinCompetenceRepository = besoinCompetenceRepository;
        this.testRepository = testRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/secteurs")
    @Transactional(readOnly = true)
    public List<SecteurResponse> secteurs() {
        return secteurRepository.findAll().stream()
                .map(s -> new SecteurResponse(s.getId(), s.getNom(), s.getDescription())).toList();
    }

    @PostMapping("/secteurs")
    @ResponseStatus(HttpStatus.CREATED)
    public SecteurResponse creerSecteur(@Valid @RequestBody SecteurRequest request) {
        SecteurActivite secteur = new SecteurActivite();
        secteur.setNom(request.nom().trim());
        secteur.setDescription(request.description());
        SecteurActivite saved = secteurRepository.save(secteur);
        auditLogService.enregistrer("REFERENTIEL_SECTEUR_CREE", "SecteurActivite", saved.getId(), "SUCCESS");
        return new SecteurResponse(saved.getId(), saved.getNom(), saved.getDescription());
    }

    @PutMapping("/secteurs/{id}")
    public SecteurResponse modifierSecteur(@PathVariable UUID id, @Valid @RequestBody SecteurRequest request) {
        SecteurActivite secteur = secteurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Secteur introuvable."));
        secteur.setNom(request.nom().trim());
        secteur.setDescription(request.description());
        SecteurActivite saved = secteurRepository.save(secteur);
        auditLogService.enregistrer("REFERENTIEL_SECTEUR_MODIFIE", "SecteurActivite", id, "SUCCESS");
        return new SecteurResponse(saved.getId(), saved.getNom(), saved.getDescription());
    }

    @DeleteMapping("/secteurs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerSecteur(@PathVariable UUID id) {
        SecteurActivite secteur = secteurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Secteur introuvable."));
        if (metierRepository.existsBySecteurActiviteId(id)) {
            throw new IllegalStateException("Ce secteur est utilisé par un ou plusieurs métiers.");
        }
        secteurRepository.delete(secteur);
        auditLogService.enregistrer("REFERENTIEL_SECTEUR_SUPPRIME", "SecteurActivite", id, "SUCCESS");
    }

    @GetMapping("/metiers")
    @Transactional(readOnly = true)
    public List<MetierResponse> metiers() {
        return metierRepository.findAll().stream().map(this::metierResponse).toList();
    }

    @PostMapping("/metiers")
    @ResponseStatus(HttpStatus.CREATED)
    public MetierResponse creerMetier(@Valid @RequestBody MetierRequest request) {
        SecteurActivite secteur = secteurRepository.findById(request.secteurId())
                .orElseThrow(() -> new IllegalArgumentException("Secteur introuvable."));
        Metier metier = new Metier();
        metier.setSecteurActivite(secteur);
        metier.setNom(request.nom().trim());
        metier.setDescription(request.description());
        Metier saved = metierRepository.save(metier);
        auditLogService.enregistrer("REFERENTIEL_METIER_CREE", "Metier", saved.getId(), "SUCCESS");
        return metierResponse(saved);
    }

    @PutMapping("/metiers/{id}")
    public MetierResponse modifierMetier(@PathVariable UUID id, @Valid @RequestBody MetierRequest request) {
        Metier metier = metierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Métier introuvable."));
        SecteurActivite secteur = secteurRepository.findById(request.secteurId())
                .orElseThrow(() -> new IllegalArgumentException("Secteur introuvable."));
        metier.setSecteurActivite(secteur);
        metier.setNom(request.nom().trim());
        metier.setDescription(request.description());
        Metier saved = metierRepository.save(metier);
        auditLogService.enregistrer("REFERENTIEL_METIER_MODIFIE", "Metier", id, "SUCCESS");
        return metierResponse(saved);
    }

    @DeleteMapping("/metiers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerMetier(@PathVariable UUID id) {
        Metier metier = metierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Métier introuvable."));
        if (competenceRepository.existsByMetierId(id)) {
            throw new IllegalStateException("Ce métier est utilisé par une ou plusieurs compétences.");
        }
        metierRepository.delete(metier);
        auditLogService.enregistrer("REFERENTIEL_METIER_SUPPRIME", "Metier", id, "SUCCESS");
    }

    @GetMapping("/competences")
    @Transactional(readOnly = true)
    public List<CompetenceAdminResponse> competences() {
        return competenceRepository.findAll().stream().map(this::competenceResponse).toList();
    }

    @PostMapping("/competences")
    @ResponseStatus(HttpStatus.CREATED)
    public CompetenceAdminResponse creerCompetence(@Valid @RequestBody CompetenceAdminRequest request) {
        Metier metier = metierRepository.findById(request.metierId())
                .orElseThrow(() -> new IllegalArgumentException("Métier introuvable."));
        Competence competence = new Competence();
        competence.setMetier(metier);
        competence.setNom(request.nom().trim());
        competence.setDescription(request.description());
        competence.setParcoursAutorises(request.parcoursPossibles());
        Competence saved = competenceRepository.save(competence);
        auditLogService.enregistrer("REFERENTIEL_COMPETENCE_CREE", "Competence", saved.getId(), "SUCCESS");
        return competenceResponse(saved);
    }

    @PutMapping("/competences/{id}")
    public CompetenceAdminResponse modifierCompetence(
            @PathVariable UUID id, @Valid @RequestBody CompetenceAdminRequest request) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compétence introuvable."));
        Metier metier = metierRepository.findById(request.metierId())
                .orElseThrow(() -> new IllegalArgumentException("Métier introuvable."));
        competence.setMetier(metier);
        competence.setNom(request.nom().trim());
        competence.setDescription(request.description());
        competence.setParcoursAutorises(request.parcoursPossibles());
        Competence saved = competenceRepository.save(competence);
        auditLogService.enregistrer("REFERENTIEL_COMPETENCE_MODIFIE", "Competence", id, "SUCCESS");
        return competenceResponse(saved);
    }

    @DeleteMapping("/competences/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerCompetence(@PathVariable UUID id) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compétence introuvable."));
        if (citoyenCompetenceRepository.existsByCompetence_Id(id)
                || besoinCompetenceRepository.existsByCompetence_Id(id)
                || testRepository.existsByCompetence_Id(id)) {
            throw new IllegalStateException("Cette compétence est utilisée et ne peut pas être supprimée.");
        }
        competenceRepository.delete(competence);
        auditLogService.enregistrer("REFERENTIEL_COMPETENCE_SUPPRIMEE", "Competence", id, "SUCCESS");
    }

    private MetierResponse metierResponse(Metier m) {
        return new MetierResponse(m.getId(), m.getSecteurActivite().getId(),
                m.getSecteurActivite().getNom(), m.getNom(), m.getDescription());
    }

    private CompetenceAdminResponse competenceResponse(Competence c) {
        return new CompetenceAdminResponse(c.getId(), c.getMetier().getId(), c.getMetier().getNom(),
                c.getMetier().getSecteurActivite().getId(), c.getMetier().getSecteurActivite().getNom(),
                c.getNom(), c.getDescription(), c.getParcoursAutorises());
    }

    public record SecteurRequest(@NotBlank @Size(max = 150) String nom, String description) {}
    public record SecteurResponse(UUID id, String nom, String description) {}
    public record MetierRequest(@NotNull UUID secteurId, @NotBlank @Size(max = 150) String nom, String description) {}
    public record MetierResponse(UUID id, UUID secteurId, String secteurNom, String nom, String description) {}
    public record CompetenceAdminRequest(@NotNull UUID metierId, @NotBlank @Size(max = 200) String nom, String description,
                                         /** Secteurs possibles ; vide = formel et informel. */
                                         List<ParcoursCompetence> parcoursPossibles) {}
    public record CompetenceAdminResponse(UUID id, UUID metierId, String metierNom, UUID secteurId,
                                          String secteurNom, String nom, String description,
                                          List<ParcoursCompetence> parcoursPossibles) {}
}
