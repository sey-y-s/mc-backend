package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.preuve.TypePreuve;

public record PreuveRequest(
        TypePreuve type,
        String titre,
        String description,
        String fichierUrl
) {
}