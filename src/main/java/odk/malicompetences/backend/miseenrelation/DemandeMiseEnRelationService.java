package odk.malicompetences.backend.miseenrelation;

import lombok.RequiredArgsConstructor;
import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.regle.ReglesMiseEnRelation;
import odk.malicompetences.backend.common.regle.Transitions;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.talent.StatutSuiviBesoinTalent;
import odk.malicompetences.backend.talent.SuiviBesoinTalent;
import odk.malicompetences.backend.talent.SuiviBesoinTalentRepository;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DemandeMiseEnRelationService {

    private final DemandeMiseEnRelationRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenRepository citoyenRepository;
    private final SuiviBesoinTalentRepository suiviRepository;
    private final DemandeMiseEnRelationMapper mapper;

    public DemandeMiseEnRelationResponse creerDemande(DemandeMiseEnRelationRequest request) {
        UUID utilisateurId = utilisateurConnecteId();
        Utilisateur demandeur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur demandeur introuvable."));

        boolean estOrganisation = aRole(demandeur, "ORGANISATION");
        boolean estCitoyen = aRole(demandeur, "CITOYEN");
        if (!estOrganisation && !estCitoyen) {
            throw new AccessDeniedException("Seuls une organisation ou un citoyen peuvent demander une mise en relation.");
        }
        if (estOrganisation && (demandeur.getOrganisation() == null
                || demandeur.getOrganisation().getStatut() != StatutOrganisation.APPROUVEE)) {
            throw new AccessDeniedException("Votre organisation doit être approuvée pour demander une mise en relation.");
        }
        if (estCitoyen && demandeur.getCitoyen() == null) {
            throw new AccessDeniedException("Aucun profil citoyen n'est associé à ce compte.");
        }

        SuiviBesoinTalent suivi = null;
        if (request.suiviBesoinTalentId() != null) {
            if (!estOrganisation) {
                throw new AccessDeniedException("Un citoyen ne peut pas initier une mise en relation via le suivi d'un besoin d'organisation.");
            }
            suivi = suiviRepository.findByIdForUpdate(request.suiviBesoinTalentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Suivi de talent introuvable."));
            if (suivi.getBesoin() == null || suivi.getBesoin().getOrganisation() == null
                    || !suivi.getBesoin().getOrganisation().getId().equals(demandeur.getOrganisation().getId())) {
                throw new AccessDeniedException("Ce suivi n'appartient pas à votre organisation.");
            }
            if (suivi.getBesoin().getStatut() != StatutBesoin.OUVERT
                    || (suivi.getBesoin().getDateLimite() != null
                    && suivi.getBesoin().getDateLimite().isBefore(LocalDate.now()))) {
                throw new IllegalStateException("La mise en relation exige un besoin ouvert et non expiré.");
            }
            if (suivi.getStatut() != StatutSuiviBesoinTalent.A_L_ETUDE) {
                throw new IllegalStateException("Ce suivi n'est pas disponible pour une nouvelle demande.");
            }
        }

        if (request.destinataireId() == null && suivi == null) {
            throw new IllegalArgumentException("Fournissez destinataireId ou suiviBesoinTalentId.");
        }

        Citoyen destinataire = suivi == null ? null : suivi.getCitoyen();
        if (request.destinataireId() != null) {
            Citoyen cibleDemandee = citoyenRepository.findByIdentifiantPublic(request.destinataireId())
                    .filter(Citoyen::isProfilPublic)
                    .orElseThrow(() -> new ResourceNotFoundException("Profil talent introuvable ou non public."));
            if (destinataire != null && !destinataire.getId().equals(cibleDemandee.getId())) {
                throw new IllegalArgumentException("Le destinataire ne correspond pas au talent du suivi.");
            }
            destinataire = cibleDemandee;
        }
        if (destinataire == null || !destinataire.isProfilPublic()) {
            throw new ResourceNotFoundException("Profil talent introuvable ou non public.");
        }
        if (demandeur.getCitoyen() != null && demandeur.getCitoyen().getId().equals(destinataire.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous envoyer une demande de mise en relation.");
        }

        if (suivi != null && demandeRepository.existsBySuiviBesoinTalent_IdAndStatut(
                suivi.getId(), StatutContact.EN_ATTENTE)) {
            throw new IllegalStateException("Une demande est déjà en attente pour ce suivi.");
        }
        if (suivi == null && demandeRepository.existsByDemandeur_IdAndDestinataire_IdAndSuiviBesoinTalentIsNullAndStatut(
                demandeur.getId(), destinataire.getId(), StatutContact.EN_ATTENTE)) {
            throw new IllegalStateException("Une demande directe est déjà en attente pour ce talent.");
        }

        LocalDateTime maintenant = LocalDateTime.now();
        var dernierRefus = demandeRepository.trouverDateDernierRefus(
                demandeur.getId(), destinataire.getId(), StatutContact.REFUSEE);
        if (dernierRefus.isPresent() && !ReglesMiseEnRelation.peutRedemanderApresRefus(
                dernierRefus.get(), maintenant, ReglesMiseEnRelation.DELAI_APRES_REFUS)) {
            throw new IllegalStateException("Une nouvelle demande est possible 30 jours après un refus.");
        }

        DemandeMiseEnRelation demande = demandeRepository.save(
                mapper.toEntity(request, demandeur, destinataire, suivi));

        if (suivi != null) {
            Transitions.SUIVI.verifier(suivi.getStatut(), StatutSuiviBesoinTalent.CONTACT_EN_COURS);
            suivi.setStatut(StatutSuiviBesoinTalent.CONTACT_EN_COURS);
        }
        return mapper.toResponse(demande);
    }

    @Transactional(readOnly = true)
    public DemandeMiseEnRelationResponse trouverParId(UUID id) {
        DemandeMiseEnRelation demande = trouverDemande(id);
        verifierPartieAutorisee(demande);
        return mapper.toResponse(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandeMiseEnRelationResponse> listerToutes() {
        UUID utilisateurId = utilisateurConnecteId();
        List<DemandeMiseEnRelation> demandes = estAdministrateur()
                ? demandeRepository.findAll()
                : demandeRepository.findToutesPourUtilisateur(utilisateurId);
        return demandes.stream().map(mapper::toResponse).toList();
    }

    public DemandeMiseEnRelationResponse repondre(UUID id, StatutContact nouveauStatut) {
        if (nouveauStatut != StatutContact.ACCEPTEE && nouveauStatut != StatutContact.REFUSEE) {
            throw new IllegalArgumentException("La réponse doit être ACCEPTEE ou REFUSEE.");
        }
        DemandeMiseEnRelation demande = trouverDemande(id);
        if (!demande.getDestinataire().getUtilisateur().getId().equals(utilisateurConnecteId())) {
            throw new AccessDeniedException("Seul le citoyen destinataire peut répondre à cette demande.");
        }
        if (demande.getStatut() != StatutContact.EN_ATTENTE) {
            throw new IllegalStateException("Cette demande a déjà reçu une réponse.");
        }

        SuiviBesoinTalent suivi = demande.getSuiviBesoinTalent();
        if (suivi != null) {
            StatutSuiviBesoinTalent statutSuivi = nouveauStatut == StatutContact.ACCEPTEE
                    ? StatutSuiviBesoinTalent.MISE_EN_RELATION : StatutSuiviBesoinTalent.A_L_ETUDE;
            Transitions.SUIVI.verifier(suivi.getStatut(), statutSuivi);
            suivi.setStatut(statutSuivi);
        }
        Transitions.MISE_EN_RELATION.verifier(demande.getStatut(), nouveauStatut);
        demande.setStatut(nouveauStatut);
        demande.setEnAttenteCle(null);
        demande.setDateReponse(LocalDateTime.now());
        return mapper.toResponse(demandeRepository.save(demande));
    }

    public DemandeMiseEnRelationResponse modifierDemande(UUID id, DemandeMiseEnRelationRequest request) {
        DemandeMiseEnRelation demande = trouverDemande(id);
        verifierDemandeur(demande);
        if (demande.getStatut() != StatutContact.EN_ATTENTE) {
            throw new IllegalStateException("Seule une demande en attente peut être modifiée.");
        }
        if (!Objects.equals(idOuNull(demande.getSuiviBesoinTalent()), request.suiviBesoinTalentId())
                || (request.destinataireId() != null
                && !request.destinataireId().equals(demande.getDestinataire().getIdentifiantPublic()))) {
            throw new IllegalArgumentException("Le destinataire et la source d'une demande existante ne peuvent pas être changés.");
        }
        demande.setMessage(request.message().trim());
        return mapper.toResponse(demandeRepository.save(demande));
    }

    /** Annule la demande sans supprimer son historique. */
    public void supprimerDemande(UUID id) {
        DemandeMiseEnRelation demande = trouverDemande(id);
        verifierDemandeur(demande);
        if (demande.getStatut() != StatutContact.EN_ATTENTE) {
            throw new IllegalStateException("Seule une demande en attente peut être annulée.");
        }
        SuiviBesoinTalent suivi = demande.getSuiviBesoinTalent();
        if (suivi != null) {
            Transitions.SUIVI.verifier(suivi.getStatut(), StatutSuiviBesoinTalent.A_L_ETUDE);
            suivi.setStatut(StatutSuiviBesoinTalent.A_L_ETUDE);
        }
        Transitions.MISE_EN_RELATION.verifier(demande.getStatut(), StatutContact.ANNULEE);
        demande.setStatut(StatutContact.ANNULEE);
        demande.setEnAttenteCle(null);
        demande.setDateReponse(LocalDateTime.now());
        demandeRepository.save(demande);
    }

    private DemandeMiseEnRelation trouverDemande(UUID id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de mise en relation introuvable."));
    }

    private void verifierPartieAutorisee(DemandeMiseEnRelation demande) {
        if (estAdministrateur()) return;
        UUID utilisateurId = utilisateurConnecteId();
        boolean demandeur = demande.getDemandeur().getId().equals(utilisateurId);
        boolean destinataire = demande.getDestinataire().getUtilisateur().getId().equals(utilisateurId);
        if (!demandeur && !destinataire) throw new AccessDeniedException("Accès refusé.");
    }

    private void verifierDemandeur(DemandeMiseEnRelation demande) {
        if (estAdministrateur()) return;
        if (!demande.getDemandeur().getId().equals(utilisateurConnecteId())) {
            throw new AccessDeniedException("Seul le demandeur peut modifier ou annuler cette demande.");
        }
    }

    private UUID idOuNull(SuiviBesoinTalent suivi) { return suivi == null ? null : suivi.getId(); }

    private boolean aRole(Utilisateur utilisateur, String code) {
        return utilisateur.getRoles() != null && utilisateur.getRoles().stream()
                .anyMatch(role -> code.equals(role.getCode()));
    }

    private UUID utilisateurConnecteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new AccessDeniedException("Authentification requise.");
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("Identifiant utilisateur invalide.");
        }
    }

    private boolean estAdministrateur() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }
}
