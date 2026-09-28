package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.commun.ConflitMetierException;
import odk.malicompetences.backend.commun.RessourceIntrouvableException;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitoyenServiceTest {

    @Mock
    private CitoyenRepository citoyenRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private CitoyenService citoyenService;

    @BeforeEach
    void setUp() {
        citoyenService = new CitoyenService(
                citoyenRepository,
                utilisateurRepository,
                new CitoyenMapper()
        );
    }

    @Test
    void creerEnregistreLeProfilEtLaDisponibiliteParDefaut() {
        UUID utilisateurId = UUID.randomUUID();
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(utilisateurId);
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(citoyenRepository.save(any(Citoyen.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitoyenResponse response = citoyenService.creer(
                utilisateurId,
                new CitoyenRequest("Awa", "Traore", Sexe.FEMME, null, null)
        );

        assertEquals(utilisateurId, response.id());
        assertEquals("Awa", response.nom());
        assertEquals("Traore", response.prenom());
        assertEquals("FEMME", response.sexe());
        assertEquals(Disponibilite.DISPONIBLE, response.disponibilite());
        assertTrue(response.codePasseport().matches("MC-\\d{4}-[A-F0-9]{6}"));
        verify(citoyenRepository).save(any(Citoyen.class));
    }

    @Test
    void creerEchoueSiUtilisateurAbsent() {
        UUID utilisateurId = UUID.randomUUID();
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> citoyenService.creer(
                        utilisateurId,
                        new CitoyenRequest("Awa", "Traore", Sexe.FEMME, null, null)
                )
        );
        verifyNoInteractions(citoyenRepository);
    }

    @Test
    void creerEchoueSiLeProfilExisteDeja() {
        UUID utilisateurId = UUID.randomUUID();
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(utilisateurId);
        when(utilisateurRepository.findById(utilisateurId)).thenReturn(Optional.of(utilisateur));
        when(citoyenRepository.existsById(utilisateurId)).thenReturn(true);

        assertThrows(
                ConflitMetierException.class,
                () -> citoyenService.creer(
                        utilisateurId,
                        new CitoyenRequest("Awa", "Traore", Sexe.FEMME, null, null)
                )
        );
        verify(citoyenRepository, never()).save(any());
    }
}
