package odk.malicompetences.backend.commun;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GestionnaireErreursApi {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> gererValidation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Les données fournies ne respectent pas les règles de validation."
        );
        problem.setTitle("Requête invalide");

        Map<String, String> erreurs = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                erreurs.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        problem.setProperty("errors", erreurs);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> gererCorpsInvalide() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Le corps de la requête est absent ou invalide."
        );
        problem.setTitle("Requête invalide");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ProblemDetail> gererRessourceIntrouvable(
            RessourceIntrouvableException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        problem.setTitle("Ressource introuvable");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ConflitMetierException.class)
    public ResponseEntity<ProblemDetail> gererConflit(ConflitMetierException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
        problem.setTitle("Conflit métier");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
