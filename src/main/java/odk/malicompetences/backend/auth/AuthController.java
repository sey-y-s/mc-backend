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
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.inscrire(request);
    }

    @PostMapping("/verify-otp")
    public LoginResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/resend-otp")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/mot-de-passe")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changerMotDePasse(
            @Valid @RequestBody ChangementMotDePasseRequest request,
            Authentication authentication
    ) {
        authService.changerMotDePasse(
                UUID.fromString(authentication.getName()), request
        );
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.renouvelerSession(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authService.deconnecter(request == null ? null : request.refreshToken());
    }

    /** Mot de passe oublié, étape 1 : toujours 202, que le compte existe ou non. */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void motDePasseOublie(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.demanderReinitialisation(request);
    }

    /** Mot de passe oublié, étape 2 : code + nouveau mot de passe. */
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reinitialiserMotDePasse(@Valid @RequestBody ResetPasswordRequest request) {
        authService.reinitialiserMotDePasse(request);
    }
}
