package odk.malicompetences.backend.portfolio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PortfolioMediaStorageService {

    public static final String MEDIA_URL_PREFIX = "/api/v1/portfolio-medias/";
    private static final long MAX_FILE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "webm");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final String SAFE_FILE_NAME = "^[0-9a-fA-F-]{36}\\.(jpg|jpeg|png|webp|mp4|mov|webm|pdf|doc|docx)$";

    private final Path directory;

    public PortfolioMediaStorageService(
            @Value("${app.media.storage-directory:uploads/portfolio}") String directory
    ) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, TypeMedia type) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est vide.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Le fichier dépasse 10 Mo.");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!isAllowed(type, extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le format du fichier ne correspond pas au type de média.");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path target = directory.resolve(storedName).normalize();
        if (!target.getParent().equals(directory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide.");
        }

        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible d'enregistrer le fichier.", exception);
        }

        return MEDIA_URL_PREFIX + storedName;
    }

    public Resource load(String fileName) {
        if (fileName == null || !fileName.matches(SAFE_FILE_NAME)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Média introuvable.");
        }
        Path file = directory.resolve(fileName).normalize();
        if (!file.getParent().equals(directory) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Média introuvable.");
        }
        return new FileSystemResource(file);
    }

    public MediaType contentType(String fileName) {
        String extension = extensionOf(fileName);
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "mp4" -> MediaType.parseMediaType("video/mp4");
            case "mov" -> MediaType.parseMediaType("video/quicktime");
            case "webm" -> MediaType.parseMediaType("video/webm");
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "doc" -> MediaType.parseMediaType("application/msword");
            case "docx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    public void delete(String mediaUrl) {
        if (mediaUrl == null || !mediaUrl.startsWith(MEDIA_URL_PREFIX)) return;
        String fileName = mediaUrl.substring(MEDIA_URL_PREFIX.length());
        if (!fileName.matches(SAFE_FILE_NAME)) return;
        Path file = directory.resolve(fileName).normalize();
        if (!file.getParent().equals(directory)) return;
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Le fichier orphelin pourra être nettoyé sans bloquer la réponse métier.
        }
    }

    private boolean isAllowed(TypeMedia type, String extension) {
        if (type == null) return false;
        return switch (type) {
            case IMAGE -> IMAGE_EXTENSIONS.contains(extension);
            case VIDEO -> VIDEO_EXTENSIONS.contains(extension);
            case DOCUMENT -> DOCUMENT_EXTENSIONS.contains(extension);
            case AUTRE -> false;
        };
    }

    private String extensionOf(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
