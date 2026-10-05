package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
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
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + request.utilisateurId()));

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

    public void supprimerOrganisation(UUID id) {
        if (!organisationRepository.existsById(id)) {
            throw new RuntimeException("Organisation non trouvée avec l'id : " + id);
        }
        organisationRepository.deleteById(id);
    }
}