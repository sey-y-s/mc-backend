package odk.malicompetences.backend.preuve;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record PreuveRequest(
        @NotNull(message = "L'ID de la compétence du citoyen est obligatoire")
        UUID citoyenCompetenceId,

        @NotNull(message = "Le type de preuve est obligatoire")
        TypePreuve type,

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
        String titre,

        String description,

        @Size(max = 500, message = "L'URL du fichier ne doit pas dépasser 500 caractères")
        String fichierUrl
) {}