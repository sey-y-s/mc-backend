package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommuneMapper {

    public Commune toEntity(CommuneRequest request, Region region) {
        if (request == null) return null;
        Commune commune = new Commune();
        commune.setRegion(region);
        commune.setNom(request.nom());
        return commune;
    }

    public void updateEntity(CommuneRequest request, Commune commune, Region region) {
        if (request != null && commune != null) {
            if (region != null) {
                commune.setRegion(region);
            }
            commune.setNom(request.nom());
        }
    }

    public CommuneResponse toResponse(Commune commune) {
        if (commune == null) return null;

        UUID regionId = commune.getRegion() != null ? commune.getRegion().getId() : null;
        String regionNom = commune.getRegion() != null ? commune.getRegion().getNom() : null;

        return new CommuneResponse(
                commune.getId(),
                regionId,
                regionNom,
                commune.getNom()
        );
    }
}