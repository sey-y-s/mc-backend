package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.territoire.Commune;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfilCompletudeServiceTest {

    private final CitoyenCompetenceRepository competences = mock(CitoyenCompetenceRepository.class);
    private final ProfilCompletudeService service = new ProfilCompletudeService(competences);

    private Citoyen profilComplet() {
        Citoyen citoyen = new Citoyen();
        citoyen.setId(UUID.randomUUID());
        citoyen.setSexe(Sexe.HOMME);
        citoyen.setPhotoUrl("/api/v1/portfolio-medias/photo.jpg");
        citoyen.setCommune(mock(Commune.class));
        when(competences.existsByCitoyenIdAndEtatNot(citoyen.getId(), EtatCompetence.DECLAREE)).thenReturn(true);
        return citoyen;
    }

    @Test
    void unProfilCompletEstVisible() {
        Citoyen citoyen = profilComplet();

        assertTrue(service.estVisible(citoyen));
        assertDoesNotThrow(() -> service.exigerVisible(citoyen));
    }

    @Test
    void sansPhotoLeProfilEstInvisible() {
        Citoyen citoyen = profilComplet();
        citoyen.setPhotoUrl(null);

        assertFalse(service.estVisible(citoyen));
        verify(competences, never()).existsByCitoyenIdAndEtatNot(any(), any());
    }

    @Test
    void uneAdressePhotoVideNeComptePas() {
        Citoyen citoyen = profilComplet();
        citoyen.setPhotoUrl("   ");

        assertFalse(service.estVisible(citoyen));
    }

    @Test
    void sansSexeOuSansCommuneLeProfilEstInvisible() {
        Citoyen sansSexe = profilComplet();
        sansSexe.setSexe(null);
        Citoyen sansCommune = profilComplet();
        sansCommune.setCommune(null);

        assertFalse(service.estVisible(sansSexe));
        assertFalse(service.estVisible(sansCommune));
    }

    @Test
    void sansPremierePreuveLeProfilEstInvisibleEtIntrouvable() {
        Citoyen citoyen = profilComplet();
        when(competences.existsByCitoyenIdAndEtatNot(citoyen.getId(), EtatCompetence.DECLAREE)).thenReturn(false);

        assertFalse(service.estVisible(citoyen));
        assertThrows(ResourceNotFoundException.class, () -> service.exigerVisible(citoyen));
    }
}
