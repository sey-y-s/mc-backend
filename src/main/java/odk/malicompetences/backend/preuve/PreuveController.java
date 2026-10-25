package odk.malicompetences.backend.preuve;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint de téléchargement de fichier privé.
 * GET /api/v1/preuves/{id}/fichier
 * Accès : propriétaire ou ADMIN. Jamais d'URL publique dans les réponses.
 */
@RestController
@RequestMapping("/api/v1/preuves")
public class PreuveController {

    private final PreuveService preuveService;

    public PreuveController(PreuveService preuveService) {
        this.preuveService = preuveService;
    }

    @GetMapping("/{id}/fichier")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> telecharger(@PathVariable UUID id) {
        String cle = preuveService.cleStockage(id);
        Resource resource = preuveService.chargerFichier(id);
        MediaType contentType = preuveService.contentType(cle);
        String nomFichier = cle.substring(cle.lastIndexOf('/') + 1);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(nomFichier).build().toString())
                .body(resource);
    }
}
