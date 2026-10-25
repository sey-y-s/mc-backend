package odk.malicompetences.backend.talent;

import java.util.UUID;

public record SuiviBesoinTalentRequest(
        UUID besoinId,
        UUID citoyenId,
        StatutSuiviBesoinTalent statut,
        String noteInterne
) {}