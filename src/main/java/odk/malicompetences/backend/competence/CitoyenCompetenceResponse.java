package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.preuve.TypePreuve;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CitoyenCompetenceResponse(
        UUID id,
        UUID competenceId,
        String competenceNom,
        NiveauCompetence niveau,
        EtatCompetence etat,
        LocalDateTime dateDeclaration,
        ParcoursCompetence parcours,
        /** Types de preuve que le citoyen peut déposer pour cette compétence. */
        List<TypePreuve> preuvesAcceptees,
        int nombrePreuves,
        /** Secteurs possibles pour cette compétence (un seul = imposé). */
        List<ParcoursCompetence> parcoursPossibles
) {
}
