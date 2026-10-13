package odk.malicompetences.backend.notification;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationOwnershipTest {

    @Test
    void destinatairePeutMarquerSonLienCommeLu() {
        var liens = mock(NotificationDestinataireRepository.class);
        UUID userId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();
        NotificationDestinataire link = link(userId, false);
        when(liens.findByIdAndUtilisateurId(linkId, userId)).thenReturn(Optional.of(link));
        NotificationService service = service(liens);

        NotificationResponse response = service.marquerCommeLue(linkId, userId, false);

        assertTrue(response.lu());
        assertEquals(linkId, response.destinataireId());
        assertNotNull(response.dateLecture());
        verify(liens).save(link);
        verify(liens, never()).findById(linkId);
    }

    @Test
    void utilisateurNePeutPasMarquerLeLienDUnAutreCommeLu() {
        var liens = mock(NotificationDestinataireRepository.class);
        UUID userId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();
        when(liens.findByIdAndUtilisateurId(linkId, userId)).thenReturn(Optional.empty());
        NotificationService service = service(liens);

        assertThrows(ResourceNotFoundException.class, () ->
                service.marquerCommeLue(linkId, userId, false));
        verify(liens, never()).save(any());
    }

    private NotificationService service(NotificationDestinataireRepository links) {
        return new NotificationService(
                mock(NotificationRepository.class),
                links,
                mock(odk.malicompetences.backend.utilisateur.UtilisateurRepository.class),
                new NotificationMapper()
        );
    }

    private NotificationDestinataire link(UUID userId, boolean read) {
        Utilisateur user = new Utilisateur();
        user.setId(userId);
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setTitre("Validation");
        notification.setMessage("Votre compétence est validée");
        notification.setType(TypeNotification.VALIDATION);
        notification.setDateCreation(LocalDateTime.now());
        NotificationDestinataire link = new NotificationDestinataire();
        link.setId(UUID.randomUUID());
        link.setUtilisateur(user);
        link.setNotification(notification);
        link.setLu(read);
        link.setDateReception(LocalDateTime.now());
        return link;
    }
}
