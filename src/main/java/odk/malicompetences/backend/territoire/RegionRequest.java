package odk.malicompetences.backend.territoire;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegionRequest(
        @jakarta.validation.constraints.Pattern(
                regexp = "^[A-Z0-9_-]{2,32}$",
                message = "Le code stable doit contenir 2 à 32 caractères A-Z, 0-9, _ ou -"
        )
        String codeStable,

        NiveauTerritoire niveau,

        @NotBlank(message = "Le nom de la région est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        /** Région parente, obligatoire pour un CERCLE et interdite sinon. */
        java.util.UUID parentId
) {
    public RegionRequest(String codeStable, NiveauTerritoire niveau, String nom) {
        this(codeStable, niveau, nom, null);
    }
}