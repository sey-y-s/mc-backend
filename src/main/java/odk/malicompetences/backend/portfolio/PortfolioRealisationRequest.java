package odk.malicompetences.backend.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PortfolioRealisationRequest(
        @NotNull(message = "L'ID du citoyen est obligatoire")
        UUID citoyenId,

        @NotBlank(message = "Le titre est obligatoire")
        String titre,

        String description,
        LocalDate dateRealisation,
        String localisation,
        List<MediaRealisationRequest> medias,
        String lienUrl
) {}