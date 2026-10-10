package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(request.identifiant())
                .orElseGet(() -> utilisateurRepository.findByEmail(request.identifiant())
                        .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects")));

        if (!utilisateur.isActif()
                || !passwordEncoder.matches(request.password(), utilisateur.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants incorrects");
        }

        return new LoginResponse(jwtService.generateToken(utilisateur));
    }

    public void inscrire(RegisterRequest request) {
        if (utilisateurRepository.existsByTelephone(request.telephone())) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé");
        }
        if (request.email() != null && !request.email().isBlank()
                && utilisateurRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }

        Role role = request.role() == null ? Role.CITOYEN : request.role();
        if (role != Role.CITOYEN && role != Role.ORGANISATION) {
            throw new IllegalArgumentException("Ce rôle ne peut pas être créé par inscription publique");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(request.telephone().trim());
        utilisateur.setEmail(request.email());
        utilisateur.setPasswordHash(passwordEncoder.encode(request.password()));
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }
}
