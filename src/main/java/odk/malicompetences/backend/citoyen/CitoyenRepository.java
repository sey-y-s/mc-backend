package odk.malicompetences.backend.citoyen;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CitoyenRepository extends JpaRepository<Citoyen, UUID> {
    java.util.Optional<Citoyen> findByIdentifiantPublic(UUID identifiantPublic);

    /** Retrouve le citoyen par l'id de son compte utilisateur (id du jeton JWT). */
    java.util.Optional<Citoyen> findByUtilisateurId(UUID utilisateurId);
}