package odk.malicompetences.backend.citoyen;

import java.util.UUID;

public record CitoyenResponse(
        UUID id,
        String codePasseport,
        String nom,
        String prenom,
        String sexe,
        String photoUrl,
        Disponibilite disponibilite,
        UUID communeId
) {
}