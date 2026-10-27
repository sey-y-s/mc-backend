package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentification : contrôle des identifiants, ouverture de session et changement du mot de passe.
 * L'inscription est dans {@link InscriptionService}, le mot de passe oublié dans
 * {@link ReinitialisationMotDePasseService}, la vérification des contacts dans {@link VerificationService}.
 */
@Service
@Transactional
public class AuthService {

    private static final String IDENTIFIANTS_INCORRECTS = "Identifiants incorrects";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Utilisateur utilisateur = AuthIdentifiant.chercher(utilisateurRepository, request.identifiant())
                .orElseThrow(() -> new IllegalArgumentException(IDENTIFIANTS_INCORRECTS));

        if (!utilisateur.isActif() || !passwordEncoder.matches(request.password(), utilisateur.getPasswordHash())) {
            throw new IllegalArgumentException(IDENTIFIANTS_INCORRECTS);
        }
        // Mot de passe correct mais aucun contact vérifié : pas de session, le client ouvre l'écran du code.
        if (!utilisateur.estVerifie()) {
            throw new CompteNonVerifieException();
        }
        return ouvrirSession(utilisateur);
    }

    /** Ouvre la session d'un compte dont un contact est vérifié (connexion, ou saisie du code OTP). */
    public LoginResponse ouvrirSession(Utilisateur utilisateur) {
        return new LoginResponse(jwtService.generateToken(utilisateur));
    }

    public void changerMotDePasse(UUID authenticatedUserId, ChangementMotDePasseRequest request) {
        if (!authenticatedUserId.equals(request.utilisateurId())) {
            throw new AccessDeniedException("Vous ne pouvez modifier que votre propre mot de passe");
        }
        Utilisateur utilisateur = utilisateurRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        if (!passwordEncoder.matches(request.ancienMotDePasse(), utilisateur.getPasswordHash())) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }
        if (passwordEncoder.matches(request.nouveauMotDePasse(), utilisateur.getPasswordHash())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent");
        }
        utilisateur.setPasswordHash(passwordEncoder.encode(request.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }
}
