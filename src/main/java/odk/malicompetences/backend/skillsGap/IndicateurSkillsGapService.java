package odk.malicompetences.backend.skillsGap;

import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.territoire.Region;
import odk.malicompetences.backend.territoire.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class IndicateurSkillsGapService {

    private final IndicateurSkillsGapRepository indicateurSkillsGapRepository;
    private final RegionRepository regionRepository;
    private final CompetenceRepository competenceRepository;
    private final IndicateurSkillsGapMapper indicateurSkillsGapMapper;

    public IndicateurSkillsGapService(
            IndicateurSkillsGapRepository indicateurSkillsGapRepository,
            RegionRepository regionRepository,
            CompetenceRepository competenceRepository,
            IndicateurSkillsGapMapper indicateurSkillsGapMapper
    ) {
        this.indicateurSkillsGapRepository = indicateurSkillsGapRepository;
        this.regionRepository = regionRepository;
        this.competenceRepository = competenceRepository;
        this.indicateurSkillsGapMapper = indicateurSkillsGapMapper;
    }

    public IndicateurSkillsGapResponse creerIndicateur(IndicateurSkillsGapRequest request) {
        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id : " + request.regionId()));

        Competence competence = competenceRepository.findById(request.competenceId())
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + request.competenceId()));

        IndicateurSkillsGap indicateur = indicateurSkillsGapMapper.toEntity(request, region, competence);
        IndicateurSkillsGap saved = indicateurSkillsGapRepository.save(indicateur);
        return indicateurSkillsGapMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IndicateurSkillsGapResponse trouverParId(UUID id) {
        IndicateurSkillsGap indicateur = indicateurSkillsGapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicateur Skills Gap non trouvé avec l'id : " + id));
        return indicateurSkillsGapMapper.toResponse(indicateur);
    }

    @Transactional(readOnly = true)
    public List<IndicateurSkillsGapResponse> listerTous() {
        return indicateurSkillsGapRepository.findAll().stream()
                .map(indicateurSkillsGapMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IndicateurSkillsGapResponse> listerParRegion(UUID regionId) {
        return indicateurSkillsGapRepository.findAll().stream()
                .filter(i -> i.getRegion().getId().equals(regionId))
                .map(indicateurSkillsGapMapper::toResponse)
                .collect(Collectors.toList());
    }

    public IndicateurSkillsGapResponse modifierIndicateur(UUID id, IndicateurSkillsGapRequest request) {
        IndicateurSkillsGap indicateur = indicateurSkillsGapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicateur Skills Gap non trouvé avec l'id : " + id));

        Region region = null;
        if (request.regionId() != null) {
            region = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new RuntimeException("Région non trouvée avec l'id : " + request.regionId()));
        }

        Competence competence = null;
        if (request.competenceId() != null) {
            competence = competenceRepository.findById(request.competenceId())
                    .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + request.competenceId()));
        }

        indicateurSkillsGapMapper.updateEntity(request, indicateur, region, competence);
        IndicateurSkillsGap updated = indicateurSkillsGapRepository.save(indicateur);
        return indicateurSkillsGapMapper.toResponse(updated);
    }

    public void supprimerIndicateur(UUID id) {
        if (!indicateurSkillsGapRepository.existsById(id)) {
            throw new RuntimeException("Indicateur Skills Gap non trouvé avec l'id : " + id);
        }
        indicateurSkillsGapRepository.deleteById(id);
    }
}