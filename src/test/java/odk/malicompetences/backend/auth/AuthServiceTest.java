package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.citoyen.Citoyen;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UtilisateurRepository utilisateurRepository;
    private CitoyenRepository citoyenRepository;
    private OrganisationRepository organisationRepository;
    private CommuneRepository communeRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService service;

    @BeforeEach
    void setUp() {
        utilisateurRepository = mock(UtilisateurRepository.class);
        citoyenRepository = mock(CitoyenRepository.class);
        organisationRepository = mock(OrganisationRepository.class);
        communeRepository = mock(CommuneRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        service = new AuthService(utilisateurRepository, citoyenRepository, organisationRepository,
                communeRepository, passwordEncoder, jwtService);

        when(utilisateurRepository.existsByTelephone(any())).thenReturn(false);
        when(utilisateurRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("motdepasse123")).thenReturn("hash-bcrypt");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur utilisateur = invocation.getArgument(0);
            utilisateur.setId(UUID.randomUUID());
            return utilisateur;
        });
    }

    @Test
    void inscriptionCitoyenCreeLeCompteEtLePasseport() {
        RegisterRequest request = new RegisterRequest(
                " 70000000 ", "TEST@EXAMPLE.COM", "motdepasse123", Role.CITOYEN,
                "Diallo", "Awa", Sexe.FEMME, null, null, null, null
        );

        service.inscrire(request);

        var captor = org.mockito.ArgumentCaptor.forClass(Citoyen.class);
        verify(citoyenRepository).save(captor.capture());
        Citoyen citoyen = captor.getValue();
        assertNotNull(citoyen.getId());
        assertEquals(citoyen.getUtilisateur().getId(), citoyen.getId());
        assertEquals("Diallo", citoyen.getNom());
        assertEquals(Sexe.FEMME, citoyen.getSexe());
        assertTrue(citoyen.getCodePasseport().matches("MC-\\d{4}-[A-F0-9]{6}"));
        assertEquals("70000000", citoyen.getUtilisateur().getTelephone());
        assertEquals("test@example.com", citoyen.getUtilisateur().getEmail());
        assertEquals("hash-bcrypt", citoyen.getUtilisateur().getPasswordHash());
        verifyNoInteractions(organisationRepository);
    }

    @Test
    void inscriptionCitoyenSansNomEstRefuseeAvantCreationDuCompte() {
        RegisterRequest request = new RegisterRequest(
                "70000000", null, "motdepasse123", Role.CITOYEN,
                " ", "Awa", null, null, null, null, null
        );

        assertThrows(IllegalArgumentException.class, () -> service.inscrire(request));
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
        verifyNoInteractions(citoyenRepository, organisationRepository);
    }

    @Test
    void inscriptionOrganisationCreeUnProfilEnAttente() {
        RegisterRequest request = new RegisterRequest(
                "76000000", null, "motdepasse123", Role.ORGANISATION,
                null, null, null, null, null, "Atelier Sugu", "Atelier local"
        );

        service.inscrire(request);

        var captor = org.mockito.ArgumentCaptor.forClass(odk.malicompetences.backend.organisation.Organisation.class);
        verify(organisationRepository).save(captor.capture());
        assertEquals("Atelier Sugu", captor.getValue().getNom());
        assertEquals(odk.malicompetences.backend.organisation.StatutOrganisation.EN_ATTENTE,
                captor.getValue().getStatut());
        verifyNoInteractions(citoyenRepository);
    }
}
