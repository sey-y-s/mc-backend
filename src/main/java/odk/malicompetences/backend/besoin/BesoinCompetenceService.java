package odk.malicompetences.backend.besoin;

import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BesoinCompetenceService {

    private final BesoinRepository besoinRepository;
    private final BesoinCompetenceRepository besoinCompetenceRepository;
    private final OrganisationRepository organisationRepository;
    private final CompetenceRepository competenceRepository;
    private final BesoinMapper besoinMapper;

    public BesoinCompetenceService(
            BesoinRepository besoinRepository,
            BesoinCompetenceRepository besoinCompetenceRepository,
            OrganisationRepository organisationRepository,
            CompetenceRepository competenceRepository,
            BesoinMapper besoinMapper
    ) {
        this.besoinRepository = besoinRepository;
        this.besoinCompetenceRepository = besoinCompetenceRepository;
        this.organisationRepository = organisationRepository;
        this.competenceRepository = competenceRepository;
        this.besoinMapper = besoinMapper;
    }

    // Créer un besoin avec ses compétences associées
    public BesoinResponse creerBesoin(BesoinRequest request) {
        Organisation organisation = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée avec l'id : " + request.organisationId()));

        Besoin besoin = besoinMapper.toEntity(request, organisation);
        Besoin savedBesoin = besoinRepository.save(besoin);

        List<BesoinCompetence> savedCompetences = List.of();
        if (request.competences() != null && !request.competences().isEmpty()) {
            List<BesoinCompetence> besoinCompetences = request.competences().stream().map(compReq -> {
                Competence competence = competenceRepository.findById(compReq.competenceId())
                        .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + compReq.competenceId()));
                return besoinMapper.toBesoinCompetenceEntity(compReq, savedBesoin, competence);
            }).collect(Collectors.toList());

            savedCompetences = besoinCompetenceRepository.saveAll(besoinCompetences);
        }

        return besoinMapper.toResponse(savedBesoin, savedCompetences);
    }

    // Consulter un besoin par son ID avec ses compétences
    @Transactional(readOnly = true)
    public BesoinResponse trouverBesoinParId(UUID id) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec l'id : " + id));

        List<BesoinCompetence> competences = besoinCompetenceRepository.findByBesoinId(id);

        return besoinMapper.toResponse(besoin, competences);
    }

    public BesoinResponse modifierBesoin(UUID id, BesoinRequest request) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec l'id : " + id));
        Organisation organisation = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée avec l'id : " + request.organisationId()));

        besoin.setOrganisation(organisation);
        besoin.setTitre(request.titre());
        besoin.setDescription(request.description());
        besoin.setStatut(request.statut());
        besoin.setDateDebut(request.dateDebut());
        besoin.setDateFin(request.dateFin());
        Besoin savedBesoin = besoinRepository.save(besoin);

        List<BesoinCompetence> anciennesCompetences = besoinCompetenceRepository.findByBesoinId(id);
        besoinCompetenceRepository.deleteAll(anciennesCompetences);

        List<BesoinCompetence> nouvellesCompetences = request.competences() == null
                ? List.of()
                : request.competences().stream().map(compReq -> {
                    Competence competence = competenceRepository.findById(compReq.competenceId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Compétence non trouvée avec l'id : " + compReq.competenceId()));
                    return besoinMapper.toBesoinCompetenceEntity(compReq, savedBesoin, competence);
                }).toList();
        List<BesoinCompetence> savedCompetences = besoinCompetenceRepository.saveAll(nouvellesCompetences);

        return besoinMapper.toResponse(savedBesoin, savedCompetences);
    }

    @Transactional(readOnly = true)
    public List<BesoinResponse> listerParOrganisation(UUID organisationId) {
        return besoinRepository.findByOrganisationId(organisationId).stream()
                .map(besoin -> besoinMapper.toResponse(
                        besoin,
                        besoinCompetenceRepository.findByBesoinId(besoin.getId())))
                .toList();
    }

    // Lister tous les besoins
    @Transactional(readOnly = true)
    public List<BesoinResponse> listerTousLesBesoins() {
        return besoinRepository.findAll().stream()
                .map(besoin -> besoinMapper.toResponse(
                        besoin,
                        besoinCompetenceRepository.findByBesoinId(besoin.getId())))
                .collect(Collectors.toList());
    }

    // Supprimer un besoin
    public void supprimerBesoin(UUID id) {
        if (!besoinRepository.existsById(id)) {
            throw new RuntimeException("Besoin non trouvé avec l'id : " + id);
        }
        besoinRepository.deleteById(id);
    }
}