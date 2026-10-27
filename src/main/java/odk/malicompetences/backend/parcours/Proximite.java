package odk.malicompetences.backend.parcours;

/** Position d'un centre par rapport au citoyen. L'ordre des valeurs sert au tri (du plus proche au plus lointain). */
public enum Proximite {
    MEME_COMMUNE("Dans votre commune"),
    MEME_REGION("Dans votre région"),
    INCONNUE("Localisation à confirmer"),
    AUTRE_REGION("Dans une autre région");

    private final String libelle;

    Proximite(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }
}
