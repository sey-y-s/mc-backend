package odk.malicompetences.backend.utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository
        extends JpaRepository<Utilisateur, UUID> {
    Optional<Utilisateur> findByRole(Role role);

    Optional<Utilisateur> findByTelephone(String telephone);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByTelephone(String telephone);

    boolean existsByEmail(String email);
}