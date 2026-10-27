package odk.malicompetences.backend.territoire;

import java.util.UUID;

public record RegionResponse(
        UUID id,
        String codeStable,
        NiveauTerritoire niveau,
        String nom,
        UUID parentId,
        String parentNom
) {
    public RegionResponse(UUID id, String codeStable, NiveauTerritoire niveau, String nom) {
        this(id, codeStable, niveau, nom, null, null);
    }
}
