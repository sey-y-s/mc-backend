package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import org.springframework.security.access.AccessDeniedException;
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

    public SuiviBesoinTalentService(
            SuiviBesoinTalentRepository SuiviBesoinTalentRepository,
            OrganisationRepository organisationRepository,
            BesoinRepository besoinRepository,
            CitoyenRepository citoyenRepository,
            SuiviBesoinTalentMapper SuiviBesoinTalentMapper
    ) {
        this.SuiviBesoinTalentRepository = SuiviBesoinTalentRepository;
        this.organisationRepository = organisationRepository;
        this.besoinRepository = besoinRepository;
        this.citoyenRepository = citoyenRepository;
        this.SuiviBesoinTalentMapper = SuiviBesoinTalentMapper;
    }

    public SuiviBesoinTalentResponse creerSuivi(SuiviBesoinTalentRequest request) {
        Organisation org = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));
        Besoin besoin = besoinRepository.findById(request.besoinId())
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé"));
        if (!besoin.getOrganisation().getId().equals(org.getId())) {
            throw new AccessDeniedException(
                    "Le besoin n’appartient pas à l’organisation indiquée.");
        }
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));

        SuiviBesoinTalent suivi = SuiviBesoinTalentMapper.toEntity(request, org, besoin, citoyen);
        return SuiviBesoinTalentMapper.toResponse(SuiviBesoinTalentRepository.save(suivi));
    }

    @Transactional(readOnly = true)
    public SuiviBesoinTalentResponse trouverParId(UUID id) {
        SuiviBesoinTalent suivi = SuiviBesoinTalentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suivi de talent non trouvé"));
        return SuiviBesoinTalentMapper.toResponse(suivi);
    }

    @Transactional(readOnly = true)
    public List<SuiviBesoinTalentResponse> listerParBesoin(UUID besoinId) {
        return SuiviBesoinTalentRepository.findByBesoinId(besoinId).stream()
                .map(SuiviBesoinTalentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void verifierBesoinEtOrganisation(UUID besoinId, UUID organisationId) {
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé"));
        if (!besoin.getOrganisation().getId().equals(organisationId)) {
            throw new AccessDeniedException("Accès refusé à ce besoin.");
        }
    }

    @Transactional(readOnly = true)
    public List<SuiviBesoinTalentResponse> listerParOrganisation(UUID organisationId) {
        return SuiviBesoinTalentRepository.findByOrganisationId(organisationId).stream()
                .map(SuiviBesoinTalentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public SuiviBesoinTalentResponse modifierSuivi(UUID id, SuiviBesoinTalentRequest request) {
        SuiviBesoinTalent suivi = SuiviBesoinTalentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suivi de talent non trouvé"));

        SuiviBesoinTalentMapper.updateEntity(request, suivi);
        return SuiviBesoinTalentMapper.toResponse(SuiviBesoinTalentRepository.save(suivi));
    }

    public void supprimerSuivi(UUID id) {
        if (!SuiviBesoinTalentRepository.existsById(id)) {
            throw new RuntimeException("Suivi de talent non trouvé");
        }
        SuiviBesoinTalentRepository.deleteById(id);
    }
}