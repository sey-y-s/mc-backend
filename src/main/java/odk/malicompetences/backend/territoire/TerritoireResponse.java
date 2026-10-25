package odk.malicompetences.backend.territoire;

import java.util.UUID;

public record TerritoireResponse(
        UUID id,
        String code,
        String nom,
        NiveauTerritoire niveau,
        boolean actif,
        UUID parentId,
        String parentNom
) {
    public TerritoireResponse(UUID id, String code, String nom, NiveauTerritoire niveau, boolean actif) {
        this(id, code, nom, niveau, actif, null, null);
    }
}
