package odk.malicompetences.backend.centre;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import odk.malicompetences.backend.audit.AuditLogService;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.evaluateur.EvaluateurCentre;
import odk.malicompetences.backend.evaluateur.EvaluateurCentreRepository;
import odk.malicompetences.backend.evaluateur.EvaluateurHabilitation;
import odk.malicompetences.backend.evaluateur.EvaluateurHabilitationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Agrément des centres (statut + compétences évaluables) et habilitations des évaluateurs.
 * L'agrément relève de l'administration nationale ; l'habilitation d'un évaluateur peut aussi
 * être réglée par l'administrateur de son centre, dans la limite des compétences agréées.
 */
@RestController
@Transactional
public class CentreAgrementController {

    private final CentreRepository centres;
    private final CentreCompetenceRepository agrements;
    private final CompetenceRepository competences;
    private final EvaluateurCentreRepository evaluateurs;
    private final EvaluateurHabilitationRepository habilitations;
    private final AuditLogService audit;

    public CentreAgrementController(CentreRepository centres, CentreCompetenceRepository agrements,
                                    CompetenceRepository competences, EvaluateurCentreRepository evaluateurs,
                                    EvaluateurHabilitationRepository habilitations, AuditLogService audit) {
        this.centres = centres;
        this.agrements = agrements;
        this.competences = competences;
        this.evaluateurs = evaluateurs;
        this.habilitations = habilitations;
        this.audit = audit;
    }

    public record CompetenceRef(UUID id, String nom) {
    }

    public record Agrement(UUID centreId, String centreNom, StatutCentre statut, List<CompetenceRef> competences) {
    }

    public record AgrementRequest(@NotNull StatutCentre statut, @NotNull List<UUID> competenceIds) {
    }

    public record Habilitation(UUID centreId, UUID citoyenId, boolean actif, LocalDate dateFin,
                               boolean toutesCompetencesAgreees, List<CompetenceRef> competences) {
    }

    public record HabilitationRequest(List<UUID> competenceIds, LocalDate dateFin) {
    }

    @GetMapping({"/api/v1/admin/centres/{id}/agrement"})
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CENTRE_ADMIN')")
    @Transactional(readOnly = true)
    public Agrement lireAgrement(@PathVariable UUID id, Authentication auth) {
        Centre centre = centre(id);
        verifierLecture(centre, auth);
        return agrement(centre);
    }

    @PutMapping({"/api/v1/admin/centres/{id}/agrement"})
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public Agrement modifierAgrement(@PathVariable UUID id, @Valid @RequestBody AgrementRequest request) {
        Centre centre = centre(id);
        centre.setStatut(request.statut());
        centres.save(centre);
        Set<UUID> voulues = new HashSet<>(request.competenceIds());
        Map<UUID, CentreCompetence> existants = new HashMap<>();
        for (CentreCompetence a : agrements.findByCentre_Id(id)) existants.put(a.getCompetence().getId(), a);
        // Les agréments retirés sont désactivés (historique conservé), les nouveaux sont créés.
        for (CentreCompetence a : existants.values()) {
            boolean garder = voulues.contains(a.getCompetence().getId());
            if (a.isActif() != garder) {
                a.setActif(garder);
                a.setDateFin(garder ? null : LocalDate.now());
                if (garder) a.setDateDebut(LocalDate.now());
                agrements.save(a);
            }
        }
        for (UUID competenceId : voulues) {
            if (existants.containsKey(competenceId)) continue;
            Competence competence = competences.findById(competenceId)
                    .orElseThrow(() -> new IllegalArgumentException("Compétence introuvable."));
            CentreCompetence a = new CentreCompetence();
            a.setCentre(centre);
            a.setCompetence(competence);
            a.setActif(true);
            a.setDateDebut(LocalDate.now());
            agrements.save(a);
        }
        audit.enregistrer("CENTRE_AGREMENT_MODIFIE:" + request.statut(), "Centre", id, "SUCCESS");
        return agrement(centre);
    }

