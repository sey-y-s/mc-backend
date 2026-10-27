package odk.malicompetences.backend.localisation;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.regle.ReglesLocalisation;
import odk.malicompetences.backend.localisation.LocalisationDtos.*;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Commune de référence du citoyen (consignes V1.2).
 * <ul>
 *   <li>Le GPS est facultatif et n'est utilisé qu'avec le consentement du citoyen.</li>
 *   <li>Une position reçue est convertie en commune puis oubliée : elle n'est ni enregistrée,
 *       ni journalisée, ni recopiée dans un message d'erreur.</li>
 *   <li>La commune de référence n'est jamais changée automatiquement : une différence produit
 *       une suggestion que le citoyen confirme ou refuse.</li>
 *   <li>Une seule suggestion en attente par citoyen, et un délai après un refus.</li>
 * </ul>
 */
@Service
@Transactional
public class LocalisationCitoyenService {

    private final CitoyenRepository citoyenRepository;
    private final CommuneRepository communeRepository;
    private final RechercheCommuneProche rechercheCommune;
    private final DemandeActualisationLocalisationRepository demandeRepository;
    private final int delaiApresRefusJours;
    private final int expirationJours;

    public LocalisationCitoyenService(
            CitoyenRepository citoyenRepository,
            CommuneRepository communeRepository,
            RechercheCommuneProche rechercheCommune,
            DemandeActualisationLocalisationRepository demandeRepository,
            @Value("${app.localisation.delai-apres-refus-jours:30}") int delaiApresRefusJours,
            @Value("${app.localisation.expiration-suggestion-jours:30}") int expirationJours
    ) {
        this.citoyenRepository = citoyenRepository;
        this.communeRepository = communeRepository;
        this.rechercheCommune = rechercheCommune;
        this.demandeRepository = demandeRepository;
        this.delaiApresRefusJours = delaiApresRefusJours;
        this.expirationJours = expirationJours;
    }

    @Transactional(readOnly = true)
    public MaLocalisation lire(UUID citoyenId) {
        Citoyen c = citoyen(citoyenId);
        Suggestion enAttente = demandeEnAttente(citoyenId);
        Commune commune = c.getCommune();
        return new MaLocalisation(
                commune == null ? null : commune.getId(),
                commune == null ? null : commune.getNom(),
                commune == null || commune.getRegion() == null ? null : commune.getRegion().getId(),
                commune == null || commune.getRegion() == null ? null : commune.getRegion().getNom(),
                c.getLocalisationSource(),
                c.getLocalisationConfirmeeLe(),
                c.isGpsConsenti(),
                c.getGpsConsentiLe(),
                enAttente
        );
    }

    /** Le citoyen choisit lui-même sa commune (profil, déménagement signalé). */
    public MaLocalisation declarer(UUID citoyenId, UUID communeId) {
        Citoyen c = citoyen(citoyenId);
        Commune commune = communeRepository.findById(communeId)
                .orElseThrow(() -> new IllegalArgumentException("Commune introuvable."));
        c.definirCommuneReference(commune, SourceLocalisation.DECLARATION);
        citoyenRepository.save(c);
        // Une suggestion en attente devient sans objet.
        demandeRepository.findFirstByCitoyen_IdAndStatut(citoyenId, StatutActualisation.EN_ATTENTE)
                .ifPresent(d -> cloturer(d, communeId.equals(d.getCommuneSuggeree().getId())
                        ? StatutActualisation.ACCEPTEE : StatutActualisation.EXPIREE));
        return lire(citoyenId);
    }

    public MaLocalisation consentement(UUID citoyenId, boolean consenti) {
        Citoyen c = citoyen(citoyenId);
        c.setGpsConsenti(consenti);
        c.setGpsConsentiLe(consenti ? LocalDateTime.now() : null);
        citoyenRepository.save(c);
        if (!consenti) {
            // Retrait du consentement : la suggestion issue du GPS n'est plus proposée.
            demandeRepository.findFirstByCitoyen_IdAndStatut(citoyenId, StatutActualisation.EN_ATTENTE)
                    .ifPresent(d -> cloturer(d, StatutActualisation.EXPIREE));
        }
        return lire(citoyenId);
    }

