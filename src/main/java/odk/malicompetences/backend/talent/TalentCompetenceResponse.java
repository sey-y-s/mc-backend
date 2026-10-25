package odk.malicompetences.backend.talent;


import odk.malicompetences.backend.competence.NiveauCompetence;

import java.util.UUID;

/** Compétence d'un talent (profil anonymisé). {@code id} et {@code competenceId} désignent la compétence du référentiel. */
public record TalentCompetenceResponse(
        UUID id,
        String nom,
        UUID competenceId,
        NiveauCompetence niveau,
        boolean validee,
        boolean aPreuve
) {
}
