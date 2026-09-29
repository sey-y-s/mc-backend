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
public class SuiviTalentService {

    private final SuiviTalentRepository suiviTalentRepository;
    private final OrganisationRepository organisationRepository;
    private final BesoinRepository besoinRepository;
    private final CitoyenRepository citoyenRepository;
    private final SuiviTalentMapper suiviTalentMapper;

    public SuiviTalentService(
            SuiviTalentRepository suiviTalentRepository,
            OrganisationRepository organisationRepository,
            BesoinRepository besoinRepository,
            CitoyenRepository citoyenRepository,
            SuiviTalentMapper suiviTalentMapper
    ) {
        this.suiviTalentRepository = suiviTalentRepository;
        this.organisationRepository = organisationRepository;
        this.besoinRepository = besoinRepository;
        this.citoyenRepository = citoyenRepository;
        this.suiviTalentMapper = suiviTalentMapper;
    }

    public SuiviTalentResponse creerSuivi(SuiviTalentRequest request) {
        Organisation org = organisationRepository.findById(request.organisationId())
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));
        Besoin besoin = besoinRepository.findById(request.besoinId())
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé"));
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));

        SuiviTalent suivi = suiviTalentMapper.toEntity(request, org, besoin, citoyen);
        return suiviTalentMapper.toResponse(suiviTalentRepository.save(suivi));
    }

    @Transactional(readOnly = true)
    public SuiviTalentResponse trouverParId(UUID id) {
        SuiviTalent suivi = suiviTalentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suivi de talent non trouvé"));
        return suiviTalentMapper.toResponse(suivi);
    }

    @Transactional(readOnly = true)
    public List<SuiviTalentResponse> listerParBesoin(UUID besoinId) {
        return suiviTalentRepository.findAll().stream()
                .filter(s -> s.getBesoin().getId().equals(besoinId))
                .map(suiviTalentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public SuiviTalentResponse modifierSuivi(UUID id, SuiviTalentRequest request) {
        SuiviTalent suivi = suiviTalentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suivi de talent non trouvé"));

        suiviTalentMapper.updateEntity(request, suivi);
        return suiviTalentMapper.toResponse(suiviTalentRepository.save(suivi));
    }

    public void supprimerSuivi(UUID id) {
        if (!suiviTalentRepository.existsById(id)) {
            throw new RuntimeException("Suivi de talent non trouvé");
        }
        suiviTalentRepository.deleteById(id);
    }
}