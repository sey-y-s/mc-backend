package odk.malicompetences.backend.testnumerique;

import java.util.UUID;

/** Test numérique tel que renvoyé par l'API (pas d'entité JPA : aucune relation « à la demande » à sérialiser). */
public record TestNumeriqueResponse(UUID id, UUID competenceId, String competenceNom,
                                    String titre, String description, boolean actif) {

    /** À appeler pendant la transaction : lit le nom de la compétence. */
    public static TestNumeriqueResponse from(TestNumerique test) {
        return new TestNumeriqueResponse(
                test.getId(),
                test.getCompetence().getId(),
                test.getCompetence().getNom(),
                test.getTitre(),
                test.getDescription(),
                test.isActif());
    }
}
