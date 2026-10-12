package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.preuve.PreuveRepository;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CitoyenValidationServiceTest {

    private final ValidationRepository validationRepository = mock(ValidationRepository.class);
    private final ValidationService service = new ValidationService(
            validationRepository,
            mock(CitoyenCompetenceRepository.class),
            mock(PreuveRepository.class),
            mock(UtilisateurRepository.class),
            mock(CentreRepository.class),
            new ValidationMapper()
    );

    @Test
    void listMineFiltreParCitoyenEtRetourneUnDtoSansTelephoneEvaluateur() {
        UUID citoyenId = UUID.randomUUID();
        Validation validation = validation(citoyenId);
        when(validationRepository.findByCitoyenCompetenceCitoyenId(
                eq(citoyenId), any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(validation)));

        var page = service.listerPourCitoyen(citoyenId, null, 0, 20);

        assertEquals(1, page.getTotalElements());
        CitoyenValidationResponse response = page.getContent().getFirst();
        assertEquals("Menuiserie", response.competenceNom());
        assertEquals(StatutValidation.APPROUVEE, response.statut());
        assertEquals("Évaluateur", response.validateurLabel());
        assertEquals(validation.getDateDemande(), response.dateDemande());
        verify(validationRepository).findByCitoyenCompetenceCitoyenId(
                citoyenId, PageRequest.of(
                        0, 20, org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC,
                                "dateDemande"
                        )
                )
        );
    }

    @Test
    void listMineAppliqueLeFiltreStatutEtRefuseUnePaginationNonSure() {
        UUID citoyenId = UUID.randomUUID();
        when(validationRepository.findByCitoyenCompetenceCitoyenIdAndStatut(
                eq(citoyenId), eq(StatutValidation.REJETEE), any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of()));

        assertTrue(service.listerPourCitoyen(
                citoyenId, StatutValidation.REJETEE, 0, 25
        ).isEmpty());
        verify(validationRepository).findByCitoyenCompetenceCitoyenIdAndStatut(
                eq(citoyenId), eq(StatutValidation.REJETEE), any(PageRequest.class)
        );
        assertThrows(IllegalArgumentException.class, () ->
                service.listerPourCitoyen(citoyenId, null, 0, 101));
    }

    private Validation validation(UUID citoyenId) {
        var citoyen = new odk.malicompetences.backend.citoyen.Citoyen();
        citoyen.setId(citoyenId);
        Competence competence = new Competence();
        competence.setNom("Menuiserie");
        CitoyenCompetence citoyenCompetence = new CitoyenCompetence();
        citoyenCompetence.setId(UUID.randomUUID());
        citoyenCompetence.setCitoyen(citoyen);
        citoyenCompetence.setCompetence(competence);

        Validation validation = new Validation();
        validation.setId(UUID.randomUUID());
        validation.setCitoyenCompetence(citoyenCompetence);
        validation.setType(TypeValidation.VALIDATION_PRATIQUE);
        validation.setStatut(StatutValidation.APPROUVEE);
        validation.setDateDemande(LocalDateTime.now().minusDays(1));
        validation.setDateValidation(LocalDateTime.now());
        return validation;
    }
}
