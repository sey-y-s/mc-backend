package odk.malicompetences.backend.opportunite;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.notification.NotificationRequest;
import odk.malicompetences.backend.notification.NotificationService;
import odk.malicompetences.backend.notification.TypeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Opportunités liées à une localité : alerte des citoyens de la localité à la publication,
 * candidatures ouvertes à tous, candidats locaux présentés en premier.
 */
@Service
@Transactional
public class CandidatureService {

    private static final Logger log = LoggerFactory.getLogger(CandidatureService.class);

    private final OpportuniteRepository opportuniteRepository;
    private final CandidatureOpportuniteRepository candidatureRepository;
    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final NotificationService notificationService;
    private final OpportuniteMapper mapper;

    public CandidatureService(OpportuniteRepository opportuniteRepository,
                              CandidatureOpportuniteRepository candidatureRepository,
                              CitoyenRepository citoyenRepository,
                              CitoyenCompetenceRepository citoyenCompetenceRepository,
                              NotificationService notificationService,
                              OpportuniteMapper mapper) {
        this.opportuniteRepository = opportuniteRepository;
        this.candidatureRepository = candidatureRepository;
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    // ----- Proximité -----

    static String proximite(Opportunite op, Citoyen c) {
        if (op.getRegion() == null && op.getCommune() == null) return "NATIONALE";
        var communeCitoyen = c == null ? null : c.getCommune();
        if (communeCitoyen == null) return "AUTRE";
        if (op.getCommune() != null) {
            if (op.getCommune().getId().equals(communeCitoyen.getId())) return "COMMUNE";
            var regionOp = op.getCommune().getRegion();
            return regionOp != null && communeCitoyen.getRegion() != null
                    && regionOp.getId().equals(communeCitoyen.getRegion().getId()) ? "REGION" : "AUTRE";
        }
        return communeCitoyen.getRegion() != null && op.getRegion().getId().equals(communeCitoyen.getRegion().getId())
                ? "REGION" : "AUTRE";
    }

    private static int rang(String proximite) {
        return switch (proximite) {
            case "COMMUNE" -> 0;
            case "REGION" -> 1;
            case "NATIONALE" -> 2;
            default -> 3;
        };
    }

    /** Opportunités visibles pour un citoyen : celles de sa commune puis de sa région d'abord. */
    @Transactional(readOnly = true)
    public List<OpportunitePourMoiResponse> pourMoi(UUID citoyenId) {
        Citoyen c = citoyenRepository.findById(citoyenId).orElse(null);
        return opportuniteRepository.findVisibles(StatutOpportunite.PUBLIEE, LocalDate.now()).stream()
                .map(op -> {
                    var candidature = candidatureRepository.findByOpportuniteIdAndCitoyenId(op.getId(), citoyenId);
                    return new OpportunitePourMoiResponse(mapper.toResponse(op), proximite(op, c),
                            candidature.isPresent(), candidature.map(CandidatureOpportunite::getStatut).orElse(null));
                })
                .sorted(Comparator.comparingInt((OpportunitePourMoiResponse o) -> rang(o.proximite()))
                        .thenComparing(o -> o.opportunite().datePublication(), Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    // ----- Candidatures -----

    public CandidatureResponse postuler(UUID opportuniteId, UUID citoyenId, CandidatureRequest request) {
        Opportunite op = opportuniteRepository.findVisibleById(opportuniteId, StatutOpportunite.PUBLIEE, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunité introuvable ou close."));
        Citoyen c = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable."));
        if (candidatureRepository.existsByOpportuniteIdAndCitoyenId(opportuniteId, citoyenId)) {
            throw new IllegalStateException("Vous avez déjà postulé à cette opportunité.");
        }
        CandidatureOpportunite cand = new CandidatureOpportunite();
        cand.setOpportunite(op);
        cand.setCitoyen(c);
        cand.setMessage(request == null || request.message() == null || request.message().isBlank() ? null : request.message().trim());
        cand.setStatut(StatutCandidature.ENVOYEE);
        String p = proximite(op, c);
        cand.setLocale("COMMUNE".equals(p) || "REGION".equals(p));
        cand.setDateCandidature(LocalDateTime.now());
        return versReponse(candidatureRepository.save(cand));
    }

    @Transactional(readOnly = true)
    public List<CandidatureResponse> mesCandidatures(UUID citoyenId) {
        return candidatureRepository.findByCitoyenIdOrderByDateCandidatureDesc(citoyenId).stream().map(this::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CandidatureResponse> candidaturesDe(UUID opportuniteId) {
        if (!opportuniteRepository.existsById(opportuniteId)) {
            throw new ResourceNotFoundException("Opportunité introuvable.");
        }
        return candidatureRepository.findByOpportuniteIdOrderByLocaleDescDateCandidatureAsc(opportuniteId).stream()
                .map(this::versReponse).toList();
    }

    public CandidatureResponse changerStatut(UUID candidatureId, StatutCandidature statut) {
        CandidatureOpportunite cand = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature introuvable."));
        cand.setStatut(statut);
        CandidatureOpportunite saved = candidatureRepository.save(cand);
        if (statut == StatutCandidature.RETENUE) {
            notifier(List.of(cand.getCitoyen().getId()), "Candidature retenue",
                    "Votre candidature à « " + cand.getOpportunite().getTitre() + " » a été retenue. Vous serez contacté prochainement.");
        }
        return versReponse(saved);
    }

    private CandidatureResponse versReponse(CandidatureOpportunite c) {
        Citoyen cit = c.getCitoyen();
        var commune = cit.getCommune();
        var competences = citoyenCompetenceRepository.findByCitoyenId(cit.getId());
        return new CandidatureResponse(
                c.getId(), c.getOpportunite().getId(), c.getOpportunite().getTitre(), cit.getId(),
                (cit.getPrenom() == null ? "" : cit.getPrenom() + " ") + (cit.getNom() == null ? "" : cit.getNom()),
                cit.getUtilisateur() != null ? cit.getUtilisateur().getTelephone() : null,
                commune != null ? commune.getNom() : null,
                commune != null && commune.getRegion() != null ? commune.getRegion().getNom() : null,
                c.isLocale(), competences.size(),
                (int) competences.stream().filter(cc -> cc.getEtat() == EtatCompetence.VALIDEE).count(),
                c.getMessage(), c.getStatut(), c.getDateCandidature());
    }

    // ----- Alerte des citoyens de la localité -----

    /** À la première publication, prévient les citoyens de la commune (ou de la région) concernée. */
    public int diffuser(UUID opportuniteId) {
        Opportunite op = opportuniteRepository.findById(opportuniteId).orElse(null);
        if (op == null || op.getStatut() != StatutOpportunite.PUBLIEE || op.getDateDiffusion() != null) return 0;
        if (op.getRegion() == null && op.getCommune() == null) return 0;
        List<Citoyen> cibles = op.getCommune() != null
                ? citoyenRepository.findByCommune_Id(op.getCommune().getId())
                : citoyenRepository.findByCommune_Region_Id(op.getRegion().getId());
        op.setDateDiffusion(LocalDateTime.now());
        opportuniteRepository.save(op);
        if (cibles.isEmpty()) return 0;
        String lieu = op.getCommune() != null ? op.getCommune().getNom() : "votre région";
        notifier(cibles.stream().map(Citoyen::getId).toList(),
                "Nouvelle opportunité près de chez vous",
                "« " + op.getTitre() + " » concerne " + lieu + ". Les habitants de la localité sont prioritaires : postulez dès maintenant.");
        return cibles.size();
    }

    private void notifier(List<UUID> destinataires, String titre, String message) {
        try {
            notificationService.creerEtEnvoyerNotification(new NotificationRequest(titre, message, TypeNotification.OPPORTUNITE, destinataires));
        } catch (RuntimeException e) {
            // L'alerte ne doit jamais empêcher la publication ou la décision.
            log.warn("Notification non envoyée : {}", e.getMessage());
        }
    }
}
