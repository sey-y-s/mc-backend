package odk.malicompetences.backend.citoyen;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record LocalisationProfilRequest(@NotNull UUID communeId) {}
