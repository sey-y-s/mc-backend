package odk.malicompetences.backend.utilisateur;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.validation.NumeroTelephone;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilisateurContactServiceTest {

    @Test
    void metAJourEtNormaliseLesCoordonneesSansToucherAuRoleOuMotDePasse() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setTelephone("+22370000000");
        utilisateur.setEmail("old@example.com");
        utilisateur.setPasswordHash("unchanged-hash");
        utilisateur.setRole(Role.CITOYEN);
        when(repository.findById(utilisateur.getId())).thenReturn(Optional.of(utilisateur));
        when(repository.save(utilisateur)).thenReturn(utilisateur);
        UtilisateurService service = new UtilisateurService(
                repository, new UtilisateurMapper(), mock(PasswordEncoder.class),
                mock(PreferencesUtilisateurRepository.class)
        );

        UtilisateurResponse response = service.modifierCoordonnees(
                utilisateur.getId(),
                new UtilisateurContactRequest(" 70 00 00 01 ", " NEW@EXAMPLE.COM ")
        );

        assertEquals(NumeroTelephone.normaliser("70000001"), response.telephone());
        assertEquals("new@example.com", response.email());
        assertEquals("unchanged-hash", utilisateur.getPasswordHash());
        assertEquals(Role.CITOYEN, response.role());
    }

    @Test
    void refuseUnTelephoneDejaUtilise() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setTelephone("+22370000000");
        when(repository.findById(utilisateur.getId())).thenReturn(Optional.of(utilisateur));
        when(repository.existsByTelephone("+22370000001")).thenReturn(true);
        UtilisateurService service = new UtilisateurService(
                repository, new UtilisateurMapper(), mock(PasswordEncoder.class),
                mock(PreferencesUtilisateurRepository.class)
        );

        assertThrows(IllegalStateException.class, () ->
                service.modifierCoordonnees(
                        utilisateur.getId(),
                        new UtilisateurContactRequest("70000001", null)
                ));
        verify(repository, never()).save(any());
    }

    @Test
    void creationNormaliseTelephoneEtEmailPourPermettreLaConnexion() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        when(repository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UtilisateurService service = new UtilisateurService(
                repository, new UtilisateurMapper(), mock(PasswordEncoder.class),
                mock(PreferencesUtilisateurRepository.class)
        );

        UtilisateurResponse response = service.creerUtilisateur(
                new UtilisateurRequest(" 70 00 00 00 ", " ADMIN@Example.COM ", "motdepasse123", Role.ADMIN, null)
        );

        assertEquals(NumeroTelephone.normaliser("70000000"), response.telephone());
        assertEquals("admin@example.com", response.email());
    }

    @Test
    void suppressionDunUtilisateurInexistantRenvoieIntrouvable() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);
        UtilisateurService service = new UtilisateurService(
                repository, new UtilisateurMapper(), mock(PasswordEncoder.class),
                mock(PreferencesUtilisateurRepository.class)
        );

        assertThrows(ResourceNotFoundException.class, () -> service.supprimerUtilisateur(id));
    }
}
