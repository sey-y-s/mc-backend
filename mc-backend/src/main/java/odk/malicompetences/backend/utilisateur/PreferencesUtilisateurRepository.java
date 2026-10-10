package odk.malicompetences.backend.utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PreferencesUtilisateurRepository
        extends JpaRepository<PreferencesUtilisateur, UUID> {
}
