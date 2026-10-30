package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.Sexe;
import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * L'ancien AuthService a été découpé : ce test couvre maintenant l'inscription
 * ({@link InscriptionService}), le mot de passe oublié ({@link ReinitialisationMotDePasseService})
 * et la connexion ({@link AuthService}).
 */
class AuthServiceTest {

    private UtilisateurRepository utilisateurRepository;
    private CitoyenRepository citoyenRepository;
    private OrganisationRepository organisationRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private ResetPasswordTokenRepository resetTokenRepository;
    private CodeSender codeSender;

    private InscriptionService inscription;
    private ReinitialisationMotDePasseService reinitialisation;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        utilisateurRepository = mock(UtilisateurRepository.class);
        citoyenRepository = mock(CitoyenRepository.class);
        organisationRepository = mock(OrganisationRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        resetTokenRepository = mock(ResetPasswordTokenRepository.class);
        codeSender = mock(CodeSender.class);
        VerificationService verificationService = mock(VerificationService.class);

        inscription = new InscriptionService(utilisateurRepository, passwordEncoder, verificationService, List.of(
                new ProfilCitoyenInitialiseur(citoyenRepository, mock(CommuneRepository.class))));
        reinitialisation = new ReinitialisationMotDePasseService(
                utilisateurRepository, resetTokenRepository, passwordEncoder, codeSender, verificationService);
        auth = new AuthService(utilisateurRepository, passwordEncoder, jwtService);

        when(utilisateurRepository.existsByTelephone(any())).thenReturn(false);
        when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("motdepasse123")).thenReturn("hash-bcrypt");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur utilisateur = invocation.getArgument(0);
            if (utilisateur.getId() == null) utilisateur.setId(UUID.randomUUID());
            return utilisateur;
        });
    }

    // ------------------------------------------------------------------ inscription

    @Test
    void inscriptionCitoyenSansNomEstRefuseeAvantCreationDuCompte() {
        RegisterRequest request = new RegisterRequest(
                "70000000", null, "motdepasse123", Role.CITOYEN,
                " ", "Awa", Sexe.FEMME, null, UUID.randomUUID(), null, null, null, true
        );

        assertThrows(IllegalArgumentException.class, () -> inscription.inscrire(request));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
        verifyNoInteractions(citoyenRepository, organisationRepository);
    }

    @Test
    void inscriptionSansAcceptationDesConditionsEstRefusee() {
        RegisterRequest request = new RegisterRequest(
                "70000000", null, "motdepasse123", Role.CITOYEN,
                "Diallo", "Awa", Sexe.FEMME, null, UUID.randomUUID(), null, null, null, false
        );

        assertThrows(IllegalArgumentException.class, () -> inscription.inscrire(request));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void inscriptionPubliqueRefuseUnRoleReserve() {
        RegisterRequest request = new RegisterRequest(
                "70000000", null, "motdepasse123", Role.ADMIN,
                null, null, null, null, null, null, null, null, true
        );

        assertThrows(IllegalArgumentException.class, () -> inscription.inscrire(request));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    // ------------------------------------------------------------------ mot de passe oublié

    @Test
    void motDePasseOublieSansCompteNeRevelePasQueLeCompteNexistePas() {
        when(utilisateurRepository.findByTelephone(any())).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail(any())).thenReturn(Optional.empty());

        reinitialisation.demander(new ForgotPasswordRequest("70000000"));

        verifyNoInteractions(codeSender);
        verifyNoInteractions(resetTokenRepository);
    }

    @Test
    void motDePasseOublieEnvoieUnCodeASixChiffres() {
        Utilisateur utilisateur = compte();
        when(utilisateurRepository.findByTelephone(any())).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode(matches("\\d{6}"))).thenReturn("hash-du-code");

        reinitialisation.demander(new ForgotPasswordRequest("70000000"));

        verify(resetTokenRepository).invaliderTous(utilisateur.getId());
        verify(codeSender).envoyer(eq(utilisateur), matches("\\d{6}"));
    }

    @Test
    void codeFauxIncrementeLesTentativesEtEstRefuse() {
        Utilisateur utilisateur = compte();
        when(utilisateurRepository.findByTelephone(any())).thenReturn(Optional.of(utilisateur));
        ResetPasswordToken jeton = new ResetPasswordToken();
        jeton.setCodeHash("hash-du-code");
        jeton.setExpireLe(LocalDateTime.now().plusMinutes(10));
        when(resetTokenRepository.findFirstByUtilisateurIdAndUtiliseFalseOrderByDateCreationDesc(any()))
                .thenReturn(Optional.of(jeton));
        when(passwordEncoder.matches("000000", "hash-du-code")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> reinitialisation.reinitialiser(
                new ResetPasswordRequest("70000000", "000000", "nouveau-mdp-123")));

        assertEquals(1, jeton.getTentatives());
        assertFalse(jeton.isUtilise());
    }

    // ------------------------------------------------------------------ connexion

    @Test
    void connexionAvecMauvaisMotDePasseEstRefusee() {
        Utilisateur utilisateur = compte();
        utilisateur.setPasswordHash("hash-bcrypt");
        when(utilisateurRepository.findByTelephone(any())).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("mauvais", "hash-bcrypt")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> auth.login(new LoginRequest("70000000", "mauvais")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void connexionDunCompteNonVerifieOuvreLEcranDuCode() {
        Utilisateur utilisateur = compte();
        utilisateur.setPasswordHash("hash-bcrypt");
        when(utilisateurRepository.findByTelephone(any())).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("motdepasse123", "hash-bcrypt")).thenReturn(true);

        assertThrows(CompteNonVerifieException.class, () -> auth.login(new LoginRequest("70000000", "motdepasse123")));
        verifyNoInteractions(jwtService);
    }

    private static Utilisateur compte() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setTelephone("70000000");
        utilisateur.setActif(true);
        return utilisateur;
    }
}
