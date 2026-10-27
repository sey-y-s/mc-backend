package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Component;

@Component
public class RegionMapper {

    public Region toEntity(RegionRequest request) {
        if (request == null) return null;
        Region region = new Region();
        region.setCodeStable(request.codeStable());
        region.setNiveau(niveauPour(request.niveau(), request.codeStable(), request.nom()));
        region.setNom(request.nom());
        return region;
    }

    public void updateEntity(RegionRequest request, Region region) {
        if (request != null && region != null) {
            if (request.codeStable() != null) region.setCodeStable(request.codeStable());
            if (request.niveau() != null) region.setNiveau(request.niveau());
            region.setNom(request.nom());
        }
    }

    public RegionResponse toResponse(Region region) {
        if (region == null) return null;
        return new RegionResponse(
                region.getId(),
                region.getCodeStable(),
                region.getNiveau(),
                region.getNom(),
                region.getParent() == null ? null : region.getParent().getId(),
                region.getParent() == null ? null : region.getParent().getNom()
        );
    }

    private NiveauTerritoire niveauPour(NiveauTerritoire niveau, String codeStable, String nom) {
        if (niveau != null) return niveau;
        if (codeStable != null && codeStable.startsWith("ML-DIS-")) return NiveauTerritoire.DISTRICT;
        if (nom != null && nom.toLowerCase(java.util.Locale.ROOT).contains("bamako")) return NiveauTerritoire.DISTRICT;
        return NiveauTerritoire.REGION;
    }
}