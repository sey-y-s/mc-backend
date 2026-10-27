package odk.malicompetences.backend.institution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Défense fail-closed : l'espace ne s'active qu'après validation explicite du RBAC,
 * des périodes publiables, de la suppression complémentaire et de la recette confidentialité.
 */
@Component
public class InstitutionFeatureGate {
    private final boolean enabled;
    private final boolean confidentialityReviewed;

    public InstitutionFeatureGate(
            @Value("${app.institution.enabled:false}") boolean enabled,
            @Value("${app.institution.confidentiality-reviewed:false}") boolean confidentialityReviewed
    ) {
        this.enabled = enabled;
        this.confidentialityReviewed = confidentialityReviewed;
    }

    public void requireEnabled() {
        if (!enabled || !confidentialityReviewed) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Espace Institution désactivé : contrat de confidentialité et autorisations non validés."
            );
        }
    }
}
