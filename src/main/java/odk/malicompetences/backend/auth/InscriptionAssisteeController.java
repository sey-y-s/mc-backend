package odk.malicompetences.backend.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import odk.malicompetences.backend.citoyen.Sexe;
import odk.malicompetences.backend.utilisateur.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Inscription assistée (consignes V1.2) : un agent de terrain ouvre le compte d'une personne qui ne peut
 * pas s'inscrire seule. L'agent est tracé (cree_par_id) et le consentement de la personne est obligatoire.
 * Les écrans complets de l'agent sont prévus après la V1.
 */
@RestController
@RequestMapping("/api/v1/agent/inscriptions")
@PreAuthorize("hasAnyRole('AGENT_TERRAIN','ADMIN','SUPER_ADMIN')")
public class InscriptionAssisteeController {

    private final InscriptionService inscriptionService;

    public InscriptionAssisteeController(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
    }

    public record InscriptionAssisteeRequest(
            @NotBlank(message = "Le téléphone est obligatoire") @Size(max = 30) String telephone,
            @Size(max = 255) String email,
            @NotBlank(message = "Le nom est obligatoire") @Size(max = 100) String nom,
            @NotBlank(message = "Le prénom est obligatoire") @Size(max = 100) String prenom,
            Sexe sexe,
            UUID communeId,
            @NotNull(message = "Indiquez si le consentement de la personne a été recueilli") Boolean consentementRecueilli
    ) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> inscrire(@Valid @RequestBody InscriptionAssisteeRequest r, Authentication auth) {
        UUID agentId;
        try {
            agentId = UUID.fromString(auth.getName());
        } catch (Exception e) {
            throw new AccessDeniedException("Identifiant utilisateur invalide.");
        }
        RegisterRequest personne = new RegisterRequest(r.telephone(), r.email(), null, Role.CITOYEN,
                r.nom(), r.prenom(), r.sexe(), null, r.communeId(), null, null, null, Boolean.TRUE);
        return inscriptionService.inscrireAssiste(personne, Boolean.TRUE.equals(r.consentementRecueilli()), agentId);
    }
}