    @GetMapping("/api/v1/centres/{centreId}/evaluateurs/{citoyenId}/habilitation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CENTRE_ADMIN')")
    @Transactional(readOnly = true)
    public Habilitation lireHabilitation(@PathVariable UUID centreId, @PathVariable UUID citoyenId, Authentication auth) {
        Centre centre = centre(centreId);
        verifierLecture(centre, auth);
        return habilitation(evaluateur(citoyenId, centreId));
    }

    @PutMapping("/api/v1/centres/{centreId}/evaluateurs/{citoyenId}/habilitation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','CENTRE_ADMIN')")
    public Habilitation modifierHabilitation(@PathVariable UUID centreId, @PathVariable UUID citoyenId,
                                             @RequestBody HabilitationRequest request, Authentication auth) {
        Centre centre = centre(centreId);
        verifierLecture(centre, auth);
        EvaluateurCentre e = evaluateur(citoyenId, centreId);
        e.setDateFinHabilitation(request.dateFin());
        evaluateurs.save(e);
        habilitations.deleteByEvaluateurCentre_Id(e.getId());
        habilitations.flush();
        if (request.competenceIds() != null) {
            Set<UUID> agreees = new HashSet<>();
            for (CentreCompetence a : agrements.findByCentre_Id(centreId)) {
                if (a.valableLe(LocalDate.now())) agreees.add(a.getCompetence().getId());
            }
            for (UUID competenceId : new LinkedHashSet<>(request.competenceIds())) {
                if (!agreees.contains(competenceId)) {
                    throw new IllegalArgumentException("Le centre n'est pas agréé pour l'une des compétences choisies.");
                }
                EvaluateurHabilitation h = new EvaluateurHabilitation();
                h.setEvaluateurCentre(e);
                h.setCompetence(competences.getReferenceById(competenceId));
                habilitations.save(h);
            }
        }
        audit.enregistrer("EVALUATEUR_HABILITATION_MODIFIEE", "EvaluateurCentre", e.getId(), "SUCCESS");
        return habilitation(e);
    }

    // ------------------------------------------------------------------

    private Agrement agrement(Centre centre) {
        List<CompetenceRef> liste = agrements.findByCentre_Id(centre.getId()).stream()
                .filter(a -> a.valableLe(LocalDate.now()))
                .map(a -> new CompetenceRef(a.getCompetence().getId(), a.getCompetence().getNom()))
                .sorted(Comparator.comparing(CompetenceRef::nom))
                .toList();
        return new Agrement(centre.getId(), centre.getNom(), centre.getStatut(), liste);
    }

    private Habilitation habilitation(EvaluateurCentre e) {
        List<CompetenceRef> liste = habilitations.findByEvaluateurCentre_Id(e.getId()).stream()
                .map(h -> new CompetenceRef(h.getCompetence().getId(), h.getCompetence().getNom()))
                .sorted(Comparator.comparing(CompetenceRef::nom))
                .toList();
        return new Habilitation(e.getCentre().getId(), e.getCitoyen().getId(), e.isActif(),
                e.getDateFinHabilitation(), liste.isEmpty(), liste);
    }

    private EvaluateurCentre evaluateur(UUID citoyenId, UUID centreId) {
        return evaluateurs.findByCitoyenIdAndCentreId(citoyenId, centreId)
                .orElseThrow(() -> new ResourceNotFoundException("Cet évaluateur n'est pas désigné dans ce centre."));
    }

    private Centre centre(UUID id) {
        return centres.findById(id).orElseThrow(() -> new ResourceNotFoundException("Centre introuvable."));
    }

    /** Un administrateur de centre n'agit que sur son propre centre. */
    private void verifierLecture(Centre centre, Authentication auth) {
        boolean national = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (national) return;
        if (centre.getAdministrateur() == null || !centre.getAdministrateur().getId().toString().equals(auth.getName())) {
            throw new AccessDeniedException("Vous ne gérez pas ce centre.");
        }
    }
}
