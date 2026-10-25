package odk.malicompetences.backend.util;

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
public class PrivateFileStorageService {

    private static final long MAX_FILE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "mp4", "mov", "webm", "pdf", "doc", "docx");
    private static final String SAFE_KEY_PATTERN =
            "^[0-9a-fA-F-]{36}\\.(jpg|jpeg|png|webp|mp4|mov|webm|pdf|doc|docx)$";

    private final Path directory;

    public PrivateFileStorageService(
            @Value("${app.media.storage-directory:uploads/preuves}") String directory
    ) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    /**
     * Valide et stocke le fichier. Renvoie une clé de stockage privé (UUID + extension).
     * Jamais d'URL publique.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est vide.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Le fichier dépasse 10 Mo.");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Format de fichier non pris en charge : " + extension);
        }
        String key = UUID.randomUUID() + "." + extension;
        Path target = directory.resolve(key).normalize();
        if (!target.getParent().equals(directory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide.");
        }
        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible d'enregistrer le fichier.", ex);
        }
        return key;
    }

    /**
     * Charge le fichier depuis sa clé privée. Lance 404 si introuvable ou clé invalide.
     */
    public Resource loadAsResource(String key) {
        if (key == null || !key.matches(SAFE_KEY_PATTERN)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable.");
        }
        Path file = directory.resolve(key).normalize();
        if (!file.getParent().equals(directory) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable.");
        }
        return new FileSystemResource(file);
    }

    /**
     * Renvoie le Content-Type adapté à l'extension de la clé.
     */
    public MediaType contentType(String key) {
        return switch (extensionOf(key)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png"         -> MediaType.IMAGE_PNG;
            case "webp"        -> MediaType.parseMediaType("image/webp");
            case "mp4"         -> MediaType.parseMediaType("video/mp4");
            case "mov"         -> MediaType.parseMediaType("video/quicktime");
            case "webm"        -> MediaType.parseMediaType("video/webm");
            case "pdf"         -> MediaType.APPLICATION_PDF;
            case "doc"         -> MediaType.parseMediaType("application/msword");
            case "docx"        -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default            -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    /**
     * Supprime le fichier physique en silence (erreur ignorée — archivage métier déjà fait).
     */
    public void delete(String key) {
        if (key == null || !key.matches(SAFE_KEY_PATTERN)) return;
        Path file = directory.resolve(key).normalize();
        if (!file.getParent().equals(directory)) return;
        try { Files.deleteIfExists(file); } catch (IOException ignored) { }
    }

    private String extensionOf(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

