package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.utilisateur.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final OrganisationMapper organisationMapper;

    public OrganisationService(
            OrganisationRepository organisationRepository,
            UtilisateurRepository utilisateurRepository,
            OrganisationMapper organisationMapper
    ) {
        this.organisationRepository = organisationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.organisationMapper = organisationMapper;
    }

    public OrganisationResponse creerOrganisation(OrganisationRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(request.utilisateurId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'id : " + request.utilisateurId()));
        if (utilisateur.getRole() != Role.ORGANISATION) {
            throw new IllegalArgumentException("Le compte associé doit avoir le rôle ORGANISATION");
        }
        if (organisationRepository.existsById(request.utilisateurId())) {
            throw new IllegalStateException("Le profil organisation existe déjà");
        }

        Organisation organisation = organisationMapper.toEntity(request, utilisateur);
        Organisation savedOrganisation = organisationRepository.save(organisation);
        return organisationMapper.toResponse(savedOrganisation);
    }

    @Transactional(readOnly = true)
    public OrganisationResponse trouverParId(UUID id) {
        Organisation org = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée avec l'id : " + id));
        return organisationMapper.toResponse(org);
    }

    @Transactional(readOnly = true)
    public List<OrganisationResponse> listerToutes() {
        return organisationRepository.findAll().stream()
                .map(organisationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrganisationResponse modifierOrganisation(UUID id, OrganisationRequest request) {
        Organisation org = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée avec l'id : " + id));

        organisationMapper.updateEntity(request, org);
        Organisation updatedOrganisation = organisationRepository.save(org);
        return organisationMapper.toResponse(updatedOrganisation);
    }

    public OrganisationResponse modifierMonProfil(UUID utilisateurId, OrganisationProfilRequest request) {
        Organisation organisation = organisationRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Profil organisation introuvable"));
        organisation.setNom(request.nom().trim());
        organisation.setDescription(request.description());
        return organisationMapper.toResponse(organisationRepository.save(organisation));
    }

    public OrganisationResponse modifierStatut(UUID id, StatutOrganisation statut) {
        if (statut == null) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organisation introuvable."));
        organisation.setStatut(statut);
        return organisationMapper.toResponse(organisationRepository.save(organisation));
    }

    public void supprimerOrganisation(UUID id) {
        if (!organisationRepository.existsById(id)) {
            throw new RuntimeException("Organisation non trouvée avec l'id : " + id);
        }
        organisationRepository.deleteById(id);
    }
}