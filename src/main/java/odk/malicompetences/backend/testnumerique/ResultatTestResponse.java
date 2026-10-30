package odk.malicompetences.backend.testnumerique;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ResultatTestResponse(UUID id, UUID testId, UUID citoyenId,
                                   BigDecimal score, boolean reussi,
                                   LocalDateTime datePassage, UUID preuveId,
                                   boolean competenceValidee) {
    public static ResultatTestResponse from(ResultatTest r) {
        return new ResultatTestResponse(r.getId(), r.getTest().getId(), r.getCitoyen().getId(),
                r.getScore(), Boolean.TRUE.equals(r.getReussi()), r.getDatePassage(),
                r.getPreuve() != null ? r.getPreuve().getId() : null, r.isCompetenceValidee());
    }
}
