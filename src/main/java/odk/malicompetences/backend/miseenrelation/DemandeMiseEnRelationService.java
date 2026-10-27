package odk.malicompetences.backend.miseenrelation;

import odk.malicompetences.backend.common.regle.ReglesMiseEnRelation;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.talent.SuiviBesoinTalent;
import odk.malicompetences.backend.talent.SuiviBesoinTalentRepository;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandeMiseEnRelationService {

    @org.springframework.beans.factory.annotation.Value("${app.mise-en-relation.delai-apres-refus-jours:30}")
    private int delaiApresRefusJours = 30;

    private final DemandeMiseEnRelationRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenRepository citoyenRepository;
    private final SuiviBesoinTalentRepository SuiviBesoinTalentRepository;
    private final DemandeMiseEnRelationMapper mapper;
    private final odk.malicompetences.backend.citoyen.ProfilCompletudeService profilCompletudeService;

    public DemandeMiseEnRelationService(
            DemandeMiseEnRelationRepository demandeRepository,
            UtilisateurRepository utilisateurRepository,
            CitoyenRepository citoyenRepository,
            SuiviBesoinTalentRepository SuiviBesoinTalentRepository,
            DemandeMiseEnRelationMapper mapper,
            odk.malicompetences.backend.citoyen.ProfilCompletudeService profilCompletudeService
    ) {
        this.demandeRepository = demandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.citoyenRepository = citoyenRepository;
        this.SuiviBesoinTalentRepository = SuiviBesoinTalentRepository;
        this.mapper = mapper;
        this.profilCompletudeService = profilCompletudeService;
    }

    // Créer une demande de mise en relation
    public DemandeMiseEnRelationResponse creerDemande(DemandeMiseEnRelationRequest request) {
        verifierProprietaire(request.demandeurId(), "ORGANISATION");
        Utilisateur demandeur = utilisateurRepository.findById(request.demandeurId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Utilisateur demandeur non trouvé avec l'id : " + request.demandeurId()));

        Citoyen destinataire = citoyenRepository.findById(request.destinataireId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Citoyen destinataire non trouvé avec l'id : " + request.destinataireId()));
        profilCompletudeService.exigerVisible(destinataire);

        SuiviBesoinTalent SuiviBesoinTalent = null;
        if (request.suiviBesoinTalentId() != null) {
            SuiviBesoinTalent = SuiviBesoinTalentRepository.findById(request.suiviBesoinTalentId())
                    .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Suivi de talent non trouvé avec l'id : " + request.suiviBesoinTalentId()));
            if (!SuiviBesoinTalent.getOrganisation().getId().equals(demandeur.getId())
                    || !SuiviBesoinTalent.getCitoyen().getId().equals(destinataire.getId())) {
                throw new IllegalArgumentException("Le suivi ne correspond pas à l'organisation et au citoyen de la demande");
            }
        }
        if (demandeur.getId().equals(destinataire.getId())) {
            throw new IllegalArgumentException("Un compte ne peut pas demander une mise en relation avec lui-même");
        }
        // Une seule demande en attente par suivi (garanti aussi en base) ou, sans suivi, par paire.
        boolean dejaEnAttente = SuiviBesoinTalent != null
                ? demandeRepository.existsBySuiviBesoinTalent_IdAndStatut(SuiviBesoinTalent.getId(), StatutContact.EN_ATTENTE)
                : demandeRepository.existsByDemandeur_IdAndDestinataire_IdAndStatut(demandeur.getId(), destinataire.getId(), StatutContact.EN_ATTENTE);
        if (dejaEnAttente) {
            throw new IllegalStateException("Une demande de mise en relation attend déjà la réponse de ce talent.");
        }
        // Après un refus, un délai est respecté avant une nouvelle demande au même talent.
        demandeRepository.findFirstByDemandeur_IdAndDestinataire_IdAndStatutOrderByDateReponseDesc(
                        demandeur.getId(), destinataire.getId(), StatutContact.REFUSEE)
                .ifPresent(d -> {
                    if (!ReglesMiseEnRelation.peutRedemanderApresRefus(
                            d.getDateReponse(), java.time.LocalDateTime.now(), java.time.Duration.ofDays(delaiApresRefusJours))) {
                        throw new IllegalStateException("Ce talent a décliné une demande récemment : une nouvelle demande sera possible après "
                                + delaiApresRefusJours + " jours.");
                    }
                });

        DemandeMiseEnRelation demande = mapper.toEntity(request, demandeur, destinataire, SuiviBesoinTalent);
        demande.setStatut(StatutContact.EN_ATTENTE);
        demande.setDateReponse(null);
        DemandeMiseEnRelation savedDemande = demandeRepository.save(demande);
        return mapper.toResponse(savedDemande);
    }

    // Consulter une demande par son ID
    @Transactional(readOnly = true)
    public DemandeMiseEnRelationResponse trouverParId(UUID id) {
        DemandeMiseEnRelation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Demande de mise en relation non trouvée avec l'id : " + id));
        if (!estAdministrateur()) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || (!demande.getDemandeur().getId().toString().equals(auth.getName()) && !demande.getDestinataire().getId().toString().equals(auth.getName()))) {
                throw new org.springframework.security.access.AccessDeniedException("Accès refusé");
            }
        }
        return mapper.toResponse(demande);
    }

    // Lister toutes les demandes
    @Transactional(readOnly = true)
    public List<DemandeMiseEnRelationResponse> listerToutes() {
        return demandeRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Demandes de l'utilisateur connecté
    @Transactional(readOnly = true)
    public List<DemandeMiseEnRelationResponse> listerMesDemandes() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        UUID moi = UUID.fromString(auth.getName());
        boolean citoyen = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CITOYEN"));
        List<DemandeMiseEnRelation> demandes = citoyen
                ? demandeRepository.findByDestinataire_IdOrderByDateDemandeDesc(moi)
                : demandeRepository.findByDemandeur_IdOrderByDateDemandeDesc(moi);
        return demandes.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    /** L'organisation retire une demande encore en attente. */
    public DemandeMiseEnRelationResponse annuler(UUID id) {
        DemandeMiseEnRelation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Demande de mise en relation introuvable"));
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !demande.getDemandeur().getId().toString().equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Cette demande n'a pas été envoyée par votre compte.");
        }
        if (demande.getStatut() != StatutContact.EN_ATTENTE) {
            throw new IllegalStateException("Seule une demande en attente peut être annulée.");
        }
        demande.setStatut(StatutContact.ANNULEE);
        demande.setDateReponse(java.time.LocalDateTime.now());
        return mapper.toResponse(demandeRepository.save(demande));
    }

    // Réponse du citoyen destinataire
    public DemandeMiseEnRelationResponse repondre(UUID id, StatutContact statut) {
        if (statut != StatutContact.ACCEPTEE && statut != StatutContact.REFUSEE) {
            throw new IllegalArgumentException("La réponse doit être ACCEPTEE ou REFUSEE");
        }
        DemandeMiseEnRelation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Demande de mise en relation introuvable"));
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !demande.getDestinataire().getId().toString().equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Cette demande ne vous est pas adressée.");
        }
        if (demande.getStatut() != StatutContact.EN_ATTENTE) {
            throw new IllegalStateException("Vous avez déjà répondu à cette demande.");
        }
        demande.setStatut(statut);
        demande.setDateReponse(java.time.LocalDateTime.now());
        return mapper.toResponse(demandeRepository.save(demande));
    }

    // Modifier ou mettre à jour le statut d'une demande
    public DemandeMiseEnRelationResponse modifierDemande(UUID id, DemandeMiseEnRelationRequest request) {
        DemandeMiseEnRelation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Demande de mise en relation non trouvée avec l'id : " + id));

        // Les transitions de statut passent par les opérations dédiées (réponse du citoyen, annulation) :
        // ici, seul le message peut être corrigé tant que la demande est en attente.
        if (request.statut() != null && request.statut() != demande.getStatut()) {
            throw new IllegalStateException("Le statut d'une demande ne se modifie pas directement.");
        }
        if (demande.getStatut() != StatutContact.EN_ATTENTE) {
            throw new IllegalStateException("Cette demande a déjà reçu une réponse.");
        }
        mapper.updateEntityFromRequest(request, demande);
        DemandeMiseEnRelation updatedDemande = demandeRepository.save(demande);
        return mapper.toResponse(updatedDemande);
    }

    // Supprimer une demande
    public void supprimerDemande(UUID id) {
        if (!demandeRepository.existsById(id)) {
            throw new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Impossible de supprimer, demande non trouvée avec l'id : " + id);
        }
        demandeRepository.deleteById(id);
    }

    /** Vérifie directement que l'utilisateur connecté possède la ressource. */
    private void verifierProprietaire(UUID proprietaireId, String roleAttendu) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean administrateur = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (administrateur) return;
        boolean bonRole = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleAttendu));
        boolean bonUtilisateur = false;
        if (authentication != null && authentication.getName() != null && proprietaireId != null) {
            try { bonUtilisateur = proprietaireId.equals(UUID.fromString(authentication.getName())); }
            catch (IllegalArgumentException ignored) { bonUtilisateur = false; }
        }
        if (!bonRole || !bonUtilisateur) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé : cette ressource ne vous appartient pas.");
        }
    }

    private boolean estAdministrateur() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}