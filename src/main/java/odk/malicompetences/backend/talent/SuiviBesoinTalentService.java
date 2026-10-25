package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.regle.Transitions;
import odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SuiviBesoinTalentService {

    private final SuiviBesoinTalentRepository suiviRepository;
    private final OrganisationRepository organisationRepository;
    private final BesoinRepository besoinRepository;
    private final CitoyenRepository citoyenRepository;
    private final SuiviBesoinTalentMapper mapper;
    private final DemandeMiseEnRelationRepository demandeRepository;

    public SuiviBesoinTalentService(
            SuiviBesoinTalentRepository suiviRepository,
            OrganisationRepository organisationRepository,
            BesoinRepository besoinRepository,
            CitoyenRepository citoyenRepository,
            SuiviBesoinTalentMapper mapper,
            DemandeMiseEnRelationRepository demandeRepository
    ) {
        this.suiviRepository = suiviRepository;
        this.organisationRepository = organisationRepository;
        this.besoinRepository = besoinRepository;
        this.citoyenRepository = citoyenRepository;
        this.mapper = mapper;
        this.demandeRepository = demandeRepository;
    }

    public SuiviBesoinTalentResponse creerSuivi(
            SuiviBesoinTalentRequest request
    ) {
        if (request == null || request.besoinId() == null
                || request.citoyenId() == null) {
            throw new IllegalArgumentException(
                    "Le besoin et l'identifiant public du talent sont obligatoires."
            );
        }

        Organisation organisation = organisationConnectee();

        if (organisation.getStatut() != StatutOrganisation.APPROUVEE) {
            throw new AccessDeniedException(
                    "L'organisation doit être approuvée pour suivre un talent."
            );
        }

        Besoin besoin = besoinRepository.findById(request.besoinId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Besoin introuvable.")
                );

        verifierProprietaireDuBesoin(besoin, organisation);

        if (besoin.getStatut() != StatutBesoin.OUVERT) {
            throw new IllegalStateException(
                    "Seul un besoin ouvert peut recevoir un suivi."
            );
        }

        Citoyen citoyen = citoyenRepository
                .findByIdentifiantPublic(request.citoyenId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profil talent introuvable.")
                );

        if (!citoyen.isProfilPublic()) {
            throw new AccessDeniedException(
                    "Ce profil n'est pas accessible aux organisations."
            );
        }

        if (suiviRepository.existsByBesoin_IdAndCitoyen_Id(
                besoin.getId(), citoyen.getId())) {
            throw new IllegalStateException(
                    "Ce talent est déjà suivi pour ce besoin."
            );
        }

        // Le statut initial et le matching sont définis côté serveur.
        SuiviBesoinTalent suivi = mapper.toEntity(request, besoin, citoyen);
        suivi.setStatut(StatutSuiviBesoinTalent.A_L_ETUDE);
        suivi.setScoreCorrespondance(null);
        suivi.setDetailsCorrespondance(null);

        return mapper.toResponse(suiviRepository.save(suivi), true);
    }

    @Transactional(readOnly = true)
    public SuiviBesoinTalentResponse trouverParId(UUID id) {
        SuiviBesoinTalent suivi = suiviRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Suivi de talent introuvable.")
                );

        verifierAccesAuSuivi(suivi);

        boolean noteVisible = estAdministrateur()
                || estOrganisationProprietaire(suivi);

        return mapper.toResponse(suivi, noteVisible);
    }

    @Transactional(readOnly = true)
    public List<SuiviBesoinTalentResponse> listerParBesoin(UUID besoinId) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Besoin introuvable.")
                );

        if (!estAdministrateur()) {
            Organisation organisation = organisationConnectee();
            verifierProprietaireDuBesoin(besoin, organisation);
        }

        return suiviRepository.findAllByBesoin_Id(besoinId).stream()
                // Une organisation ne doit jamais retrouver un profil privé.
                .filter(suivi -> estAdministrateur()
                        || suivi.getCitoyen().isProfilPublic())
                .map(suivi -> mapper.toResponse(suivi, true))
                .toList();
    }

    public SuiviBesoinTalentResponse modifierSuivi(
            UUID id,
            SuiviBesoinTalentRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Les données de modification sont obligatoires."
            );
        }

        SuiviBesoinTalent suivi = suiviRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Suivi de talent introuvable.")
                );

        Organisation organisation = organisationConnectee();
        verifierProprietaireDuBesoin(suivi.getBesoin(), organisation);

        if (!suivi.getCitoyen().isProfilPublic()) {
            throw new AccessDeniedException(
                    "Ce profil n'est plus accessible aux organisations."
            );
        }

        // Le mapper ne modifie que la note interne.
        mapper.updateEntity(request, suivi);

        // Le statut est piloté par la mise en relation.
        // L'organisation peut seulement archiver ou désarchiver.
        if (request.statut() != null
                && request.statut() != suivi.getStatut()) {

            StatutSuiviBesoinTalent nouveauStatut = request.statut();

            if (nouveauStatut != StatutSuiviBesoinTalent.ARCHIVE
                    && !(suivi.getStatut() == StatutSuiviBesoinTalent.ARCHIVE
                    && nouveauStatut == StatutSuiviBesoinTalent.A_L_ETUDE)) {
                throw new IllegalArgumentException(
                        "Seuls l'archivage et le retour à l'étude sont autorisés ici."
                );
            }

            Transitions.SUIVI.verifier(suivi.getStatut(), nouveauStatut);
            suivi.setStatut(nouveauStatut);
        }

        return mapper.toResponse(suiviRepository.save(suivi), true);
    }

    public void supprimerSuivi(UUID id) {
        SuiviBesoinTalent suivi = suiviRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Suivi de talent introuvable.")
                );

        Organisation organisation = organisationConnectee();
        verifierProprietaireDuBesoin(suivi.getBesoin(), organisation);

        // Conserver les suivis qui possèdent un historique de mise en relation.
        if (demandeRepository.existsBySuiviBesoinTalent_Id(id)) {
            throw new IllegalStateException(
                    "Ce suivi possède un historique de mise en relation. "
                            + "Archivez-le plutôt que de le supprimer."
            );
        }

        if (suivi.getStatut() != StatutSuiviBesoinTalent.A_L_ETUDE
                && suivi.getStatut() != StatutSuiviBesoinTalent.ARCHIVE) {
            throw new IllegalStateException(
                    "Seul un suivi sans mise en relation peut être supprimé."
            );
        }

        suiviRepository.delete(suivi);
    }

    private void verifierAccesAuSuivi(SuiviBesoinTalent suivi) {
        if (estAdministrateur()) {
            return;
        }

        Authentication auth = authentication();
        UUID utilisateurId = utilisateurConnecteId();

        if (aRole(auth, "ROLE_ORGANISATION")) {
            Organisation organisation = organisationRepository
                    .findByUtilisateur_Id(utilisateurId)
                    .orElseThrow(() -> new AccessDeniedException(
                            "Aucun profil organisation associé à ce compte."
                    ));

            verifierProprietaireDuBesoin(suivi.getBesoin(), organisation);

            if (!suivi.getCitoyen().isProfilPublic()) {
                throw new AccessDeniedException(
                        "Ce profil n'est plus accessible aux organisations."
                );
            }
            return;
        }

        if (aRole(auth, "ROLE_CITOYEN")) {
            Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                    .orElseThrow(() -> new AccessDeniedException(
                            "Aucun profil citoyen associé à ce compte."
                    ));

            if (citoyen.getId().equals(suivi.getCitoyen().getId())) {
                return;
            }
        }

        throw new AccessDeniedException("Accès refusé.");
    }

    private Organisation organisationConnectee() {
        if (!aRole(authentication(), "ROLE_ORGANISATION")) {
            throw new AccessDeniedException(
                    "Cette action est réservée aux organisations."
            );
        }

        Organisation organisation = organisationRepository
                .findByUtilisateur_Id(utilisateurConnecteId())
                .orElseThrow(() -> new AccessDeniedException(
                        "Aucun profil organisation associé à ce compte."
                ));

        if (organisation.getStatut() == StatutOrganisation.SUSPENDUE
                || organisation.getStatut() == StatutOrganisation.REJETEE) {
            throw new AccessDeniedException(
                    "Cette organisation n'est pas autorisée à utiliser cette fonctionnalité."
            );
        }

        return organisation;
    }

    private void verifierProprietaireDuBesoin(
            Besoin besoin,
            Organisation organisation
    ) {
        if (besoin.getOrganisation() == null
                || !besoin.getOrganisation().getId().equals(organisation.getId())) {
            throw new AccessDeniedException(
                    "Ce besoin n'appartient pas à votre organisation."
            );
        }
    }

    private boolean estOrganisationProprietaire(SuiviBesoinTalent suivi) {
        if (!aRole(authentication(), "ROLE_ORGANISATION")) {
            return false;
        }

        Organisation organisation = organisationRepository
                .findByUtilisateur_Id(utilisateurConnecteId())
                .orElse(null);

        return organisation != null
                && suivi.getBesoin().getOrganisation().getId()
                .equals(organisation.getId());
    }

    private boolean estAdministrateur() {
        Authentication auth = authentication();
        return aRole(auth, "ROLE_ADMIN") || aRole(auth, "ROLE_SUPER_ADMIN");
    }

    private boolean aRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private UUID utilisateurConnecteId() {
        Authentication auth = authentication();

        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentification requise.");
        }

        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException(
                    "L'identifiant du compte connecté est invalide."
            );
        }
    }
}