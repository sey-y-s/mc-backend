package odk.malicompetences.backend.cv;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.Disponibilite;
import odk.malicompetences.backend.competence.*;
import odk.malicompetences.backend.experience.ExperienceRepository;
import odk.malicompetences.backend.portfolio.PortfolioMediaStorageService;
import odk.malicompetences.backend.testnumerique.ResultatTest;
import odk.malicompetences.backend.testnumerique.ResultatTestRepository;
import odk.malicompetences.backend.testnumerique.TestNumerique;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CvServiceTest {

    @Test
    void generatesPdfWithOnlyValidatedSkillsAndTestResults() throws Exception {
        UUID citizenId = UUID.randomUUID();
        Citoyen citoyen = new Citoyen();
        citoyen.setId(citizenId);
        citoyen.setNom("Traore");
        citoyen.setPrenom("Aminata");
        citoyen.setCodePasseport("MC-2026-123456");
        citoyen.setDisponibilite(Disponibilite.DISPONIBLE);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("aminata@example.com");
        utilisateur.setTelephone("+223 70000000");
        citoyen.setUtilisateur(utilisateur);

        Competence validated = competence("Gestion de projet");
        Competence declared = competence("Compétence non validée");
        CitoyenRepository citoyenRepository = mock(CitoyenRepository.class);
        CitoyenCompetenceRepository competenceRepository = mock(CitoyenCompetenceRepository.class);
        ExperienceRepository experienceRepository = mock(ExperienceRepository.class);
        ResultatTestRepository resultRepository = mock(ResultatTestRepository.class);
        PortfolioMediaStorageService mediaStorage = mock(PortfolioMediaStorageService.class);

        when(citoyenRepository.findById(citizenId)).thenReturn(Optional.of(citoyen));
        when(competenceRepository.findByCitoyenId(citizenId)).thenReturn(List.of(
                citizenCompetence(citoyen, validated, EtatCompetence.VALIDEE),
                citizenCompetence(citoyen, declared, EtatCompetence.DECLAREE)
        ));
        when(experienceRepository.findByCitoyenId(citizenId)).thenReturn(List.of());
        TestNumerique test = new TestNumerique();
        test.setTitre("Test de gestion de projet");
        ResultatTest result = new ResultatTest();
        result.setTest(test);
        result.setScore(new BigDecimal("85.00"));
        result.setReussi(true);
        when(resultRepository.findByCitoyenIdOrderByDatePassageDesc(citizenId))
                .thenReturn(List.of(result));

        CvService service = new CvService(
                citoyenRepository, competenceRepository, experienceRepository, resultRepository, mediaStorage);
        CvService.GeneratedCv generated = service.generate(citizenId);

        assertTrue(generated.content().length > 0);
        assertTrue(generated.fileName().endsWith(".pdf"));
        PdfReader reader = new PdfReader(new ByteArrayInputStream(generated.content()));
        String text = new PdfTextExtractor(reader).getTextFromPage(1);
        reader.close();
        assertTrue(text.contains("Aminata Traore"));
        assertTrue(text.contains("Gestion de projet"));
        assertFalse(text.contains("Compétence non validée"));
        assertTrue(text.contains("Test de gestion de projet"));
        assertTrue(text.contains("85"));
    }

    private Competence competence(String nom) {
        Competence competence = new Competence();
        competence.setNom(nom);
        return competence;
    }

    private CitoyenCompetence citizenCompetence(
            Citoyen citoyen,
            Competence competence,
            EtatCompetence etat
    ) {
        CitoyenCompetence citizenCompetence = new CitoyenCompetence();
        citizenCompetence.setCitoyen(citoyen);
        citizenCompetence.setCompetence(competence);
        citizenCompetence.setNiveau(NiveauCompetence.INTERMEDIAIRE);
        citizenCompetence.setEtat(etat);
        return citizenCompetence;
    }
}
