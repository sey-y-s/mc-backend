package odk.malicompetences.backend.parcours;

/** Les 4 statuts d'avancement d'une compétence (affichés au citoyen et aux recruteurs). */
public enum StatutParcours {
    DECLAREE("Déclarée"),
    PREUVE_RECUE("Preuve reçue"),
    EN_COURS_EVALUATION("En cours d'évaluation"),
    VALIDEE("Validée");

    private final String libelle;

    StatutParcours(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
