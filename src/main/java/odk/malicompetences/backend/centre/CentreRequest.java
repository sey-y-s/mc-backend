package odk.malicompetences.backend.centre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CentreRequest(
        @NotBlank @Size(max = 180) String nom,
        String description,
        @Size(max = 255) String adresse,
        @Size(max = 30) String telephone,
        @jakarta.validation.constraints.Email @Size(max = 190) String email,
        @NotNull UUID communeId
) {}
