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
        verifierProprietaire(request.organisationId(), "ORGANISATION");
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

        List<BesoinCompetence> competences = besoinCompetenceRepository.findAll().stream()
                .filter(bc -> bc.getBesoin().getId().equals(id))
                .collect(Collectors.toList());

        return besoinMapper.toResponse(besoin, competences);
    }

    // Lister tous les besoins
    @Transactional(readOnly = true)
    public List<BesoinResponse> listerTousLesBesoins() {
        return besoinRepository.findAll().stream().map(besoin -> {
            List<BesoinCompetence> competences = besoinCompetenceRepository.findAll().stream()
                    .filter(bc -> bc.getBesoin().getId().equals(besoin.getId()))
                    .collect(Collectors.toList());
            return besoinMapper.toResponse(besoin, competences);
        }).collect(Collectors.toList());
    }

    // Supprimer un besoin
    public void supprimerBesoin(UUID id) {
        if (!besoinRepository.existsById(id)) {
            throw new RuntimeException("Besoin non trouvé avec l'id : " + id);
        }
        besoinRepository.deleteById(id);
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