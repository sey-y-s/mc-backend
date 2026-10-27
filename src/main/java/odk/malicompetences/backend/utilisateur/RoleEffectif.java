package odk.malicompetences.backend.utilisateur;

import odk.malicompetences.backend.centre.CentreRepository;
import org.springframework.stereotype.Component;

/**
 * Rôle réellement appliqué à un compte (consignes V1.2).
 * <p>
 * {@code CENTRE_ADMIN} n'est pas un rôle générique : il découle uniquement du rattachement du compte
 * à un centre (centre.administrateur_id), fait par un administrateur national. Retirer le rattachement
 * retire le droit, sans autre manipulation. Les rôles nationaux (administration, organisation,
 * institution, agent de terrain) ne sont jamais remplacés.
 */
@Component
public class RoleEffectif {

    private final CentreRepository centreRepository;

    public RoleEffectif(CentreRepository centreRepository) {
        this.centreRepository = centreRepository;
    }

    public Role de(Utilisateur utilisateur) {
        Role stocke = utilisateur.getRole();
        if (stocke != null && stocke != Role.CITOYEN && stocke != Role.CENTRE_ADMIN && stocke != Role.EVALUATEUR) {
            return stocke;
        }
        if (centreRepository.findFirstByAdministrateurId(utilisateur.getId()).isPresent()) {
            return Role.CENTRE_ADMIN;
        }
        return stocke == Role.CENTRE_ADMIN ? Role.CITOYEN : stocke;
    }

    /** Un compte ne peut gérer un centre que s'il n'exerce pas déjà un rôle national. */
    public static boolean peutAdministrerUnCentre(Utilisateur utilisateur) {
        Role r = utilisateur.getRole();
        return r == null || r == Role.CITOYEN || r == Role.CENTRE_ADMIN || r == Role.EVALUATEUR;
    }
}
