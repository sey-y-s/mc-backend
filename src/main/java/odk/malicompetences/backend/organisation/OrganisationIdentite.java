package odk.malicompetences.backend.organisation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Identité juridique et coordonnées d'une organisation (inscription et profil).
 * Les numéros exigés dépendent du type (voir {@link OrganisationRegles}).
 */
public record OrganisationIdentite(
        TypeOrganisation typeOrganisation,
        @Size(max = 30) String sigle,
        @Size(max = 80) String formeJuridique,
        @Size(max = 150) String secteurActivite,
        @PastOrPresent(message = "La date de création ne peut pas être dans le futur") LocalDate dateCreation,
        @Min(0) @Max(1_000_000) Integer effectif,
        @Size(max = 80) String numeroRccm,
        @Size(max = 40) String nif,
        @Size(max = 40) String numeroInps,
        @Size(max = 80) String numeroRecepisse,
        @Size(max = 80) String numeroAccordCadre,
        @Size(max = 150) String referenceActe,
        @Size(max = 255) String adresse,
        UUID communeId,
        @Size(max = 255) String siteWeb,
        @Size(max = 150) String responsableNom,
        @Size(max = 100) String responsableFonction,
        @Size(max = 30) String responsableTelephone
) {
}
