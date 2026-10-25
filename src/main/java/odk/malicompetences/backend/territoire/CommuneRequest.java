package odk.malicompetences.backend.territoire;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommuneRequest(
        @NotNull(message = "Le territoire parent est obligatoire")
        UUID territoireId,

        @NotBlank(message = "Le code est obligatoire")
        @Size(max = 30, message = "Le code ne doit pas dépasser 30 caractères")
        String code,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 120, message = "Le nom ne doit pas dépasser 120 caractères")
        String nom,

        Boolean actif
) {}
