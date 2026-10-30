package odk.malicompetences.backend.opportunite;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.ProfilCompletudeService;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Un profil incomplet ne peut pas postuler : il doit d'abord compléter son passeport. */
class CandidatureProfilCompletTest {

    @Test
    void unProfilIncompletNePeutPasPostuler() {
        OpportuniteRepository opportunites = mock(OpportuniteRepository.class);
        CandidatureOpportuniteRepository candidatures = mock(CandidatureOpportuniteRepository.class);
        CitoyenRepository citoyens = mock(CitoyenRepository.class);
        ProfilCompletudeService completude = mock(ProfilCompletudeService.class);
        CandidatureService service = new CandidatureService(opportunites, candidatures, citoyens,
                mock(CitoyenCompetenceRepository.class), mock(NotificationService.class), mock(OpportuniteMapper.class),
                completude);

        UUID citoyenId = UUID.randomUUID();
        UUID opportuniteId = UUID.randomUUID();
        Citoyen citoyen = new Citoyen();
        citoyen.setId(citoyenId);
        when(opportunites.findVisibleById(eq(opportuniteId), any(), any(LocalDate.class)))
                .thenReturn(Optional.of(new Opportunite()));
        when(citoyens.findById(citoyenId)).thenReturn(Optional.of(citoyen));
        when(completude.estVisible(citoyen)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> service.postuler(opportuniteId, citoyenId, null));
        verify(candidatures, never()).save(any());
    }
}
