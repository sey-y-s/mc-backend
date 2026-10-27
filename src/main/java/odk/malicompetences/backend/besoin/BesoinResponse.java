package odk.malicompetences.backend.besoin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BesoinResponse(
        UUID id,
        UUID organisationId,
        String organisationNom,
        String titre,
        String description,
        StatutBesoin statut,
        LocalDate dateDebut,
        LocalDate dateFin,
        LocalDateTime dateCreation,
        List<BesoinCompetenceResponse> competences,
        UUID regionId,
        String regionNom,
        UUID communeId,
        String communeNom
) {
    public BesoinResponse(UUID id, UUID organisationId, String organisationNom, String titre, String description,
                          StatutBesoin statut, LocalDate dateDebut, LocalDate dateFin, LocalDateTime dateCreation,
                          List<BesoinCompetenceResponse> competences) {
        this(id, organisationId, organisationNom, titre, description, statut, dateDebut, dateFin, dateCreation,
                competences, null, null, null, null);
    }
}