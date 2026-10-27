package odk.malicompetences.backend.portfolio;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/portfolio-medias")
public class PortfolioMediaFileController {

    private final PortfolioMediaStorageService storage;
    private final PortfolioMediaAccessService accessService;

    public PortfolioMediaFileController(
            PortfolioMediaStorageService storage,
            PortfolioMediaAccessService accessService
    ) {
        this.storage = storage;
        this.accessService = accessService;
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> get(
            @PathVariable String fileName,
            Authentication authentication
    ) {
        accessService.verifierAcces(fileName, authentication);
        return ResponseEntity.ok()
                .contentType(storage.contentType(fileName))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=300")
                .body(storage.load(fileName));
    }
}
