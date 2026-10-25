package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.common.statut.StatutEvaluateur;
import java.util.UUID;

public record EvaluateurProfilResponse(UUID id, UUID citoyenId, StatutEvaluateur statut) {}
