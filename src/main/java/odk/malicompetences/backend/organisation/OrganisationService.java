package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.regle.Transitions;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrganisationService {
    private final OrganisationRepository organisationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final OrganisationMapper organisationMapper;

    public OrganisationService(OrganisationRepository organisationRepository,
                               UtilisateurRepository utilisateurRepository,
                               OrganisationMapper organisationMapper) {
        this.organisationRepository = organisationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.organisationMapper = organisationMapper;
    }

    public OrganisationResponse creerOrganisation(UUID utilisateurId, OrganisationRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        boolean roleOrganisation = utilisateur.getRoles() != null && utilisateur.getRoles().stream()
                .anyMatch(role -> "ORGANISATION".equals(role.getCode()));
        if (!roleOrganisation) throw new IllegalArgumentException("Le compte associé doit avoir le rôle ORGANISATION.");
        if (organisationRepository.existsByUtilisateur_Id(utilisateur.getId()))
            throw new IllegalStateException("Un profil organisation existe déjà pour ce compte.");

        Organisation organisation = organisationMapper.toEntity(request, utilisateur);
        // Un compte ne peut pas s'auto-approuver lors de la création.
        organisation.setStatut(StatutOrganisation.EN_ATTENTE);
        return organisationMapper.toResponse(organisationRepository.save(organisation));
    }

    @Transactional(readOnly = true)
    public OrganisationResponse trouverParId(UUID id) {
        return organisationRepository.findById(id).map(organisationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation introuvable."));
    }

    @Transactional(readOnly = true)
    public OrganisationResponse trouverMonProfil(UUID utilisateurId) {
        return organisationRepository.findByUtilisateur_Id(utilisateurId).map(organisationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Profil organisation introuvable."));
    }

    @Transactional(readOnly = true)
    public long compterToutes() { return organisationRepository.count(); }

    @Transactional(readOnly = true)
    public List<OrganisationResponse> listerToutes() {
        return organisationRepository.findAll().stream().map(organisationMapper::toResponse).toList();
    }

    public OrganisationResponse modifierOrganisation(UUID id, OrganisationRequest request) {
        Organisation org = charger(id);
        organisationMapper.updateEntity(request, org);
        return organisationMapper.toResponse(organisationRepository.save(org));
    }

    public OrganisationResponse modifierMonProfil(UUID utilisateurId, OrganisationProfilRequest request) {
        Organisation organisation = organisationRepository.findByUtilisateur_Id(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil organisation introuvable."));
        verifierProprietaire(organisation);
        organisation.setNom(request.nom().trim());
        organisation.setDescription(request.description());
        organisation.setNumeroRccm(request.numeroRccm() == null || request.numeroRccm().isBlank()
                ? null : request.numeroRccm().trim());
        if (organisation.getStatut() == StatutOrganisation.REJETEE) {
            Transitions.ORGANISATION.verifier(StatutOrganisation.REJETEE, StatutOrganisation.EN_ATTENTE);
            organisation.setStatut(StatutOrganisation.EN_ATTENTE);
            organisation.setMotifStatut(null);
        }
        return organisationMapper.toResponse(organisationRepository.save(organisation));
    }

    public OrganisationResponse modifierStatut(UUID id, StatutOrganisation statut) {
        return modifierStatut(id, statut, null);
    }

    public OrganisationResponse modifierStatut(UUID id, StatutOrganisation statut, String motif) {
        if (statut == null) throw new IllegalArgumentException("Le statut est obligatoire.");
        Organisation organisation = charger(id);
        if ((statut == StatutOrganisation.REJETEE || statut == StatutOrganisation.SUSPENDUE)
                && (motif == null || motif.isBlank()))
            throw new IllegalArgumentException("Un motif est obligatoire pour un rejet ou une suspension.");
        if (statut != organisation.getStatut()) Transitions.ORGANISATION.verifier(organisation.getStatut(), statut);
        organisation.setStatut(statut);
        organisation.setMotifStatut((statut == StatutOrganisation.REJETEE || statut == StatutOrganisation.SUSPENDUE)
                ? motif.trim() : null);
        return organisationMapper.toResponse(organisationRepository.save(organisation));
    }



    private Organisation charger(UUID id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation introuvable."));
    }

    private void verifierProprietaire(Organisation organisation) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || !organisation.getUtilisateur().getId().toString().equals(auth.getName()))
            throw new AccessDeniedException("Ce profil organisation ne vous appartient pas.");
    }
}
