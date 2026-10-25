package odk.malicompetences.backend.territoire;

import java.util.List;
import java.util.UUID;

public record CommuneResponse(
        UUID id,
        UUID territoireId,
        String territoireNom,
        String code,
        String nom,
        boolean actif,
        List<TerritoireSummary> hierarchieParents
) {
    public CommuneResponse(UUID id, UUID territoireId, String code, String nom, boolean actif) {
        this(id, territoireId, null, code, nom, actif, null);
    }

    public record TerritoireSummary(UUID id, String code, String nom, NiveauTerritoire niveau) {}
}
