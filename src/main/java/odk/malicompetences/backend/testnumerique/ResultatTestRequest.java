package odk.malicompetences.backend.testnumerique;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ResultatTestRequest(
        @NotNull UUID citoyenId,
        @NotNull Map<UUID, List<UUID>> reponses
) {}
