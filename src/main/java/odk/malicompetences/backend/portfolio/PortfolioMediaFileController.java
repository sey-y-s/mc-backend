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

@RestController
@RequestMapping("/api/v1/portfolio-medias")
public class PortfolioMediaFileController {

    private final PortfolioMediaStorageService storage;

    public PortfolioMediaFileController(PortfolioMediaStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> get(@PathVariable String fileName) {
        return ResponseEntity.ok()
                .contentType(storage.contentType(fileName))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(fileName).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=300")
                .body(storage.load(fileName));
    }
}
