package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.notification.NotificationRequest;
import odk.malicompetences.backend.notification.NotificationService;
import odk.malicompetences.backend.notification.TypeNotification;
import odk.malicompetences.backend.preuve.PreuveRepository;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Le citoyen est prévenu de chaque décision, qu'elle vienne d'un administrateur ou d'un évaluateur. */
class DecisionNotificationTest {

    private final ValidationRepository validations = mock(ValidationRepository.class);
    private final CitoyenCompetenceRepository competences = mock(CitoyenCompetenceRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final ValidationService service = new ValidationService(validations, competences, mock(PreuveRepository.class),
            mock(UtilisateurRepository.class), mock(CentreRepository.class), new ValidationMapper());

    private final UUID citoyenId = UUID.randomUUID();
    private final UUID validationId = UUID.randomUUID();
    private final Validation validation = new Validation();

    @BeforeEach
    void preparer() {
        service.setNotificationService(notifications);
        Citoyen citoyen = new Citoyen();
        citoyen.setId(citoyenId);
        Competence competence = new Competence();
        competence.setNom("Soudure");
        CitoyenCompetence cc = new CitoyenCompetence();
        cc.setCitoyen(citoyen);
        cc.setCompetence(competence);
        cc.setEtat(EtatCompetence.ATTESTEE);
        validation.setId(validationId);
        validation.setCitoyenCompetence(cc);
        validation.setType(TypeValidation.DIPLOME_VERIFIE);
        validation.setStatut(StatutValidation.EN_ATTENTE);
        when(validations.findById(validationId)).thenReturn(Optional.of(validation));
        when(validations.save(any(Validation.class))).thenAnswer(i -> i.getArgument(0));
    }

    private NotificationRequest envoyee() {
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notifications).creerEtEnvoyerNotification(captor.capture());
        return captor.getValue();
    }

    @Test
    void uneApprobationPrevientLeCitoyen() {
        service.decider(validationId, StatutValidation.APPROUVEE, null, null, UUID.randomUUID());

        NotificationRequest n = envoyee();
        assertEquals(TypeNotification.VALIDATION, n.type());
        assertEquals(java.util.List.of(citoyenId), n.destinatairesIds());
        assertTrue(n.message().contains("Soudure"));
    }

    @Test
    void unRefusPrevientLeCitoyenAvecLeMotif() {
        service.decider(validationId, StatutValidation.REJETEE, "Pièce illisible", null, UUID.randomUUID());

        NotificationRequest n = envoyee();
        assertTrue(n.message().contains("Pièce illisible"));
    }

    @Test
    void leTestNumeriqueEnvoieSonPropreMessageSansDoublon() {
        validation.setType(TypeValidation.TEST_NUMERIQUE);

        service.decider(validationId, StatutValidation.APPROUVEE, null, null, UUID.randomUUID());

        verify(notifications, never()).creerEtEnvoyerNotification(any());
    }
}
