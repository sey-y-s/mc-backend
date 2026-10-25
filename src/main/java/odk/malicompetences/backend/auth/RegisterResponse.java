package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.statut.StatutCompte;

import java.util.UUID;

public record RegisterResponse(
        UUID utilisateurId,
        String message,
        StatutCompte statut,
        String canal,
        String destinataire
) {}

