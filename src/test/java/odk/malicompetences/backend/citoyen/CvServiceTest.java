package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.experience.Experience;
import odk.malicompetences.backend.experience.ExperienceRepository;
import odk.malicompetences.backend.preuve.Preuve;
import odk.malicompetences.backend.preuve.PreuveRepository;
import odk.malicompetences.backend.testnumerique.ResultatTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CvServiceTest {

    private final UUID id = UUID.randomUUID();
    private final CitoyenRepository citoyens = mock(CitoyenRepository.class);
    private final CitoyenCompetenceRepository competences = mock(CitoyenCompetenceRepository.class);
    private final ExperienceRepository experiences = mock(ExperienceRepository.class);
    private final ResultatTestRepository tests = mock(ResultatTestRepository.class);
    private final PreuveRepository preuves = mock(PreuveRepository.class);
    private final CvService service = new CvService(citoyens, competences, experiences, tests, preuves);

    @BeforeEach
    void preparer() {
        Citoyen citoyen = new Citoyen();
        citoyen.setId(id);
        citoyen.setNom("Traoré");
        citoyen.setPrenom("Awa");
        citoyen.setCodePasseport("ML-2026-000001");
        when(citoyens.findById(id)).thenReturn(Optional.of(citoyen));
        when(experiences.findByCitoyenId(any())).thenReturn(List.of());
        when(preuves.findByCitoyenCompetenceCitoyenId(any())).thenReturn(List.of());
        when(tests.findByCitoyenIdOrderByDatePassageDesc(any())).thenReturn(List.of());
    }

    private void competence(EtatCompetence etat) {
        Competence competence = new Competence();
        competence.setNom("Menuiserie");
        CitoyenCompetence cc = new CitoyenCompetence();
        cc.setCompetence(competence);
        cc.setNiveau(NiveauCompetence.INTERMEDIAIRE);
        cc.setEtat(etat);
        when(competences.findByCitoyenId(id)).thenReturn(List.of(cc));
    }

    @Test
    void genereUnPdfSigneAvecUneCompetenceValideeEtUneExperience() {
        competence(EtatCompetence.VALIDEE);
        when(experiences.findByCitoyenId(id)).thenReturn(List.of(new Experience()));

        CvService.GeneratedCv cv = service.generate(id);

        assertEquals("cv-" + id + ".pdf", cv.fileName());
        assertTrue(new String(cv.content(), 0, 4, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        assertTrue(cv.content().length > 1500);
    }

    @Test
    void uneSimplePreuveSuffitCommeContexte() {
        competence(EtatCompetence.VALIDEE);
        when(preuves.findByCitoyenCompetenceCitoyenId(id)).thenReturn(List.of(new Preuve()));

        assertTrue(service.generate(id).content().length > 1500);
    }

    @Test
    void sansCompetenceValideeLeCvEstRefuse() {
        competence(EtatCompetence.ATTESTEE);
        when(experiences.findByCitoyenId(id)).thenReturn(List.of(new Experience()));

        assertThrows(IllegalStateException.class, () -> service.generate(id));
    }

    @Test
    void sansExperienceNiPreuveLeCvEstRefuse() {
        competence(EtatCompetence.VALIDEE);

        assertThrows(IllegalStateException.class, () -> service.generate(id));
    }
}
