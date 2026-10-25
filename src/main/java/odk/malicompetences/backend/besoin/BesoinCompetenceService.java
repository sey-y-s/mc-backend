package odk.malicompetences.backend.besoin;


import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.common.regle.Transitions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional(readOnly = true)
    public List<BesoinCompetenceResponse> listerCompetences(UUID besoinId) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin introuvable."));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        return besoinCompetenceRepository.findByBesoin_Id(besoinId).stream()
                .map(besoinMapper::toBesoinCompetenceResponse).toList();
    }

    public BesoinCompetenceResponse ajouterCompetence(UUID besoinId, BesoinCompetenceRequest request) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin introuvable."));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        if (besoinCompetenceRepository.existsByBesoin_IdAndCompetence_Id(besoinId, request.competenceId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                    "Cette compétence est déjà associée au besoin.");
        }
        Competence competence = competenceRepository.findById(request.competenceId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Compétence introuvable."));
        return besoinMapper.toBesoinCompetenceResponse(besoinCompetenceRepository.save(
                besoinMapper.toBesoinCompetenceEntity(request, besoin, competence)));
    }

    public BesoinCompetenceResponse modifierCompetence(UUID besoinId, UUID competenceId, BesoinCompetenceRequest request) {
        if (!competenceId.equals(request.competenceId())) {
            throw new IllegalArgumentException("L'ID de compétence du corps doit correspondre à celui de l'URL.");
        }
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin introuvable."));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        BesoinCompetence association = besoinCompetenceRepository.findByBesoin_IdAndCompetence_Id(besoinId, competenceId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Compétence non associée à ce besoin."));
        association.setNiveauRequis(request.niveauRequis());
        association.setNombreRequis(request.nombreRequis());
        return besoinMapper.toBesoinCompetenceResponse(besoinCompetenceRepository.save(association));
    }

    public void supprimerCompetence(UUID besoinId, UUID competenceId) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin introuvable."));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        BesoinCompetence association = besoinCompetenceRepository.findByBesoin_IdAndCompetence_Id(besoinId, competenceId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Compétence non associée à ce besoin."));
        besoinCompetenceRepository.delete(association);
    }

    // Créer un besoin avec ses compétences associées
    public BesoinResponse creerBesoin(BesoinRequest request) {
        verifierProprietaire(request.organisationId(), "ORGANISATION");
        Organisation organisation = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée avec l'id : " + request.organisationId()));

        if (request.statut() != StatutBesoin.BROUILLON && request.statut() != StatutBesoin.OUVERT) {
            throw new IllegalArgumentException("Un nouveau besoin doit commencer en BROUILLON ou être publié en OUVERT.");
        }
        if (request.statut() == StatutBesoin.OUVERT
                && request.dateLimite() != null && request.dateLimite().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("La date limite d'un besoin ouvert ne peut pas être passée.");
        }
        if (request.statut() == StatutBesoin.OUVERT
                && organisation.getStatut() != odk.malicompetences.backend.organisation.StatutOrganisation.APPROUVEE) {
            throw new IllegalStateException("Une organisation non approuvée ne peut pas publier un besoin.");
        }
        if (request.competences() == null || request.competences().isEmpty()) {
            throw new IllegalArgumentException("Un besoin doit préciser au moins une compétence.");
        }
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

        List<BesoinCompetence> competences = besoinCompetenceRepository.findByBesoin_Id(id);

        return besoinMapper.toResponse(besoin, competences);
    }

    @Transactional(readOnly = true)
    public List<BesoinResponse> listerParOrganisation(UUID organisationId) {
        verifierProprietaire(organisationId, "ORGANISATION");
        return besoinRepository.findAllByOrganisation_Id(organisationId).stream().map(besoin -> {
            List<BesoinCompetence> competences = besoinCompetenceRepository.findByBesoin_Id(besoin.getId());
            return besoinMapper.toResponse(besoin, competences);
        }).toList();
    }

    // Lister tous les besoins
    @Transactional(readOnly = true)
    public List<BesoinResponse> listerTousLesBesoins() {
        return besoinRepository.findAll().stream().map(besoin -> {
            List<BesoinCompetence> competences = besoinCompetenceRepository.findByBesoin_Id(besoin.getId());
            return besoinMapper.toResponse(besoin, competences);
        }).collect(Collectors.toList());
    }

    // Supprimer un besoin
    public void supprimerBesoin(UUID id) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec l'id : " + id));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        if (besoin.getStatut() != StatutBesoin.ANNULE) {
            Transitions.BESOIN.verifier(besoin.getStatut(), StatutBesoin.ANNULE);
            besoin.setStatut(StatutBesoin.ANNULE);
            besoinRepository.save(besoin);
        }
    }



    public BesoinResponse modifierBesoin(UUID id, BesoinRequest request) {
        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec l'id : " + id));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        if (request.organisationId() != null && !request.organisationId().equals(besoin.getOrganisation().getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Le propriétaire du besoin ne peut pas être changé.");
        }
        if (request.statut() == StatutBesoin.OUVERT
                && besoin.getOrganisation().getStatut() != odk.malicompetences.backend.organisation.StatutOrganisation.APPROUVEE) {
            throw new IllegalStateException("Une organisation non approuvée ne peut pas publier un besoin.");
        }
        if (request.statut() != null && request.statut() != besoin.getStatut()) {
            Transitions.BESOIN.verifier(besoin.getStatut(), request.statut());
        }
        besoin.setTitre(request.titre());
        besoin.setDescription(request.description());
        besoin.setDateLimite(request.dateLimite());
        if (request.statut() != null) {
            besoin.setStatut(request.statut());
            if (request.statut() == StatutBesoin.OUVERT && besoin.getPublieLe() == null) {
                besoin.setPublieLe(java.time.LocalDateTime.now());
            }
        }
        Besoin saved = besoinRepository.save(besoin);
        if (request.competences() != null) {
            if (request.competences().isEmpty()) {
                throw new IllegalArgumentException("Un besoin doit conserver au moins une compétence.");
            }
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
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec l'id : " + id));
        verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        if (besoin.getStatut() == StatutBesoin.CLOTURE) return trouverBesoinParId(id);
        Transitions.BESOIN.verifier(besoin.getStatut(), StatutBesoin.CLOTURE);
        besoin.setStatut(StatutBesoin.CLOTURE);
        besoinRepository.save(besoin);
        return trouverBesoinParId(id);
    }

    /** Vérifie le propriétaire via organisation.utilisateur_id, pas en comparant deux UUID différents. */
    private void verifierProprietaire(UUID organisationId, String roleAttendu) {
        if (estAdministrateur()) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean bonRole = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleAttendu));
        UUID utilisateurId;
        try { utilisateurId = UUID.fromString(auth == null ? "" : auth.getName()); }
        catch (IllegalArgumentException ex) { throw new org.springframework.security.access.AccessDeniedException("Authentification requise."); }
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Organisation introuvable."));
        if (!bonRole || organisation.getUtilisateur() == null || !organisation.getUtilisateur().getId().equals(utilisateurId)) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé : cette ressource ne vous appartient pas.");
        }
    }

    private boolean estAdministrateur() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}