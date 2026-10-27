package odk.malicompetences.backend.parcours;

import odk.malicompetences.backend.validation.TypeValidation;

/** Les 5 modalités de validation d'une compétence (complémentaires, sans hiérarchie préétablie). */
public enum ModaliteValidation {
    DOCUMENTAIRE("Validation documentaire",
            "Vérification de vos diplômes, certificats ou attestations.",
            TypeValidation.DIPLOME_VERIFIE),
    TEST_NUMERIQUE("Test numérique",
            "Test en ligne de vos connaissances théoriques (QCM).",
            TypeValidation.TEST_NUMERIQUE),
    PRATIQUE_CENTRE("Évaluation pratique en centre",
            "Examen pratique dans un centre de formation professionnelle agréé.",
            TypeValidation.VALIDATION_PRATIQUE),
    PORTFOLIO_MEDIA("Évaluation sur portfolio",
            "Analyse à distance de vos réalisations (photos, vidéos datées, plans, projets).",
            TypeValidation.PORTFOLIO_MEDIA),
    MAITRE_ARTISAN("Évaluation par un maître artisan",
            "Observation de votre savoir-faire sur le terrain par un maître artisan référent habilité.",
            TypeValidation.MAITRE_ARTISAN);

    private final String libelle;
    private final String description;
    private final TypeValidation typeValidation;

    ModaliteValidation(String libelle, String description, TypeValidation typeValidation) {
        this.libelle = libelle;
        this.description = description;
        this.typeValidation = typeValidation;
    }

    public String libelle() {
        return libelle;
    }

    public String description() {
        return description;
    }

    /** Type de décision enregistré quand cette modalité aboutit à une demande d'évaluation. */
    public TypeValidation typeValidation() {
        return typeValidation;
    }
}
