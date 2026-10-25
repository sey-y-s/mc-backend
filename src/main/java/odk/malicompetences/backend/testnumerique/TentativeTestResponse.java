package odk.malicompetences.backend.testnumerique;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.UUID;
public record TentativeTestResponse(UUID id,UUID demandeValidationId,UUID testId,BigDecimal score,BigDecimal seuilApplique,Boolean reussi,LocalDateTime commenceeLe,LocalDateTime termineeLe){ public static TentativeTestResponse from(TentativeTest t){return new TentativeTestResponse(t.getId(),t.getDemande().getId(),t.getTest().getId(),t.getScore(),t.getSeuilApplique(),t.getReussi(),t.getCommenceeLe(),t.getTermineeLe());}}
