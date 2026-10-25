package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CitoyenIdResolver {
    private final CitoyenRepository citoyenRepository;

    public CitoyenIdResolver(CitoyenRepository citoyenRepository) {
        this.citoyenRepository = citoyenRepository;
    }

    public UUID resoudre(String valeur, Authentication auth) {
        if (valeur == null || valeur.isBlank()) throw new IllegalArgumentException("L'identifiant du citoyen est obligatoire.");
        if (!"me".equalsIgnoreCase(valeur)) {
            try {
                return UUID.fromString(valeur);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("L'identifiant du citoyen doit être 'me' ou un UUID valide.");
            }
        }
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("Authentification requise.");
        }
        UUID utilisateurId;
        try {
            utilisateurId = UUID.fromString(auth.getName());
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.security.access.AccessDeniedException("Identité de session invalide.");
        }
        return citoyenRepository.findByUtilisateurId(utilisateurId)
                .map(Citoyen::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable pour le compte connecté."));
    }
}
