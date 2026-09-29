package odk.malicompetences.backend.utilisateur;

import java.time.LocalDateTime;
import java.util.UUID;

public record UtilisateurResponse(
        UUID id,
        String telephone,
        String email,
        boolean actif,
        Role role,
        LocalDateTime dateCreation
) {}