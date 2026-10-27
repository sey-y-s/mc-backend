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

    private odk.malicompetences.backend.territoire.RegionRepository regionRepository;
    private odk.malicompetences.backend.territoire.CommuneRepository communeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    void setTerritoires(odk.malicompetences.backend.territoire.RegionRepository regionRepository,
                        odk.malicompetences.backend.territoire.CommuneRepository communeRepository) {
        this.regionRepository = regionRepository;
        this.communeRepository = communeRepository;
    }

    /**
     * Zone d'un besoin : une commune (sa région est alors déduite), ou un territoire seul.
     * Une commune doit appartenir au territoire choisi (région, district ou cercle).
     */
    private void appliquerZone(Besoin besoin, UUID regionId, UUID communeId) {
        var territoire = regionId == null ? null : regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Territoire introuvable."));
        var commune = communeId == null ? null : communeRepository.findById(communeId)
                .orElseThrow(() -> new IllegalArgumentException("Commune introuvable."));
        if (commune != null && territoire != null) {
            boolean dansRegion = commune.getRegion() != null && commune.getRegion().getId().equals(territoire.getId());
            boolean dansCercle = commune.getCercle() != null && commune.getCercle().getId().equals(territoire.getId());
            if (!dansRegion && !dansCercle) {
                throw new IllegalArgumentException("La commune choisie n'appartient pas au territoire choisi.");
            }
        }
        besoin.setCommune(commune);
        besoin.setRegion(commune != null ? commune.getRegion() : territoire);
    }

    // Créer un besoin avec ses compétences associées
    public BesoinResponse creerBesoin(BesoinRequest request) {
        verifierProprietaire(request.organisationId(), "ORGANISATION");
        Organisation organisation = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Organisation non trouvée avec l'id : " + request.organisationId()));

        if (request.statut() == StatutBesoin.OUVERT
                && organisation.getStatut() != odk.malicompetences.backend.organisation.StatutOrganisation.APPROUVEE) {
            throw new IllegalStateException("Une organisation non approuvée ne peut pas publier un besoin.");
        }
        if (request.dateDebut() != null && request.dateFin() != null && request.dateFin().isBefore(request.dateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être postérieure ou égale à la date de début.");
        }
        Besoin besoin = besoinMapper.toEntity(request, organisation);
        appliquerZone(besoin, request.regionId(), request.communeId());
        Besoin savedBesoin = besoinRepository.save(besoin);

        List<BesoinCompetence> savedCompetences = List.of();
        if (request.competences() != null && !request.competences().isEmpty()) {
            List<BesoinCompetence> besoinCompetences = request.competences().stream().map(compReq -> {
                Competence competence = competenceRepository.findById(compReq.competenceId())
                        .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Compétence non trouvée avec l'id : " + compReq.competenceId()));
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
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin non trouvé avec l'id : " + id));

        List<BesoinCompetence> competences = besoinCompetenceRepository.findAll().stream()
                .filter(bc -> bc.getBesoin().getId().equals(id))
                .collect(Collectors.toList());

        return besoinMapper.toResponse(besoin, competences);
    }

    @Transactional(readOnly = true)
    public List<BesoinResponse> listerParOrganisation(UUID organisationId) {
        verifierProprietaire(organisationId, "ORGANISATION");
        return besoinRepository.findAllByOrganisationId(organisationId).stream().map(besoin -> {
            List<BesoinCompetence> competences = besoinCompetenceRepository.findAll().stream()
                    .filter(bc -> bc.getBesoin().getId().equals(besoin.getId())).toList();
            return besoinMapper.toResponse(besoin, competences);
        }).toList();
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
            throw new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin non trouvé avec l'id : " + id);
        }
        besoinRepository.deleteById(id);
    }



    public BesoinResponse modifierBesoin(UUID id, BesoinRequest request) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin non trouvé avec l'id : " + id));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        if (request.organisationId() != null && !request.organisationId().equals(besoin.getOrganisation().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Le propriétaire du besoin ne peut pas être changé.");
        }
        if (request.statut() == StatutBesoin.OUVERT
                && besoin.getOrganisation().getStatut() != odk.malicompetences.backend.organisation.StatutOrganisation.APPROUVEE) {
            throw new IllegalStateException("Une organisation non approuvée ne peut pas publier un besoin.");
        }
        if (request.dateDebut() != null && request.dateFin() != null && request.dateFin().isBefore(request.dateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être postérieure ou égale à la date de début.");
        }
        if (besoin.getStatut() == StatutBesoin.CLOTURE && request.statut() != StatutBesoin.CLOTURE) {
            throw new IllegalStateException("Un besoin clôturé ne peut pas être rouvert par cette opération.");
        }
        besoin.setTitre(request.titre());
        besoin.setDescription(request.description());
        besoin.setDateDebut(request.dateDebut());
        besoin.setDateFin(request.dateFin());
        besoin.setStatut(request.statut());
        appliquerZone(besoin, request.regionId(), request.communeId());
        Besoin saved = besoinRepository.save(besoin);
        if (request.competences() != null) {
            List<BesoinCompetence> existing = besoinCompetenceRepository.findAll().stream()
                    .filter(bc -> bc.getBesoin().getId().equals(id)).toList();
            besoinCompetenceRepository.deleteAll(existing);
            List<BesoinCompetence> replacements = request.competences().stream().map(compReq -> {
                Competence competence = competenceRepository.findById(compReq.competenceId())
                        .orElseThrow(() -> new IllegalArgumentException("Compétence introuvable : " + compReq.competenceId()));
                return besoinMapper.toBesoinCompetenceEntity(compReq, saved, competence);
            }).toList();
            besoinCompetenceRepository.saveAll(replacements);
        }
        return trouverBesoinParId(id);
    }

    public BesoinResponse cloturerBesoin(UUID id) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin non trouvé avec l'id : " + id));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        if (besoin.getStatut() == StatutBesoin.CLOTURE) return trouverBesoinParId(id);
        besoin.setStatut(StatutBesoin.CLOTURE);
        besoinRepository.save(besoin);
        return trouverBesoinParId(id);
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