package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.centre.CentreAdministrateur;
import odk.malicompetences.backend.centre.CentreAdministrateurRepository;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.evaluateur.EvaluateurRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.StatutCompte;
import odk.malicompetences.backend.utilisateur.RoleEntity;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthMeController {

    private final UtilisateurRepository repository;
    private final CentreAdministrateurRepository centreAdministrateurRepository;
    private final CitoyenRepository citoyenRepository;
    private final OrganisationRepository organisationRepository;
    private final EvaluateurRepository evaluateurRepository;

    public AuthMeController(
            UtilisateurRepository repository,
            CentreAdministrateurRepository centreAdministrateurRepository,
            CitoyenRepository citoyenRepository,
            OrganisationRepository organisationRepository,
            EvaluateurRepository evaluateurRepository
    ) {
        this.repository = repository;
        this.centreAdministrateurRepository = centreAdministrateurRepository;
        this.citoyenRepository = citoyenRepository;
        this.organisationRepository = organisationRepository;
        this.evaluateurRepository = evaluateurRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public SessionUtilisateur me(Authentication authentication) {
        UUID id;
        try {
            id = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.security.access.AccessDeniedException("Identité de session invalide.");
        }
        Utilisateur user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

        Set<String> roles = new HashSet<>();
        if (user.getRoles() != null) {
            roles.addAll(user.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet()));
        }

        Optional<CentreAdministrateur> centreAdmin = centreAdministrateurRepository.findById(user.getId());
        UUID centreId = null;
        if (centreAdmin.isPresent()) {
            roles.add("CENTRE_ADMIN");
            centreId = centreAdmin.get().getCentreId();
        }

        UUID citoyenId = citoyenRepository.findByUtilisateurId(user.getId())
                .map(citoyen -> citoyen.getId()).orElse(null);
        UUID organisationId = organisationRepository.findByUtilisateur_Id(user.getId())
                .map(organisation -> organisation.getId()).orElse(null);
        UUID evaluateurId = citoyenId == null ? null : evaluateurRepository.findByCitoyenId(citoyenId)
                .map(evaluateur -> evaluateur.getId()).orElse(null);

        return new SessionUtilisateur(
                user.getId(), user.getStatut(), roles,
                citoyenId, organisationId, centreId, evaluateurId
        );
    }

    public record SessionUtilisateur(
            UUID utilisateurId,
            StatutCompte statut,
            Set<String> roles,
            UUID citoyenId,
            UUID organisationId,
            UUID centreId,
            UUID evaluateurId
    ) {
        public UUID id() { return utilisateurId; }
        public String role() { return (roles != null && !roles.isEmpty()) ? roles.iterator().next() : null; }
        public boolean actif() { return statut == StatutCompte.ACTIF; }
    }
}
