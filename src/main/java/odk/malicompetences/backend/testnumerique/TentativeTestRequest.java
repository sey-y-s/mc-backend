package odk.malicompetences.backend.testnumerique;
import com.fasterxml.jackson.databind.JsonNode; import jakarta.validation.constraints.NotNull; import java.util.Map; import java.util.UUID;
public record TentativeTestRequest(@NotNull UUID demandeValidationId,@NotNull Map<UUID,JsonNode> reponses){}
