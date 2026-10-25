package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.common.statut.StatutActivite;
import java.time.LocalDate;
import java.util.UUID;

public record CitoyenResponse(
        UUID id,
        UUID identifiantPublic,
        String nom,
        String prenom,
        LocalDate dateNaissance,
        Sexe sexe,
        StatutActivite statutActivite,
        boolean profilPublic,
        UUID communeId,
        String communeNom
) {}
