package odk.malicompetences.backend.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaRealisationRequest(
        @NotNull(message = "Le type de média est obligatoire")
        TypeMedia type,

        @NotBlank(message = "L'URL du média est obligatoire")
        String urlMedia,

        String legende
) {}