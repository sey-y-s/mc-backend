package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Component;

@Component
public class RegionMapper {

    public Region toEntity(RegionRequest request) {
        if (request == null) return null;
        Region region = new Region();
        region.setNom(request.nom());
        return region;
    }

    public void updateEntity(RegionRequest request, Region region) {
        if (request != null && region != null) {
            region.setNom(request.nom());
        }
    }

    public RegionResponse toResponse(Region region) {
        if (region == null) return null;
        return new RegionResponse(
                region.getId(),
                region.getNom()
        );
    }
}
