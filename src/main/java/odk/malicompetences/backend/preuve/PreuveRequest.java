package odk.malicompetences.backend.preuve;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.common.statut.TypePreuveCitoyen;

public record PreuveRequest(

        @NotNull(message = "Le type de preuve est obligatoire")
        TypePreuveCitoyen type,

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 180, message = "Le titre ne doit pas dépasser 180 caractères")
        String titre,

        @Size(max = 180, message = "La référence document ne doit pas dépasser 180 caractères")
        String referenceDocument,

        String description,

        boolean visibleProfil
) {}
