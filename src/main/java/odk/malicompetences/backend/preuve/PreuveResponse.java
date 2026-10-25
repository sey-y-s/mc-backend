package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.common.statut.StatutPreuve;
import odk.malicompetences.backend.common.statut.TypePreuveCitoyen;

import java.time.LocalDateTime;
import java.util.UUID;

public record PreuveResponse(
        UUID id,
        UUID citoyenCompetenceId,
        TypePreuveCitoyen type,
        String titre,
        String referenceDocument,
        String description,
        StatutPreuve statut,
        boolean visibleProfil,
        LocalDateTime deposeeLe,
        /** true si un fichier physique est associé (téléchargeable via /preuves/{id}/fichier) */
        boolean aFichier
) {}
