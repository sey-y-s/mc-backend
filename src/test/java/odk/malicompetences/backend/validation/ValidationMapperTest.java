package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ValidationMapperTest {

    private final ValidationMapper mapper = new ValidationMapper();

    @Test
    void uneNouvelleValidationResteEnAttente() {
        CitoyenCompetence competence = new CitoyenCompetence();
        Utilisateur evaluateur = new Utilisateur();
        evaluateur.setId(UUID.randomUUID());

        ValidationRequest request = new ValidationRequest(
                UUID.randomUUID(), null, null,
                TypeValidation.TEST_NUMERIQUE, "Test", null
        );

        Validation validation = mapper.toEntity(request, competence, null, evaluateur, null);

        assertEquals(StatutValidation.EN_ATTENTE, validation.getStatut());
        assertNull(validation.getDateValidation());
        assertSame(evaluateur, validation.getValideePar());
    }
}
