package odk.malicompetences.backend.organisation;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Pièces officielles des organisations (RCCM, NIF, récépissé, accord-cadre…). */
@RestController
public class OrganisationDocumentController {

    private final OrganisationDocumentService service;

    public OrganisationDocumentController(OrganisationDocumentService service) {
        this.service = service;
    }

    /** Référentiel : types d'organisation et pièces exigées (public, utilisé par le formulaire d'inscription). */
    @GetMapping({"/api/v1/auth/organisations/exigences", "/api/v1/organisations/exigences"})
    public List<ExigenceType> exigences() {
        return Arrays.stream(TypeOrganisation.values())
                .map(t -> new ExigenceType(t, t.getLibelle(),
                        t.getObligatoires().stream().map(d -> new TypeDocument(d, d.getLibelle())).toList(),
                        t.getConseillees().stream().map(d -> new TypeDocument(d, d.getLibelle())).toList()))
                .toList();
    }

    @GetMapping("/api/v1/organisations/me/documents")
    @PreAuthorize("hasRole('ORGANISATION')")
    public List<OrganisationDocumentResponse> mesDocuments(Authentication auth) {
        return service.lister(UUID.fromString(auth.getName()));
    }

    @PostMapping(path = {  "/api/v1/organisations/me/documents"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ORGANISATION')")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganisationDocumentResponse deposer(Authentication auth,
                                                @RequestParam("type") TypeDocumentOrganisation type,
                                                @RequestPart("fichier") MultipartFile fichier) {
        return service.deposer(UUID.fromString(auth.getName()), type, fichier);
    }

    @DeleteMapping("/api/v1/organisations/me/documents/{documentId}")
    @PreAuthorize("hasRole('ORGANISATION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(Authentication auth, @PathVariable UUID documentId) {
        service.supprimer(UUID.fromString(auth.getName()), documentId);
    }

    @GetMapping("/api/v1/organisations/{id}/documents")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<OrganisationDocumentResponse> documentsOrganisation(@PathVariable UUID id) {
        return service.lister(id);
    }

    @GetMapping("/api/v1/organisations/documents/{documentId}/fichier")
    @PreAuthorize("hasAnyRole('ORGANISATION', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> fichier(@PathVariable UUID documentId, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        OrganisationDocumentService.Fichier f = service.lire(documentId, UUID.fromString(auth.getName()), admin);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.typeContenu()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(f.nom(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(f.contenu());
    }

    @PatchMapping("/api/v1/organisations/documents/{documentId}/verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public OrganisationDocumentResponse verifier(@PathVariable UUID documentId,
                                                 @Valid @RequestBody VerificationDocumentRequest request) {
        return service.verifier(documentId, request);
    }

    public record TypeDocument(TypeDocumentOrganisation code, String libelle) {
    }

    public record ExigenceType(TypeOrganisation code, String libelle, List<TypeDocument> obligatoires, List<TypeDocument> conseillees) {
    }
}
