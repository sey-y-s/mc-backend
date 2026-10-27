package odk.malicompetences.backend.organisation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.util.UUID;

@Entity
@Table(name = "organisation")
@Getter
@Setter
@NoArgsConstructor
public class Organisation {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutOrganisation statut = StatutOrganisation.EN_ATTENTE;

    @Column(name = "motif_statut", columnDefinition = "TEXT")
    private String motifStatut;

    // ----- Identité juridique (droit malien) -----

    @Enumerated(EnumType.STRING)
    @Column(name = "type_organisation", length = 40)
    private TypeOrganisation typeOrganisation;

    @Column(length = 30)
    private String sigle;

    /** Forme juridique précise : SARL, SA, SAS, SUARL, association reconnue d'utilité publique… */
    @Column(name = "forme_juridique", length = 80)
    private String formeJuridique;

    @Column(name = "secteur_activite", length = 150)
    private String secteurActivite;

    @Column(name = "date_creation")
    private java.time.LocalDate dateCreation;

    @Column
    private Integer effectif;

    /** Numéro d'immatriculation au Registre du commerce et du crédit mobilier (ex. MA.BKO.2026.B.12345). */
    @Column(name = "numero_rccm", length = 80)
    private String numeroRccm;

    /** Numéro d'identification fiscale délivré par la Direction générale des impôts. */
    @Column(length = 40)
    private String nif;

    /** Numéro d'employeur à l'Institut national de prévoyance sociale. */
    @Column(name = "numero_inps", length = 40)
    private String numeroInps;

    /** Récépissé de déclaration (associations et ONG). */
    @Column(name = "numero_recepisse", length = 80)
    private String numeroRecepisse;

    /** Accord-cadre signé avec l'État (ONG). */
    @Column(name = "numero_accord_cadre", length = 80)
    private String numeroAccordCadre;

    /** Référence de l'acte de création ou de l'agrément (structures publiques, organisations internationales). */
    @Column(name = "reference_acte", length = 150)
    private String referenceActe;

    // ----- Coordonnées et responsable -----

    @Column(length = 255)
    private String adresse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private odk.malicompetences.backend.territoire.Commune commune;

    @Column(name = "site_web", length = 255)
    private String siteWeb;

    @Column(name = "responsable_nom", length = 150)
    private String responsableNom;

    @Column(name = "responsable_fonction", length = 100)
    private String responsableFonction;

    @Column(name = "responsable_telephone", length = 30)
    private String responsableTelephone;

    /** Géolocalisation (facultative), en degrés décimaux WGS84. */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "date_position")
    private java.time.LocalDateTime datePosition;
}