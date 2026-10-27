package odk.malicompetences.backend.testnumerique;

import java.util.UUID;

/**
 * Proposition de réponse telle que vue par un candidat : jamais l'indication « correcte »,
 * sinon n'importe qui pourrait lire les bonnes réponses via l'API.
 */
public record PropositionPubliqueResponse(UUID id, String texte) {

    public static PropositionPubliqueResponse from(PropositionReponse proposition) {
        return new PropositionPubliqueResponse(proposition.getId(), proposition.getTexte());
    }
}
