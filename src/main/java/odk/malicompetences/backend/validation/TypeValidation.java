package odk.malicompetences.backend.validation;

public enum TypeValidation {
    DIPLOME_VERIFIE,
    PARTENAIRE_ATTESTE,
    VALIDATION_PRATIQUE,
    TEST_NUMERIQUE,
    VAE,
    /** Analyse à distance du portfolio de réalisations par un jury habilité. */
    PORTFOLIO_MEDIA,
    /** Observation du savoir-faire sur le terrain par un maître artisan référent habilité. */
    MAITRE_ARTISAN
}