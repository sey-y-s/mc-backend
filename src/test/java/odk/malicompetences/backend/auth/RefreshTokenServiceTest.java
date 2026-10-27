package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    private JetonRenouvellementRepository repository;
    private UtilisateurRepository utilisateurRepository;
    private JwtService jwtService;
    private RefreshTokenService service;
    private Utilisateur utilisateur;
    private final List<JetonRenouvellement> enregistres = new ArrayList<>();

    @BeforeEach
    void setUp() {
        repository = mock(JetonRenouvellementRepository.class);
        utilisateurRepository = mock(UtilisateurRepository.class);
        jwtService = mock(JwtService.class);
        service = new RefreshTokenService(repository, utilisateurRepository, jwtService, 30);

        utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setRole(Role.CITOYEN);
        utilisateur.setActif(true);

        when(jwtService.generateToken(any(Utilisateur.class))).thenReturn("nouvel-acces");
        when(utilisateurRepository.findById(utilisateur.getId())).thenReturn(Optional.of(utilisateur));
        when(repository.save(any(JetonRenouvellement.class))).thenAnswer(invocation -> {
            JetonRenouvellement jeton = invocation.getArgument(0);
            enregistres.add(jeton);
            return jeton;
        });
    }

    private JetonRenouvellement dernier() {
        return enregistres.get(enregistres.size() - 1);
    }

    @Test
    void leJetonEstStockeSousFormeDEmpreinteSeulement() {
        String brut = service.emettre(utilisateur.getId());

        assertNotNull(brut);
        assertNotEquals(brut, dernier().getEmpreinte());
        assertEquals(RefreshTokenService.empreinte(brut), dernier().getEmpreinte());
    }

    @Test
    void unJetonValideDonneUnNouvelAccesEtUnNouveauJeton() {
        String brut = service.emettre(utilisateur.getId());
        JetonRenouvellement initial = dernier();
        when(repository.findByEmpreinte(initial.getEmpreinte())).thenReturn(Optional.of(initial));

        LoginResponse reponse = service.renouveler(brut);

        assertEquals("nouvel-acces", reponse.token());
        assertNotNull(reponse.refreshToken());
        assertNotEquals(brut, reponse.refreshToken());
        assertNotNull(initial.getUtiliseLe());
        assertEquals(initial.getFamilleId(), dernier().getFamilleId());
    }

    @Test
    void unJetonInconnuEstRefuseEn401() {
        when(repository.findByEmpreinte(anyString())).thenReturn(Optional.empty());

        ResponseStatusException erreur = assertThrows(ResponseStatusException.class, () -> service.renouveler("inconnu"));

        assertEquals(HttpStatus.UNAUTHORIZED, erreur.getStatusCode());
    }

    @Test
    void unJetonExpireEstRefuse() {
        String brut = service.emettre(utilisateur.getId());
        JetonRenouvellement jeton = dernier();
        jeton.setExpireLe(LocalDateTime.now().minusMinutes(1));
        when(repository.findByEmpreinte(jeton.getEmpreinte())).thenReturn(Optional.of(jeton));

        assertThrows(ResponseStatusException.class, () -> service.renouveler(brut));
    }

    @Test
    void unJetonRejoueLongtempsApresCoupeToutelaChaine() {
        String brut = service.emettre(utilisateur.getId());
        JetonRenouvellement jeton = dernier();
        jeton.setUtiliseLe(LocalDateTime.now().minusMinutes(5));
        when(repository.findByEmpreinte(jeton.getEmpreinte())).thenReturn(Optional.of(jeton));

        assertThrows(ResponseStatusException.class, () -> service.renouveler(brut));

        verify(repository).revoquerFamille(jeton.getFamilleId());
    }

    @Test
    void unDoubleAppelRapprocheNeCoupePasLaSession() {
        String brut = service.emettre(utilisateur.getId());
        JetonRenouvellement jeton = dernier();
        jeton.setUtiliseLe(LocalDateTime.now().minusSeconds(2));
        when(repository.findByEmpreinte(jeton.getEmpreinte())).thenReturn(Optional.of(jeton));

        assertThrows(ResponseStatusException.class, () -> service.renouveler(brut));

        verify(repository, never()).revoquerFamille(any());
    }

    @Test
    void uneChaineRevoqueeEstRefusee() {
        String brut = service.emettre(utilisateur.getId());
        JetonRenouvellement jeton = dernier();
        jeton.setRevoque(true);
        when(repository.findByEmpreinte(jeton.getEmpreinte())).thenReturn(Optional.of(jeton));

        assertThrows(ResponseStatusException.class, () -> service.renouveler(brut));
    }

    @Test
    void uneDeconnexionCoupeLaChaine() {
        String brut = service.emettre(utilisateur.getId());
        JetonRenouvellement jeton = dernier();
        when(repository.findByEmpreinte(jeton.getEmpreinte())).thenReturn(Optional.of(jeton));

        service.revoquer(brut);

        verify(repository).revoquerFamille(jeton.getFamilleId());
    }

    @Test
    void unCompteDesactiveNePeutPasRenouveler() {
        String brut = service.emettre(utilisateur.getId());
        JetonRenouvellement jeton = dernier();
        utilisateur.setActif(false);
        when(repository.findByEmpreinte(jeton.getEmpreinte())).thenReturn(Optional.of(jeton));

        assertThrows(ResponseStatusException.class, () -> service.renouveler(brut));

        verify(repository).revoquerFamille(jeton.getFamilleId());
    }
}
