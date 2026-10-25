package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.otp.EnvoiCodeVerification;
import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.utilisateur.*;
import odk.malicompetences.backend.centre.CentreAdministrateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UtilisateurRepository utilisateurs;
    private RoleRepository roles;
    private CodeVerificationRepository codes;
    private PasswordEncoder encoder;
    private EnvoiCodeVerification envoi;
    private SessionRenouvellementRepository sessions;
    private AuthService service;

    @BeforeEach
    void setUp() {
        utilisateurs = mock(UtilisateurRepository.class);
        roles = mock(RoleRepository.class);
        codes = mock(CodeVerificationRepository.class);
        encoder = mock(PasswordEncoder.class);
        envoi = mock(EnvoiCodeVerification.class);
        sessions = mock(SessionRenouvellementRepository.class);
        when(roles.findByCode("CITOYEN")).thenReturn(Optional.of(new RoleEntity("CITOYEN", "Citoyen")));
        when(roles.findByCode("ORGANISATION")).thenReturn(Optional.of(new RoleEntity("ORGANISATION", "Organisation")));
        when(utilisateurs.existsByTelephone(anyString())).thenReturn(false);
        when(utilisateurs.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("hash");
        when(utilisateurs.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            if (u.getId() == null) u.setId(UUID.randomUUID());
            return u;
        });
        service = new AuthService(utilisateurs, roles, mock(CentreAdministrateurRepository.class), codes,
                encoder, mock(JwtService.class), envoi, sessions);
    }

    @Test
    void inscriptionCreeUnCompteEnAttenteEtEnvoieUnOtp() {
        RegisterResponse response = service.inscrire(new RegisterRequest(
                "70000000", "TEST@example.com", "motdepasse123", Role.CITOYEN, true));

        assertNotNull(response);
        verify(utilisateurs).save(any(Utilisateur.class));
        verify(codes).save(any(CodeVerification.class));
        verify(envoi).envoyer(any(), anyString(), any(), matches("\\d{6}"));
    }

    @Test
    void inscriptionSansAcceptationDesConditionsEstRefusee() {
        assertThrows(IllegalArgumentException.class, () -> service.inscrire(new RegisterRequest(
                "70000000", null, "motdepasse123", Role.CITOYEN, false)));
        verify(utilisateurs, never()).save(any(Utilisateur.class));
        verifyNoInteractions(codes, envoi);
    }

    @Test
    void inscriptionNePermetPasUnRolePrivilegie() {
        assertThrows(IllegalArgumentException.class, () -> service.inscrire(new RegisterRequest(
                "70000000", null, "motdepasse123", Role.ADMIN, true)));
        verify(utilisateurs, never()).save(any(Utilisateur.class));
    }
}
