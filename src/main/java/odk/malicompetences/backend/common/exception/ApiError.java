package odk.malicompetences.backend.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        List<DetailErreur> details
) {
    public record DetailErreur(String champ, String message) {}

    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null, null);
    }

    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path, String code) {
        this(timestamp, status, error, message, path, code, null);
    }
}
