package odk.malicompetences.backend.common.security;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    private final odk.malicompetences.backend.utilisateur.RoleEffectif roleEffectif;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository,
                                    odk.malicompetences.backend.utilisateur.RoleEffectif roleEffectif) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleEffectif = roleEffectif;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findById(java.util.UUID.fromString(username))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        return User.builder()
                .username(utilisateur.getId().toString())
                .password(utilisateur.getPasswordHash())
                .disabled(!utilisateur.isActif())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleEffectif.de(utilisateur).name())))
                .build();
    }
}
