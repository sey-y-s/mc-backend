package odk.malicompetences.backend.localisation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/** Échanges de l'API de localisation du citoyen (aucune coordonnée n'est jamais renvoyée). */
public final class LocalisationDtos {

    private LocalisationDtos() {
    }

    /** Commune de référence et état de la détection. */
    public record MaLocalisation(
            UUID communeId,
            String communeNom,
            UUID regionId,
            String regionNom,
            SourceLocalisation source,
            LocalDateTime confirmeeLe,
            boolean gpsConsenti,
            LocalDateTime gpsConsentiLe,
            Suggestion demandeEnAttente
    ) {
    }

    public record Suggestion(UUID id, UUID communeId, String communeNom, String regionNom, LocalDateTime dateCreation) {
    }

    public record DeclarationCommune(@NotNull(message = "La commune est obligatoire") UUID communeId) {
    }

    public record Consentement(@NotNull(message = "Le choix est obligatoire") Boolean consenti) {
    }

    /** Position reçue ponctuellement : convertie en commune puis oubliée (ni stockée, ni journalisée). */
    public record PositionPonctuelle(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
    ) {
        @Override
        public String toString() {
            return "PositionPonctuelle[masquée]";
        }
    }

    public enum ResultatDetection {
        /** La commune détectée est la commune de référence : aucune action. */
        IDENTIQUE,
        /** Une nouvelle suggestion a été créée : le citoyen doit confirmer ou refuser. */
        SUGGESTION_CREEE,
        /** Une suggestion est déjà en attente : rien de plus n'est créé. */
        SUGGESTION_EN_ATTENTE,
        /** Une suggestion a été refusée récemment : pas de nouvelle proposition avant la fin du délai. */
        DELAI_EN_COURS,
        /** Aucune commune connue assez proche (hors couverture du référentiel). */
        COMMUNE_INCONNUE,
        /** Le citoyen n'a pas autorisé la détection ponctuelle. */
        CONSENTEMENT_REQUIS
    }

    public record Detection(ResultatDetection resultat, String message, Suggestion suggestion) {
    }
}
