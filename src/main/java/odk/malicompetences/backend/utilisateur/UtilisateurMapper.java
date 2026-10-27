package odk.malicompetences.backend.utilisateur;

import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    public Utilisateur toEntity(UtilisateurRequest request, String encodedPassword) {
        if (request == null) return null;

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(request.telephone());
        utilisateur.setEmail(request.email());
        utilisateur.setPasswordHash(encodedPassword);
        utilisateur.setRole(request.role());
        // Compte ouvert par un administrateur : il se porte garant du contact, pas de code OTP à saisir.
        utilisateur.setTelephoneVerifieLe(java.time.LocalDateTime.now());

        if (request.actif() != null) {
            utilisateur.setActif(request.actif());
        } else {
            utilisateur.setActif(true);
        }

        return utilisateur;
    }

    public void updateEntity(UtilisateurRequest request, Utilisateur utilisateur, String encodedPassword) {
        if (request != null && utilisateur != null) {
            utilisateur.setTelephone(request.telephone());
            utilisateur.setEmail(request.email());
            utilisateur.setRole(request.role());

            if (encodedPassword != null && !encodedPassword.isBlank()) {
                utilisateur.setPasswordHash(encodedPassword);
            }

            if (request.actif() != null) {
                utilisateur.setActif(request.actif());
            }
        }
    }

    public UtilisateurResponse toResponse(Utilisateur utilisateur) {
        if (utilisateur == null) return null;

        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getTelephone(),
                utilisateur.getEmail(),
                utilisateur.isActif(),
                utilisateur.getRole(),
                utilisateur.getDateCreation()
        );
    }
}