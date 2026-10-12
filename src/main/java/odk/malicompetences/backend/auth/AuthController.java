package odk.malicompetences.backend.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.inscrire(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/mot-de-passe")
    @PreAuthorize("hasRole('CITOYEN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changerMotDePasse(
            @Valid @RequestBody ChangementMotDePasseRequest request,
            Authentication authentication
    ) {
        authService.changerMotDePasse(
                UUID.fromString(authentication.getName()), request
        );
    }

    /** Déconnexion : le JWT est sans état, le mobile supprime son jeton. Route présente pour éviter un 404. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // rien à invalider côté serveur
    }

    /** Mot de passe oublié, étape 1 : toujours 204, que le compte existe ou non. */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void motDePasseOublie(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.demanderReinitialisation(request);
    }

    /** Mot de passe oublié, étape 2 : code + nouveau mot de passe. */
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reinitialiserMotDePasse(@Valid @RequestBody ResetPasswordRequest request) {
        authService.reinitialiser(request);
    }
}
