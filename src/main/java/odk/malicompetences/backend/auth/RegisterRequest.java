package odk.malicompetences.backend.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import odk.malicompetences.backend.citoyen.Sexe;
import odk.malicompetences.backend.organisation.OrganisationIdentite;
import odk.malicompetences.backend.utilisateur.Role;

import java.util.UUID;

/** Données de création du compte et du profil métier associé. */
public record RegisterRequest(
        @NotBlank(message = "Le téléphone est obligatoire")
        @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
        String telephone,

        @Email(message = "L'email est invalide")
        @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
        @JsonAlias("motDePasse")
        String password,

        Role role,

        // Champs requis pour un compte citoyen.
        @Size(max = 100) String nom,
        @Size(max = 100) String prenom,
        Sexe sexe,
        @Size(max = 500) String photoUrl,
        UUID communeId,

        // Champs requis pour un compte organisation.
        @Size(max = 150) String nomOrganisation,
        String descriptionOrganisation,

        /** Identité juridique malienne de l'organisation (type, RCCM, NIF, récépissé…). */
        @Valid OrganisationIdentite organisation,

        /** Acceptation explicite des conditions d'utilisation (obligatoire, datée côté serveur). */
        Boolean conditionsAcceptees
) {
    public RegisterRequest(String telephone, String email, String password, Role role,
                           String nom, String prenom, Sexe sexe, String photoUrl, UUID communeId,
                           String nomOrganisation, String descriptionOrganisation, OrganisationIdentite organisation) {
        this(telephone, email, password, role, nom, prenom, sexe, photoUrl, communeId,
                nomOrganisation, descriptionOrganisation, organisation, null);
    }


    /** Compatibilité : inscription sans identité juridique détaillée. */
    public RegisterRequest(String telephone, String email, String password, Role role,
                           String nom, String prenom, Sexe sexe, String photoUrl, UUID communeId,
                           String nomOrganisation, String descriptionOrganisation) {
        this(telephone, email, password, role, nom, prenom, sexe, photoUrl, communeId,
                nomOrganisation, descriptionOrganisation, null, null);
    }
}
