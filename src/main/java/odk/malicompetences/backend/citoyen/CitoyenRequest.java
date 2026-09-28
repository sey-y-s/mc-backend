package odk.malicompetences.backend.citoyen;

public record CitoyenRequest(
        String nom,
        String prenom,
        String sexe,
        String photoUrl,
        Disponibilite disponibilite
) {
}