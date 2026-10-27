package odk.malicompetences.backend.contestation;

import odk.malicompetences.backend.audit.AuditLogService;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.contestation.ContestationDtos.*;
import odk.malicompetences.backend.notification.NotificationRequest;
import odk.malicompetences.backend.notification.NotificationService;
import odk.malicompetences.backend.notification.TypeNotification;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.validation.Validation;
import odk.malicompetences.backend.validation.ValidationRepository;
import odk.malicompetences.backend.validation.ValidationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Contestations et demandes de correction (consignes V1.2).
 * Une contestation acceptée d'une décision crée une nouvelle décision dans la même transaction ;
 * l'ancienne décision reste intacte (historique immuable).
 */
@Service
@Transactional
public class ContestationService {

    /** Champs du profil dont le citoyen peut demander la correction. */
    public static final Set<String> CHAMPS_PROFIL = Set.of(
            "nom", "prenom", "sexe", "commune", "telephone", "email", "competence", "experience", "autre");

    private static final Set<StatutContestation> EN_COURS = EnumSet.of(StatutContestation.OUVERTE, StatutContestation.EN_EXAMEN);

    private final ContestationRepository repository;
    private final CitoyenRepository citoyenRepository;
    private final ValidationRepository validationRepository;
    private final ValidationService validationService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ContestationService(ContestationRepository repository,
                               CitoyenRepository citoyenRepository,
                               ValidationRepository validationRepository,
                               ValidationService validationService,
                               UtilisateurRepository utilisateurRepository,
                               NotificationService notificationService,
                               AuditLogService auditLogService) {
        this.repository = repository;
        this.citoyenRepository = citoyenRepository;
        this.validationRepository = validationRepository;
        this.validationService = validationService;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    public ContestationResponse creer(UUID citoyenId, NouvelleContestation request) {
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable."));
        Contestation c = new Contestation();
        c.setCitoyen(citoyen);
        c.setTypeObjet(request.typeObjet());
        c.setMotif(request.motif().trim());
        switch (request.typeObjet()) {
            case DECISION_VALIDATION -> {
                if (request.validationId() == null) {
                    throw new IllegalArgumentException("Indiquez la décision que vous contestez.");
                }
                Validation v = validationRepository.findById(request.validationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Décision introuvable."));
                if (!v.getCitoyenCompetence().getCitoyen().getId().equals(citoyenId)) {
                    throw new AccessDeniedException("Vous ne pouvez contester que les décisions qui vous concernent.");
                }
                if (v.getStatut() == StatutValidation.EN_ATTENTE) {
                    throw new IllegalStateException("Cette demande n'a pas encore reçu de décision.");
                }
                if (repository.existsByValidation_IdAndStatutIn(v.getId(), EN_COURS)) {
                    throw new IllegalStateException("Une contestation de cette décision est déjà en cours d'examen.");
                }
                c.setValidation(v);
            }
            case DONNEE_PROFIL -> {
                String champ = request.champConcerne() == null ? null : request.champConcerne().trim().toLowerCase();
                if (champ == null || champ.isEmpty()) {
                    throw new IllegalArgumentException("Indiquez l'information du profil à corriger.");
                }
                if (!CHAMPS_PROFIL.contains(champ)) {
                    throw new IllegalArgumentException("Information du profil inconnue : " + request.champConcerne());
                }
                c.setChampConcerne(champ);
            }
            case AUTRE -> c.setChampConcerne(request.champConcerne() == null || request.champConcerne().isBlank()
                    ? null : request.champConcerne().trim());
        }
        return ContestationResponse.from(repository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ContestationResponse> mesContestations(UUID citoyenId) {
        return repository.findByCitoyen_IdOrderByDateCreationDesc(citoyenId).stream()
                .map(ContestationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ContestationResponse> lister(boolean enCoursSeulement) {
        List<Contestation> liste = enCoursSeulement
                ? repository.findByStatutInOrderByDateCreationAsc(EN_COURS)
                : repository.findAllByOrderByDateCreationDesc();
        return liste.stream().map(ContestationResponse::from).toList();
    }

    public ContestationResponse traiter(UUID id, Traitement t, UUID adminId) {
        Contestation c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contestation introuvable."));
        if (!EN_COURS.contains(c.getStatut())) {
            throw new IllegalStateException("Cette contestation a déjà été traitée.");
        }
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));
        switch (t.statut()) {
            case OUVERTE -> throw new IllegalArgumentException("Choisissez « En examen », « Acceptée » ou « Rejetée ».");
            case EN_EXAMEN -> c.setStatut(StatutContestation.EN_EXAMEN);
            case REJETEE -> {
                if (t.reponse() == null || t.reponse().isBlank()) {
                    throw new IllegalArgumentException("Expliquez au citoyen pourquoi sa demande est rejetée.");
                }
                cloturer(c, StatutContestation.REJETEE, t.reponse(), admin);
            }
            case ACCEPTEE -> {
                if (c.getTypeObjet() == TypeObjetContestation.DECISION_VALIDATION) {
                    if (t.nouvelleDecision() == null) {
                        throw new IllegalArgumentException("Indiquez la nouvelle décision (approuvée ou rejetée).");
                    }
                    // Nouvelle décision, même transaction ; l'ancienne reste dans l'historique.
                    Validation nouvelle = validationService.decisionCorrective(
                            c.getValidation(), t.nouvelleDecision(),
                            t.reponse() == null || t.reponse().isBlank() ? "Décision corrigée après contestation." : t.reponse(),
                            t.niveauValide(), admin);
                    c.setNouvelleDecision(nouvelle);
                }
                cloturer(c, StatutContestation.ACCEPTEE, t.reponse(), admin);
            }
        }
        Contestation saved = repository.save(c);
        auditLogService.enregistrer("CONTESTATION_" + saved.getStatut(), "Contestation", saved.getId(), "SUCCESS");
        if (saved.getStatut() == StatutContestation.ACCEPTEE || saved.getStatut() == StatutContestation.REJETEE) {
            notifier(saved);
        }
        return ContestationResponse.from(saved);
    }

    private void cloturer(Contestation c, StatutContestation statut, String reponse, Utilisateur admin) {
        c.setStatut(statut);
        c.setReponse(reponse == null || reponse.isBlank() ? null : reponse.trim());
        c.setTraiteePar(admin);
        c.setDateTraitement(LocalDateTime.now());
    }

    private void notifier(Contestation c) {
        try {
            String titre = c.getStatut() == StatutContestation.ACCEPTEE ? "Votre demande a été acceptée" : "Votre demande a été examinée";
            String message = c.getStatut() == StatutContestation.ACCEPTEE
                    ? "Votre contestation a été acceptée." + (c.getNouvelleDecision() != null ? " Une nouvelle décision a été enregistrée." : "")
                    : "Votre contestation n'a pas été retenue : " + (c.getReponse() == null ? "" : c.getReponse());
            notificationService.creerEtEnvoyerNotification(new NotificationRequest(
                    titre, message, TypeNotification.VALIDATION, List.of(c.getCitoyen().getId())));
        } catch (RuntimeException ignore) {
            // La notification est un confort : son échec n'annule pas le traitement.
        }
    }
}
