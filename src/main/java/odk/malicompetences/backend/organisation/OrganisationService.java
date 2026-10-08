package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.talent.SuiviBesoinTalentRepository;
import odk.malicompetences.backend.miseenrelation.DemandeMiseEnRelationRepository;
import odk.malicompetences.backend.notification.NotificationDestinataireRepository;
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
    private final BesoinRepository besoinRepository;
    private final SuiviBesoinTalentRepository suiviRepository;
    private final DemandeMiseEnRelationRepository relationRepository;
    private final NotificationDestinataireRepository notificationRepository;

    public OrganisationService(
            OrganisationRepository organisationRepository,
            UtilisateurRepository utilisateurRepository,
            OrganisationMapper organisationMapper,
            BesoinRepository besoinRepository,
            SuiviBesoinTalentRepository suiviRepository,
            DemandeMiseEnRelationRepository relationRepository,
            NotificationDestinataireRepository notificationRepository
    ) {
        this.organisationRepository = organisationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.organisationMapper = organisationMapper;
        this.besoinRepository = besoinRepository;
        this.suiviRepository = suiviRepository;
        this.relationRepository = relationRepository;
        this.notificationRepository = notificationRepository;
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
    public OrganisationResponse trouverPourUtilisateur(UUID utilisateurId) {
        Organisation organisation = organisationRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Aucune organisation n’est associée à l’utilisateur connecté."));
        return organisationMapper.toResponse(organisation);
    }

    @Transactional(readOnly = true)
    public List<OrganisationResponse> listerToutes() {
        return organisationRepository.findAll().stream()
                .map(organisationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganisationDashboardResponse dashboard(UUID utilisateurId) {
        organisationRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée pour l’utilisateur connecté."));
        return new OrganisationDashboardResponse(
                besoinRepository.countByOrganisationIdAndStatut(utilisateurId, StatutBesoin.OUVERT),
                suiviRepository.countByOrganisationId(utilisateurId),
                relationRepository.countByDemandeurId(utilisateurId),
                notificationRepository.countByUtilisateurIdAndLuFalse(utilisateurId)
        );
    }

    public OrganisationResponse modifierOrganisation(UUID id, OrganisationRequest request) {
        Organisation org = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée avec l'id : " + id));

        organisationMapper.updateEntity(request, org);
        Organisation updatedOrganisation = organisationRepository.save(org);
        return organisationMapper.toResponse(updatedOrganisation);
    }

    public OrganisationResponse modifierStatut(UUID id, StatutOrganisation statut) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée avec l'id : " + id));
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