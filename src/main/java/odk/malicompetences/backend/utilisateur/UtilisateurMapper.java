package odk.malicompetences.backend.utilisateur;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UtilisateurMapper {

    private final RoleRepository roleRepository;

    public UtilisateurMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Utilisateur toEntity(UtilisateurRequest request, String encodedPassword) {
        if (request == null) return null;
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(request.telephone());
        utilisateur.setEmail(request.email());
        utilisateur.setMotDePasseHash(encodedPassword);
        appliquerRoles(utilisateur, rolesDemandes(request));
        if (request.actif() != null) utilisateur.setStatut(request.actif() ? odk.malicompetences.backend.common.statut.StatutCompte.ACTIF : odk.malicompetences.backend.common.statut.StatutCompte.SUSPENDU);
        return utilisateur;
    }

    public void updateEntity(UtilisateurRequest request, Utilisateur utilisateur, String encodedPassword) {
        if (request == null || utilisateur == null) return;
        utilisateur.setTelephone(request.telephone());
        utilisateur.setEmail(request.email());
        appliquerRoles(utilisateur, rolesDemandes(request));
        if (encodedPassword != null && !encodedPassword.isBlank()) utilisateur.setMotDePasseHash(encodedPassword);
        if (request.actif() != null) utilisateur.setStatut(request.actif() ? odk.malicompetences.backend.common.statut.StatutCompte.ACTIF : odk.malicompetences.backend.common.statut.StatutCompte.SUSPENDU);
    }

    public UtilisateurResponse toResponse(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        Set<Role> roles = utilisateur.getRoles() == null ? Set.of() : utilisateur.getRoles().stream()
                .map(RoleEntity::getCode).map(code -> {
                    try { return Role.valueOf(code); } catch (IllegalArgumentException e) { return null; }
                }).filter(java.util.Objects::nonNull).collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
        Role principal = roles.stream().sorted().findFirst().orElse(null);
        return new UtilisateurResponse(utilisateur.getId(), utilisateur.getTelephone(), utilisateur.getEmail(),
                utilisateur.getStatut() == odk.malicompetences.backend.common.statut.StatutCompte.ACTIF, principal, Set.copyOf(roles), utilisateur.getCreeLe());
    }

    private Set<Role> rolesDemandes(UtilisateurRequest request) {
        Set<Role> roles = request.roles() == null || request.roles().isEmpty()
                ? (request.role() == null ? Set.of() : Set.of(request.role()))
                : request.roles();
        if (roles.contains(Role.CENTRE_ADMIN)) {
            throw new IllegalArgumentException("Le rôle CENTRE_ADMIN est déduit de l'affectation à un centre.");
        }
        if (roles.isEmpty()) throw new IllegalArgumentException("Au moins un rôle est obligatoire.");
        return roles;
    }

    private void appliquerRoles(Utilisateur utilisateur, Set<Role> roles) {
        utilisateur.getRoles().clear();
        for (Role role : roles) {
            RoleEntity entity = roleRepository.findByCode(role.name())
                    .orElseThrow(() -> new IllegalStateException("Rôle absent du référentiel : " + role.name()));
            utilisateur.getRoles().add(entity);
        }
    }
}
