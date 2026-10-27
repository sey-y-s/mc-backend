package odk.malicompetences.backend.citoyen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CitoyenRepository extends JpaRepository<Citoyen, UUID> {

    /** Citoyens domiciliés dans une commune (alerte des opportunités locales). */
    java.util.List<Citoyen> findByCommune_Id(UUID communeId);

    /** Citoyens domiciliés dans une région. */
    java.util.List<Citoyen> findByCommune_Region_Id(UUID regionId);

    /** Le fichier demandé est-il la photo de profil de ce citoyen ? */
    boolean existsByIdAndPhotoUrl(java.util.UUID id, String photoUrl);
}
