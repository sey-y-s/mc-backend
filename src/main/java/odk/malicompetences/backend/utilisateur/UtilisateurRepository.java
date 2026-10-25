package odk.malicompetences.backend.utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByTelephone(String telephone);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByTelephone(String telephone);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE r.code = :#{#role.name()}")
    Optional<Utilisateur> findByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u JOIN u.roles r WHERE r.code = :roleCode")
    boolean existsByRoleCode(@Param("roleCode") String roleCode);

    @Query("SELECT u FROM Utilisateur u WHERE u.telephone = :identifiant OR u.email = :identifiant")
    Optional<Utilisateur> findByTelephoneOrEmail(@Param("identifiant") String identifiant);
}
