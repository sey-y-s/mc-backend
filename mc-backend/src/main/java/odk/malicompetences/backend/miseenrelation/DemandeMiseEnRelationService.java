package odk.malicompetences.backend.miseenrelation;

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

    private final DemandeMiseEnRelationRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenRepository citoyenRepository;
    private final SuiviBesoinTalentRepository SuiviBesoinTalentRepository;
    private final DemandeMiseEnRelationMapper mapper;

    public DemandeMiseEnRelationService(
            DemandeMiseEnRelationRepository demandeRepository,
            UtilisateurRepository utilisateurRepository,
            CitoyenRepository citoyenRepository,
            SuiviBesoinTalentRepository SuiviBesoinTalentRepository,
            DemandeMiseEnRelationMapper mapper
    ) {
        this.demandeRepository = demandeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.citoyenRepository = citoyenRepository;
        this.SuiviBesoinTalentRepository = SuiviBesoinTalentRepository;
        this.mapper = mapper;
    }

    // Créer une demande de mise en relation
    public DemandeMiseEnRelationResponse creerDemande(DemandeMiseEnRelationRequest request) {
        verifierProprietaire(request.demandeurId(), "ORGANISATION");
        Utilisateur demandeur = utilisateurRepository.findById(request.demandeurId())
                .orElseThrow(() -> new RuntimeException("Utilisateur demandeur non trouvé avec l'id : " + request.demandeurId()));

        Citoyen destinataire = citoyenRepository.findById(request.destinataireId())
                .orElseThrow(() -> new RuntimeException("Citoyen destinataire non trouvé avec l'id : " + request.destinataireId()));

        SuiviBesoinTalent SuiviBesoinTalent = null;
        if (request.SuiviBesoinTalentId() != null) {
            SuiviBesoinTalent = SuiviBesoinTalentRepository.findById(request.SuiviBesoinTalentId())
                    .orElseThrow(() -> new RuntimeException("Suivi de talent non trouvé avec l'id : " + request.SuiviBesoinTalentId()));
            if (!SuiviBesoinTalent.getOrganisation().getId().equals(demandeur.getId())
                    || !SuiviBesoinTalent.getCitoyen().getId().equals(destinataire.getId())) {
                throw new IllegalArgumentException("Le suivi ne correspond pas à l'organisation et au citoyen de la demande");
            }
        }
        if (demandeur.getId().equals(destinataire.getId())) {
            throw new IllegalArgumentException("Un compte ne peut pas demander une mise en relation avec lui-même");
        }

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
                .orElseThrow(() -> new RuntimeException("Demande de mise en relation non trouvée avec l'id : " + id));
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

    // Modifier ou mettre à jour le statut d'une demande
    public DemandeMiseEnRelationResponse modifierDemande(UUID id, DemandeMiseEnRelationRequest request) {
        DemandeMiseEnRelation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande de mise en relation non trouvée avec l'id : " + id));

        mapper.updateEntityFromRequest(request, demande);
        DemandeMiseEnRelation updatedDemande = demandeRepository.save(demande);
        return mapper.toResponse(updatedDemande);
    }

    // Supprimer une demande
    public void supprimerDemande(UUID id) {
        if (!demandeRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer, demande non trouvée avec l'id : " + id);
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