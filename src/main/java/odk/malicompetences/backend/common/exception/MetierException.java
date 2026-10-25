package odk.malicompetences.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier avec code d'erreur stable.
 * Utilisable pour PolitiqueHabilitation.Refus ou toute autre règle métier.
 */
public class MetierException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public MetierException(String code, String message) {
        this(code, message, HttpStatus.FORBIDDEN);
    }

    public MetierException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

