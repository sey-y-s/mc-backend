package odk.malicompetences.backend.citoyen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.common.statut.StatutActivite;
import java.time.LocalDate;
import java.util.UUID;

public record CitoyenRequest(
        @NotBlank(message = "Le nom est obligatoire") @Size(max = 120) String nom,
        @NotBlank(message = "Le prénom est obligatoire") @Size(max = 120) String prenom,
        LocalDate dateNaissance,
        Sexe sexe,
        StatutActivite statutActivite,
        Boolean profilPublic,
        UUID communeId
) {}
