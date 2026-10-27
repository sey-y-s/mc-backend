package odk.malicompetences.backend.organisation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Pièce justificative déposée par une organisation (RCCM, NIF, récépissé…), vérifiée par l'administration. */
@Entity
@Table(name = "organisation_document")
@Getter
@Setter
@NoArgsConstructor
public class OrganisationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document", nullable = false, length = 40)
    private TypeDocumentOrganisation typeDocument;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "chemin_fichier", nullable = false, length = 500)
    private String cheminFichier;

    @Column(name = "type_contenu", length = 100)
    private String typeContenu;

    @Column(name = "taille_octets")
    private Long tailleOctets;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutDocumentOrganisation statut = StatutDocumentOrganisation.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot;
}
