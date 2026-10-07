package odk.malicompetences.backend.territoire;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CommuneRequest(
        @NotNull(message = "L'ID de la région est obligatoire")
        UUID regionId,

        @NotBlank(message = "Le nom de la commune est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom
) {}