    /**
     * Détection ponctuelle : la position sert uniquement à trouver la commune la plus proche,
     * puis elle est abandonnée (variables locales). Rien n'est écrit si les communes sont identiques.
     */
    public Detection detecter(UUID citoyenId, PositionPonctuelle position) {
        Citoyen c = citoyen(citoyenId);
        if (!c.isGpsConsenti()) {
            return new Detection(ResultatDetection.CONSENTEMENT_REQUIS,
                    "Activez la détection de votre commune pour utiliser votre position.", null);
        }
        Commune detectee = rechercheCommune.trouver(position.latitude(), position.longitude()).orElse(null);
        UUID reference = c.getCommune() == null ? null : c.getCommune().getId();
        UUID detecteeId = detectee == null ? null : detectee.getId();
        LocalDateTime maintenant = LocalDateTime.now();
        Duration delai = Duration.ofDays(delaiApresRefusJours);

        // 1re passe, sans écriture : commune connue ? commune inchangée ?
        ReglesLocalisation.Decision premiere =
                ReglesLocalisation.evaluer(true, false, reference, detecteeId, null, maintenant, delai);
        if (premiere == ReglesLocalisation.Decision.COMMUNE_INCONNUE) {
            return new Detection(ResultatDetection.COMMUNE_INCONNUE,
                    "Votre commune n'a pas pu être déterminée. Vous pouvez la choisir vous-même.", null);
        }
        if (premiere == ReglesLocalisation.Decision.RIEN_A_FAIRE) {
            return new Detection(ResultatDetection.IDENTIQUE, "Votre commune de référence est à jour.", null);
        }
        // 2e passe : la commune a changé, on regarde les demandes existantes (les trop anciennes expirent d'abord).
        expirerSiTropAncienne(citoyenId);
        var enAttente = demandeRepository.findFirstByCitoyen_IdAndStatut(citoyenId, StatutActualisation.EN_ATTENTE);
        var dernierRefus = demandeRepository.findFirstByCitoyen_IdAndStatutOrderByDateReponseDesc(citoyenId, StatutActualisation.REFUSEE);
        ReglesLocalisation.Decision decision = ReglesLocalisation.evaluer(true, enAttente.isPresent(), reference, detecteeId,
                dernierRefus.map(DemandeActualisationLocalisation::getDateReponse).orElse(null), maintenant, delai);
        if (decision == ReglesLocalisation.Decision.DEJA_EN_ATTENTE) {
            return new Detection(ResultatDetection.SUGGESTION_EN_ATTENTE,
                    "Une proposition de changement de commune attend déjà votre réponse.", suggestion(enAttente.get()));
        }
        if (decision == ReglesLocalisation.Decision.DELAI_NON_ECOULE) {
            return new Detection(ResultatDetection.DELAI_EN_COURS,
                    "Vous avez récemment gardé votre commune : aucune nouvelle proposition pour le moment.", null);
        }
        DemandeActualisationLocalisation d = new DemandeActualisationLocalisation();
        d.setCitoyen(c);
        d.setCommuneSuggeree(detectee);
        d.setStatut(StatutActualisation.EN_ATTENTE);
        d.setDateCreation(LocalDateTime.now());
        d = demandeRepository.save(d);
        return new Detection(ResultatDetection.SUGGESTION_CREEE,
                "Vous semblez être à " + detectee.getNom() + ". Voulez-vous en faire votre commune de référence ?",
                suggestion(d));
    }

    /** Le citoyen confirme : la commune suggérée devient sa commune de référence. */
    public MaLocalisation accepter(UUID citoyenId, UUID demandeId) {
        DemandeActualisationLocalisation d = demandeDuCitoyen(citoyenId, demandeId);
        Citoyen c = d.getCitoyen();
        c.definirCommuneReference(d.getCommuneSuggeree(), SourceLocalisation.GPS_CONFIRMEE);
        citoyenRepository.save(c);
        cloturer(d, StatutActualisation.ACCEPTEE);
        return lire(citoyenId);
    }

    /** Le citoyen refuse (voyage, déplacement temporaire) : la commune de référence est conservée. */
    public MaLocalisation refuser(UUID citoyenId, UUID demandeId) {
        cloturer(demandeDuCitoyen(citoyenId, demandeId), StatutActualisation.REFUSEE);
        return lire(citoyenId);
    }

    // ----------------------------------------------------------------------------------------

    private void expirerSiTropAncienne(UUID citoyenId) {
        demandeRepository.findFirstByCitoyen_IdAndStatut(citoyenId, StatutActualisation.EN_ATTENTE)
                .filter(d -> d.getDateCreation().isBefore(LocalDateTime.now().minusDays(expirationJours)))
                .ifPresent(d -> cloturer(d, StatutActualisation.EXPIREE));
    }

    private void cloturer(DemandeActualisationLocalisation d, StatutActualisation statut) {
        d.setStatut(statut);
        d.setDateReponse(LocalDateTime.now());
        demandeRepository.saveAndFlush(d);
    }

    private DemandeActualisationLocalisation demandeDuCitoyen(UUID citoyenId, UUID demandeId) {
        DemandeActualisationLocalisation d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposition introuvable."));
        if (!d.getCitoyen().getId().equals(citoyenId)) {
            throw new AccessDeniedException("Cette proposition ne vous concerne pas.");
        }
        if (d.getStatut() != StatutActualisation.EN_ATTENTE) {
            throw new IllegalStateException("Vous avez déjà répondu à cette proposition.");
        }
        return d;
    }

    private Suggestion demandeEnAttente(UUID citoyenId) {
        return demandeRepository.findFirstByCitoyen_IdAndStatut(citoyenId, StatutActualisation.EN_ATTENTE)
                .map(this::suggestion).orElse(null);
    }

    private Suggestion suggestion(DemandeActualisationLocalisation d) {
        Commune commune = d.getCommuneSuggeree();
        return new Suggestion(d.getId(), commune.getId(), commune.getNom(),
                commune.getRegion() == null ? null : commune.getRegion().getNom(), d.getDateCreation());
    }

    private Citoyen citoyen(UUID id) {
        return citoyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable."));
    }
}
