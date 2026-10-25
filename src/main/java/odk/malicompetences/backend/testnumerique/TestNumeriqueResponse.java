package odk.malicompetences.backend.testnumerique;

import java.math.BigDecimal;
import java.util.UUID;

public record TestNumeriqueResponse(
        UUID id,
        UUID competenceId,
        String titre,
        String description,
        String version,
        BigDecimal seuilReussite,
        int nombreQuestions
) {
    public static TestNumeriqueResponse from(TestNumerique test, int nombreQuestions) {
        return new TestNumeriqueResponse(test.getId(), test.getCompetence().getId(), test.getTitre(),
                test.getDescription(), test.getVersion(), test.getSeuilReussite(), nombreQuestions);
    }
}
