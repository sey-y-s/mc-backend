package odk.malicompetences.backend.evaluateur;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.centre.HabilitationService;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.notification.NotificationRequest;
import odk.malicompetences.backend.notification.NotificationService;
import odk.malicompetences.backend.parcours.ParcoursValidationService;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.validation.TypeValidation;
import odk.malicompetences.backend.validation.Validation;
import odk.malicompetences.backend.validation.ValidationRepository;
import odk.malicompetences.backend.validation.ValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Espace évaluateur : alerte de désignation, et un évaluateur ne voit jamais ses propres demandes. */
class EvaluateurAlertesTest {

    private final EvaluateurCentreRepository assignments = mock(EvaluateurCentreRepository.class);
    private final CitoyenRepository citoyens = mock(CitoyenRepository.class);
    private final CentreRepository centres = mock(CentreRepository.class);
    private final UtilisateurRepository utilisateurs = mock(UtilisateurRepository.class);
    private final ValidationRepository validations = mock(ValidationRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final EvaluateurService service = new EvaluateurService(assignments, citoyens, centres, utilisateurs,
            validations, mock(ValidationService.class), mock(CitoyenCompetenceRepository.class),
            mock(ParcoursValidationService.class));

    EvaluateurAlertesTest() {
        service.setNotifications(notifications);
        service.setControles(mock(HabilitationService.class), mock(EvaluateurHabilitationRepository.class),
                mock(CompetenceRepository.class));
    }

    private static Citoyen citoyen(Role role) {
        Utilisateur u = new Utilisateur();
        u.setId(UUID.randomUUID());
        u.setRole(role);
        Citoyen c = new Citoyen();
        c.setId(u.getId());
        c.setUtilisateur(u);
        return c;
    }

    @Test
    void lAdministrateurDuCentrePrevientLeCitoyenDesigne() {
        Citoyen designe = citoyen(Role.CITOYEN);
        Centre centre = new Centre();
        centre.setId(UUID.randomUUID());
        centre.setNom("Centre de Ségou");
        Utilisateur admin = new Utilisateur();
        admin.setId(UUID.randomUUID());
        admin.setRole(Role.ADMIN);
        when(citoyens.findById(designe.getId())).thenReturn(Optional.of(designe));
        when(centres.findById(centre.getId())).thenReturn(Optional.of(centre));
        when(utilisateurs.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(assignments.findByCitoyenIdAndCentreId(designe.getId(), centre.getId())).thenReturn(Optional.empty());
        when(assignments.save(any(EvaluateurCentre.class))).thenAnswer(i -> i.getArgument(0));

        service.designer(new DesignationEvaluateurRequest(designe.getId(), centre.getId(), null), admin.getId().toString());

        ArgumentCaptor<NotificationRequest> n = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notifications).creerEtEnvoyerNotification(n.capture());
        assertEquals(List.of(designe.getId()), n.getValue().destinatairesIds());
        assertTrue(n.getValue().message().contains("Centre de Ségou"));
    }

    @Test
    void unEvaluateurNeVoitPasSaPropreDemandeDansSaListe() {
        Citoyen moi = citoyen(Role.CITOYEN);
        Citoyen autre = citoyen(Role.CITOYEN);
        Centre centre = new Centre();
        centre.setId(UUID.randomUUID());
        EvaluateurCentre affectation = new EvaluateurCentre();
        affectation.setCentre(centre);
        affectation.setCitoyen(moi);
        when(assignments.findByCitoyenIdAndActifTrue(moi.getId())).thenReturn(List.of(affectation));
        when(validations.findByCentreIdAndStatut(centre.getId(), StatutValidation.EN_ATTENTE))
                .thenReturn(List.of(demande(moi, centre), demande(autre, centre)));

        List<EvaluateurDemandeResponse> liste = service.demandes(moi.getId().toString());

        assertEquals(1, liste.size());
        assertEquals(autre.getId(), liste.get(0).citoyenId());
        verify(notifications, never()).creerEtEnvoyerNotification(any());
    }

    private static Validation demande(Citoyen demandeur, Centre centre) {
        CitoyenCompetence cc = new CitoyenCompetence();
        cc.setCitoyen(demandeur);
        Validation v = new Validation();
        v.setId(UUID.randomUUID());
        v.setCitoyenCompetence(cc);
        v.setCentre(centre);
        v.setType(TypeValidation.VALIDATION_PRATIQUE);
        v.setStatut(StatutValidation.EN_ATTENTE);
        v.setDateDemande(LocalDateTime.now());
        return v;
    }
}
