package odk.malicompetences.backend.centre;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.StatutCentre;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CentreService {
    private final CentreRepository centres;
    private final CentreMapper mapper;
    private final CommuneRepository communes;

    public CentreService(CentreRepository centres, CentreMapper mapper, CommuneRepository communes) {
        this.centres = centres;
        this.mapper = mapper;
        this.communes = communes;
    }

    public CentreResponse creerCentre(CentreRequest request) {
        Commune commune = chargerCommune(request.communeId());
        return mapper.toResponse(centres.save(mapper.toEntity(request, commune)));
    }

    @Transactional(readOnly = true)
    public List<CentreResponse> listerTousLesCentres() {
        return centres.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CentreResponse trouverCentreParId(UUID id) {
        return mapper.toResponse(chargerCentre(id));
    }

    public CentreResponse modifierCentre(UUID id, CentreRequest request) {
        Centre centre = chargerCentre(id);
        Commune commune = chargerCommune(request.communeId());
        mapper.updateEntityFromRequest(request, centre, commune);
        return mapper.toResponse(centres.save(centre));
    }

    public void supprimerCentre(UUID id) {
        Centre centre = chargerCentre(id);
        if (centre.getStatut() == StatutCentre.ACTIF) {
            throw new IllegalStateException("Un centre actif ne peut pas être supprimé ; suspendez-le d'abord.");
        }
        centres.delete(centre);
    }

    private Centre chargerCentre(UUID id) {
        return centres.findById(id).orElseThrow(() -> new ResourceNotFoundException("Centre introuvable."));
    }

    private Commune chargerCommune(UUID id) {
        return communes.findById(id).filter(Commune::isActif)
                .orElseThrow(() -> new ResourceNotFoundException("Commune active introuvable."));
    }
}
