package odk.malicompetences.backend.utilisateur;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.StatutCompte;
import odk.malicompetences.backend.common.validation.NumeroTelephone;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilisateurContactServiceTest {
    private UtilisateurService service(UtilisateurRepository repository, RoleRepository roles) {
        return new UtilisateurService(repository, new UtilisateurMapper(roles), mock(PasswordEncoder.class),
                mock(PreferencesUtilisateurRepository.class));
    }

    @Test
    void metAJourEtNormaliseLesCoordonneesEtInvalideLaVerification() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setTelephone("+22370000000");
        utilisateur.setEmail("old@example.com");
        utilisateur.setMotDePasseHash("unchanged-hash");
        utilisateur.getRoles().add(new RoleEntity("CITOYEN", "Citoyen"));
        utilisateur.setStatut(StatutCompte.ACTIF);
        when(repository.findById(utilisateur.getId())).thenReturn(Optional.of(utilisateur));
        when(repository.save(utilisateur)).thenReturn(utilisateur);
        UtilisateurService service = service(repository, roles);

        UtilisateurResponse response = service.modifierCoordonnees(utilisateur.getId(),
                new UtilisateurContactRequest(" 70 00 00 01 ", " NEW@EXAMPLE.COM "));

        assertEquals(NumeroTelephone.normaliser("70000001"), response.telephone());
        assertEquals("new@example.com", response.email());
        assertEquals("unchanged-hash", utilisateur.getMotDePasseHash());
        assertTrue(response.roles().contains(Role.CITOYEN));
        assertEquals(StatutCompte.EN_ATTENTE, utilisateur.getStatut());
    }

    @Test
    void refuseUnTelephoneDejaUtilise() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(UUID.randomUUID());
        utilisateur.setTelephone("+22370000000");
        when(repository.findById(utilisateur.getId())).thenReturn(Optional.of(utilisateur));
        when(repository.existsByTelephone("+22370000001")).thenReturn(true);
        UtilisateurService service = service(repository, mock(RoleRepository.class));

        assertThrows(IllegalStateException.class, () -> service.modifierCoordonnees(
                utilisateur.getId(), new UtilisateurContactRequest("70000001", null)));
        verify(repository, never()).save(any());
    }

    @Test
    void creationNormaliseTelephoneEtEmail() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        when(roles.findByCode("ADMIN")).thenReturn(Optional.of(new RoleEntity("ADMIN", "Administrateur")));
        when(repository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.existsByTelephone(any())).thenReturn(false);
        when(repository.existsByEmail(any())).thenReturn(false);
        UtilisateurService service = new UtilisateurService(repository, new UtilisateurMapper(roles),
                passwordEncoderReturningHash(), mock(PreferencesUtilisateurRepository.class));

        UtilisateurResponse response = service.creerUtilisateur(
                new UtilisateurRequest(" 70 00 00 00 ", " ADMIN@Example.COM ", "motdepasse123", Role.ADMIN, true));

        assertEquals(NumeroTelephone.normaliser("70000000"), response.telephone());
        assertEquals("admin@example.com", response.email());
    }

    @Test
    void suppressionDunUtilisateurInexistantRenvoieIntrouvable() {
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service(repository, mock(RoleRepository.class)).supprimerUtilisateur(id));
    }

    private static PasswordEncoder passwordEncoderReturningHash() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("encoded-password");
        return encoder;
    }
}
