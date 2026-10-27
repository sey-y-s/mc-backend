package odk.malicompetences.backend.auth;

import jakarta.validation.Valid;
import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final InscriptionService inscriptionService;
    private final ReinitialisationMotDePasseService reinitialisationService;
    private final JwtService jwtService;
    private final LoginAttemptLimiter loginAttemptLimiter;
    private final RefreshTokenService refreshTokenService;
    private final VerificationService verificationService;

    public AuthController(AuthService authService, InscriptionService inscriptionService,
                          ReinitialisationMotDePasseService reinitialisationService, JwtService jwtService, LoginAttemptLimiter loginAttemptLimiter,
                          RefreshTokenService refreshTokenService, VerificationService verificationService) {
        this.authService = authService;
        this.inscriptionService = inscriptionService;
        this.reinitialisationService = reinitialisationService;
        this.jwtService = jwtService;
        this.loginAttemptLimiter = loginAttemptLimiter;
        this.refreshTokenService = refreshTokenService;
        this.verificationService = verificationService;
    }

    /** Ajoute le jeton de renouvellement à une réponse de connexion ou d'inscription. */
    private LoginResponse avecRenouvellement(LoginResponse reponse) {
        UUID utilisateurId = jwtService.extractUtilisateurId(reponse.token());
        return new LoginResponse(reponse.token(), refreshTokenService.emettre(utilisateurId), jwtService.getExpiration());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public InscriptionResponse register(@Valid @RequestBody RegisterRequest request) {
        return inscriptionService.inscrire(request);
    }

    /** Vérification du téléphone ou de l'email : le bon code ouvre le compte et la session. */
    @PostMapping("/verify-otp")
    public LoginResponse verifierOtp(@Valid @RequestBody VerifyOtpRequest request) {
        Utilisateur utilisateur = verificationService.verifier(request);
        return avecRenouvellement(authService.ouvrirSession(utilisateur));
    }

    /** Nouveau code : toujours 204, que le compte existe ou non (une minute minimum entre deux envois). */
    @PostMapping("/resend-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void renvoyerOtp(@Valid @RequestBody ResendOtpRequest request) {
        verificationService.renvoyer(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String cle = request.identifiant().trim().toLowerCase(Locale.ROOT);
        loginAttemptLimiter.verifier(cle);
        try {
            LoginResponse reponse = authService.login(request);
            loginAttemptLimiter.reussite(cle);
            return avecRenouvellement(reponse);
        } catch (IllegalArgumentException exception) {
            loginAttemptLimiter.echec(cle);
            throw exception;
        }
    }

    @PutMapping("/mot-de-passe")
    @PreAuthorize("hasRole('CITOYEN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changerMotDePasse(
            @Valid @RequestBody ChangementMotDePasseRequest request,
            Authentication authentication
    ) {
        UUID utilisateurId = UUID.fromString(authentication.getName());
        authService.changerMotDePasse(utilisateurId, request);
        // Les autres appareils doivent se reconnecter avec le nouveau mot de passe.
        refreshTokenService.revoquerTous(utilisateurId);
    }

    /** Renouvellement de session : échange un jeton de renouvellement valide contre un nouvel accès. */
    @PostMapping("/refresh")
    public LoginResponse renouveler(@Valid @RequestBody RefreshRequest request) {
        return refreshTokenService.renouveler(request.refreshToken());
    }

    /** Déconnexion : le jeton est révoqué côté serveur, il ne peut plus être réutilisé. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @RequestBody(required = false) RefreshRequest corps) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            jwtService.revoquer(authorization.substring(7));
        }
        // La chaîne de renouvellement est coupée aussi : le jeton volé ne permettrait pas de rouvrir la session.
        if (corps != null) {
            refreshTokenService.revoquer(corps.refreshToken());
        }
    }

    /** Mot de passe oublié, étape 1 : toujours 204, que le compte existe ou non. */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void motDePasseOublie(@Valid @RequestBody ForgotPasswordRequest request) {
        reinitialisationService.demander(request);
    }

    /** Mot de passe oublié, étape 2 : code + nouveau mot de passe. */
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reinitialiserMotDePasse(@Valid @RequestBody ResetPasswordRequest request) {
        refreshTokenService.revoquerTous(reinitialisationService.reinitialiser(request));
    }
}
