package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Pièces justificatives des organisations : dépôt, consultation, vérification par l'administration. */
@Service
@Transactional
public class OrganisationDocumentService {

    private static final long TAILLE_MAX = 10L * 1024 * 1024;
    private static final Map<String, String> FORMATS = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png");

    private final OrganisationDocumentRepository documentRepository;
    private final OrganisationRepository organisationRepository;
    private final Path dossier;

    public OrganisationDocumentService(OrganisationDocumentRepository documentRepository,
                                       OrganisationRepository organisationRepository,
                                       @Value("${app.organisation.documents-directory:uploads/organisation-documents}") String dossier) {
        this.documentRepository = documentRepository;
        this.organisationRepository = organisationRepository;
        this.dossier = Path.of(dossier).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<OrganisationDocumentResponse> lister(UUID organisationId) {
        return documentRepository.findByOrganisationIdOrderByDateDepotDesc(organisationId).stream()
                .map(OrganisationDocumentResponse::from).toList();
    }

    public OrganisationDocumentResponse deposer(UUID organisationId, TypeDocumentOrganisation type, MultipartFile fichier) {
        if (type == null) throw new IllegalArgumentException("Le type de document est obligatoire.");
        if (fichier == null || fichier.isEmpty()) throw new IllegalArgumentException("Le fichier est vide.");
        if (fichier.getSize() > TAILLE_MAX) throw new IllegalArgumentException("Le fichier dépasse 10 Mo.");
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation introuvable."));

        String nom = fichier.getOriginalFilename() == null ? "document" : fichier.getOriginalFilename().replace('\\', '/');
        nom = nom.substring(nom.lastIndexOf('/') + 1).trim();
        String extension = nom.contains(".") ? nom.substring(nom.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        String typeContenu = FORMATS.get(extension);
        if (typeContenu == null) {
            throw new IllegalArgumentException("Format non accepté : déposez un PDF, un JPG ou un PNG.");
        }
        String stocke = UUID.randomUUID() + "." + extension;
        Path cible = dossier.resolve(stocke).normalize();
        if (!cible.getParent().equals(dossier)) throw new IllegalArgumentException("Nom de fichier invalide.");
        try {
            Files.createDirectories(dossier);
            try (InputStream in = fichier.getInputStream()) {
                Files.copy(in, cible, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d'enregistrer le document.", e);
        }

        OrganisationDocument d = new OrganisationDocument();
        d.setOrganisation(organisation);
        d.setTypeDocument(type);
        d.setNomFichier(nom.length() > 255 ? nom.substring(0, 255) : nom);
        d.setCheminFichier(stocke);
        d.setTypeContenu(typeContenu);
        d.setTailleOctets(fichier.getSize());
        d.setStatut(StatutDocumentOrganisation.EN_ATTENTE);
        d.setDateDepot(LocalDateTime.now());
        return OrganisationDocumentResponse.from(documentRepository.save(d));
    }

    public void supprimer(UUID organisationId, UUID documentId) {
        OrganisationDocument d = trouver(documentId);
        if (!d.getOrganisation().getId().equals(organisationId)) {
            throw new AccessDeniedException("Ce document n'appartient pas à votre organisation.");
        }
        if (d.getStatut() == StatutDocumentOrganisation.VALIDE) {
            throw new IllegalStateException("Un document déjà validé ne peut pas être supprimé.");
        }
        documentRepository.delete(d);
        try {
            Files.deleteIfExists(dossier.resolve(d.getCheminFichier()).normalize());
        } catch (IOException ignored) {
            // Le fichier orphelin n'empêche pas la suppression de l'enregistrement.
        }
    }

    /** Contenu du fichier : réservé à l'organisation propriétaire et à l'administration. */
    @Transactional(readOnly = true)
    public Fichier lire(UUID documentId, UUID utilisateurId, boolean administrateur) {
        OrganisationDocument d = trouver(documentId);
        if (!administrateur && !d.getOrganisation().getId().equals(utilisateurId)) {
            throw new AccessDeniedException("Accès refusé à ce document.");
        }
        Path chemin = dossier.resolve(d.getCheminFichier()).normalize();
        if (!chemin.getParent().equals(dossier) || !Files.exists(chemin)) {
            throw new ResourceNotFoundException("Fichier introuvable.");
        }
        try {
            return new Fichier(d.getNomFichier(), d.getTypeContenu(), Files.readAllBytes(chemin));
        } catch (IOException e) {
            throw new IllegalStateException("Lecture du document impossible.", e);
        }
    }

    public OrganisationDocumentResponse verifier(UUID documentId, VerificationDocumentRequest request) {
        if (request.statut() == StatutDocumentOrganisation.REJETE
                && (request.commentaire() == null || request.commentaire().isBlank())) {
            throw new IllegalArgumentException("Indiquez pourquoi le document est refusé.");
        }
        OrganisationDocument d = trouver(documentId);
        d.setStatut(request.statut());
        d.setCommentaire(request.commentaire() == null ? null : request.commentaire().trim());
        return OrganisationDocumentResponse.from(documentRepository.save(d));
    }

    private OrganisationDocument trouver(UUID id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document introuvable."));
    }

    public record Fichier(String nom, String typeContenu, byte[] contenu) {
    }
}
