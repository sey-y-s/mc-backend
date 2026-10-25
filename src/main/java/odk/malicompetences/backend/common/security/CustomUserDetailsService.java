package odk.malicompetences.backend.common.security;

import odk.malicompetences.backend.centre.CentreAdministrateurRepository;
import odk.malicompetences.backend.common.statut.StatutCompte;
import odk.malicompetences.backend.utilisateur.RoleEntity;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final CentreAdministrateurRepository centreAdministrateurRepository;

    public CustomUserDetailsService(
            UtilisateurRepository utilisateurRepository,
            CentreAdministrateurRepository centreAdministrateurRepository
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.centreAdministrateurRepository = centreAdministrateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID id;
        try {
            id = UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Identifiant invalide : " + username);
        }

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (utilisateur.getRoles() != null) {
            for (RoleEntity role : utilisateur.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            }
        }
        if (centreAdministrateurRepository.existsById(utilisateur.getId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CENTRE_ADMIN"));
        }

        boolean actif = (utilisateur.getStatut() == StatutCompte.ACTIF);

        return User.builder()
                .username(utilisateur.getId().toString())
                .password(utilisateur.getMotDePasseHash() != null ? utilisateur.getMotDePasseHash() : "")
                .disabled(!actif)
                .authorities(authorities)
                .build();
    }
}
