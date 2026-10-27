package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SuiviBesoinTalentService {

    private final SuiviBesoinTalentRepository SuiviBesoinTalentRepository;
    private final OrganisationRepository organisationRepository;
    private final BesoinRepository besoinRepository;
    private final CitoyenRepository citoyenRepository;
    private final SuiviBesoinTalentMapper SuiviBesoinTalentMapper;
    private final odk.malicompetences.backend.citoyen.ProfilCompletudeService profilCompletudeService;

    public SuiviBesoinTalentService(
            SuiviBesoinTalentRepository SuiviBesoinTalentRepository,
            OrganisationRepository organisationRepository,
            BesoinRepository besoinRepository,
            CitoyenRepository citoyenRepository,
            SuiviBesoinTalentMapper SuiviBesoinTalentMapper,
            odk.malicompetences.backend.citoyen.ProfilCompletudeService profilCompletudeService
    ) {
        this.SuiviBesoinTalentRepository = SuiviBesoinTalentRepository;
        this.organisationRepository = organisationRepository;
        this.besoinRepository = besoinRepository;
        this.citoyenRepository = citoyenRepository;
        this.SuiviBesoinTalentMapper = SuiviBesoinTalentMapper;
        this.profilCompletudeService = profilCompletudeService;
    }

    public SuiviBesoinTalentResponse creerSuivi(SuiviBesoinTalentRequest request) {
        verifierProprietaire(request.organisationId(), "ORGANISATION");
        Organisation org = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Organisation non trouvée"));
        Besoin besoin = besoinRepository.findById(request.besoinId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin non trouvé"));
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Citoyen non trouvé"));
        profilCompletudeService.exigerVisible(citoyen);

        if (!besoin.getOrganisation().getId().equals(org.getId())) {
            throw new IllegalArgumentException("Le besoin n'appartient pas à cette organisation");
        }

        SuiviBesoinTalent suivi = SuiviBesoinTalentMapper.toEntity(request, org, besoin, citoyen);
        return SuiviBesoinTalentMapper.toResponse(SuiviBesoinTalentRepository.save(suivi));
    }

    @Transactional(readOnly = true)
    public SuiviBesoinTalentResponse trouverParId(UUID id) {
        SuiviBesoinTalent suivi = SuiviBesoinTalentRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Suivi de talent non trouvé"));
        if (!estAdministrateur()) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean organisationAutorisee = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ORGANISATION")) && suivi.getOrganisation().getId().toString().equals(auth.getName());
            boolean citoyenAutorise = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CITOYEN")) && suivi.getCitoyen().getId().toString().equals(auth.getName());
            if (!organisationAutorisee && !citoyenAutorise) throw new org.springframework.security.access.AccessDeniedException("Accès refusé");
            if (citoyenAutorise && !organisationAutorisee) {
                // La note interne appartient à l'organisation : elle n'est jamais montrée au citoyen.
                SuiviBesoinTalentResponse r = SuiviBesoinTalentMapper.toResponse(suivi);
                return new SuiviBesoinTalentResponse(r.id(), r.organisationId(), r.besoinId(), r.besoinTitre(),
                        r.citoyenId(), r.citoyenNomComplet(), r.statut(), r.scoreCorrespondance(),
                        r.detailsCorrespondance(), null, r.dateCreation(), r.dateMiseAJour());
            }
        }
        return SuiviBesoinTalentMapper.toResponse(suivi);
    }

    @Transactional(readOnly = true)
    public List<SuiviBesoinTalentResponse> listerParBesoin(UUID besoinId) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Besoin non trouvé"));
        if (!estAdministrateur()) {
            verifierProprietaire(besoin.getOrganisation().getId(), "ORGANISATION");
        }
        return SuiviBesoinTalentRepository.findAll().stream()
                .filter(s -> s.getBesoin().getId().equals(besoinId))
                .map(SuiviBesoinTalentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public SuiviBesoinTalentResponse modifierSuivi(UUID id, SuiviBesoinTalentRequest request) {
        SuiviBesoinTalent suivi = SuiviBesoinTalentRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Suivi de talent non trouvé"));

        verifierProprietaire(suivi.getOrganisation().getId(), "ORGANISATION");
        SuiviBesoinTalentMapper.updateEntity(request, suivi);
        return SuiviBesoinTalentMapper.toResponse(SuiviBesoinTalentRepository.save(suivi));
    }

    public void supprimerSuivi(UUID id) {
        if (!SuiviBesoinTalentRepository.existsById(id)) {
            throw new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Suivi de talent non trouvé");
        }
        SuiviBesoinTalent suivi = SuiviBesoinTalentRepository.findById(id).orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Suivi de talent non trouvé"));
        verifierProprietaire(suivi.getOrganisation().getId(), "ORGANISATION");
        SuiviBesoinTalentRepository.delete(suivi);
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