package odk.malicompetences.backend.centre;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class centerService {

    private final CentreRepository centreRepository;
    private final centreMapper centreMapper;

    public centerService(CentreRepository centreRepository, centreMapper centreMapper) {
        this.centreRepository = centreRepository;
        this.centreMapper = centreMapper;
    }

    // Créer un centre
    public CenterResponse creerCentre(CentreRequest request) {
        Centre centre = centreMapper.toEntity(request);
        Centre savedCentre = centreRepository.save(centre);
        return centreMapper.toResponse(savedCentre);
    }

    // Consulter tous les centres
    @Transactional(readOnly = true)
    public List<CenterResponse> listerTousLesCentres() {
        return centreRepository.findAll().stream()
                .map(centreMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Consulter un centre par son ID
    @Transactional(readOnly = true)
    public CenterResponse trouverCentreParId(UUID id) {
        Centre centre = centreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Centre non trouvé avec l'id : " + id));
        return centreMapper.toResponse(centre);
    }

    // Modifier un centre
    public CenterResponse modifierCentre(UUID id, CentreRequest request) {
        Centre centre = centreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Centre non trouvé avec l'id : " + id));

        centreMapper.updateEntityFromRequest(request, centre);
        Centre updatedCentre = centreRepository.save(centre);
        return centreMapper.toResponse(updatedCentre);
    }

    // Supprimer un centre
    public void supprimerCentre(UUID id) {
        if (!centreRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer, centre non trouvé avec l'id : " + id);
        }
        centreRepository.deleteById(id);
    }
}