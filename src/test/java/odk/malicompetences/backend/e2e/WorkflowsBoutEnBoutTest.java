package odk.malicompetences.backend.e2e;

import com.jayway.jsonpath.JsonPath;
import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreCompetence;
import odk.malicompetences.backend.centre.CentreCompetenceRepository;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.Sexe;
import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Les trois workflows principaux, joués de bout en bout sur les vraies routes (sécurité, services, base) :
 * 1) du compte à la compétence validée, 2) évaluation par un centre, 3) recherche et mise en relation.
 * Joué dans une base dédiée (malicompetences_e2e) et annulé à la fin de chaque scénario.
 * Lancement : mvnw test -Dtest=WorkflowsBoutEnBoutTest -De2e=true
 */
@EnabledIfSystemProperty(named = "e2e", matches = "true")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("maria")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:mariadb://localhost:3306/malicompetences_e2e?createDatabaseIfNotExist=true",
        "app.media.storage-directory=${java.io.tmpdir}/mc-e2e-uploads"
})
@Transactional
class WorkflowsBoutEnBoutTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @Autowired UtilisateurRepository utilisateurs;
    @Autowired CitoyenRepository citoyens;
    @Autowired CommuneRepository communes;
    @Autowired CompetenceRepository competences;
    @Autowired CitoyenCompetenceRepository citoyenCompetences;
    @Autowired CentreRepository centres;
    @Autowired CentreCompetenceRepository agrements;
    @Autowired OrganisationRepository organisations;

    Commune commune;
    Competence competence;
    Utilisateur admin;

    @BeforeEach
    void base() {
        commune = communes.findAll().get(0);
        competence = competences.findAll().stream()
                .filter(c -> c.getParcoursAutorises().size() == 2)
                .findFirst().orElseThrow();
        admin = compte(Role.ADMIN);
    }

    // ------------------------------------------------------------------ outils

    int compteur = 0;

    Utilisateur compte(Role role) {
        Utilisateur u = new Utilisateur();
        u.setTelephone("+2237" + String.format("%07d", (int) (Math.random() * 9_000_000) + compteur++));
        u.setEmail("e2e" + UUID.randomUUID() + "@test.ml");
        u.setPasswordHash("x");
        u.setRole(role);
        u.setActif(true);
        u.setTelephoneVerifieLe(LocalDateTime.now());
        return utilisateurs.saveAndFlush(u);
    }

    Citoyen citoyen(String prenom, boolean complet) {
        Utilisateur u = compte(Role.CITOYEN);
        Citoyen c = new Citoyen();
        c.setUtilisateur(u);
        c.setCodePasseport("MC-2026-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        c.setPrenom(prenom);
        c.setNom("Test");
        c.setSexe(Sexe.FEMME);
        c.setCommune(commune);
        if (complet) {
            c.setPhotoUrl("/api/v1/portfolio-medias/" + UUID.randomUUID() + ".png");
        }
        c = citoyens.saveAndFlush(c);
        if (complet) {
            CitoyenCompetence cc = new CitoyenCompetence();
            cc.setCitoyen(c);
            cc.setCompetence(competence);
            cc.setNiveau(NiveauCompetence.INTERMEDIAIRE);
            cc.setEtat(EtatCompetence.ATTESTEE);
            cc.setDateDeclaration(LocalDateTime.now());
            citoyenCompetences.saveAndFlush(cc);
        }
        return c;
    }

    String jeton(Utilisateur u) {
        return "Bearer " + jwt.generateToken(u);
    }

    String jeton(Citoyen c) {
        return jeton(c.getUtilisateur());
    }

    MvcResult appel(MockHttpServletRequestBuilder requete, String jeton, int attendu) throws Exception {
        return mvc.perform(requete.header("Authorization", jeton)).andExpect(status().is(attendu)).andReturn();
    }

    MvcResult get(String url, String jeton, int attendu) throws Exception {
        return appel(MockMvcRequestBuilders.get(url), jeton, attendu);
    }

    MvcResult envoyer(MockHttpServletRequestBuilder methode, String corps, String jeton, int attendu) throws Exception {
        return appel(methode.contentType(MediaType.APPLICATION_JSON).content(corps), jeton, attendu);
    }

    String corps(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString();
    }

    String id(MvcResult r) throws Exception {
        return JsonPath.read(corps(r), "$.id");
    }

    boolean alerte(UUID utilisateurId, String morceau) throws Exception {
        return corps(get("/api/v1/notifications/utilisateur/" + utilisateurId + "/non-lues", jeton(utilisateurs.findById(utilisateurId).orElseThrow()), 200))
                .contains(morceau);
    }

    // ------------------------------------------------------------------ workflow 1

    @Test
    void workflow1_duCompteALaCompetenceValidee() throws Exception {
        Citoyen awa = citoyen("Awa", false);
        String t = jeton(awa);

        // 1. déclaration de la compétence (secteur formel)
        String cc = id(envoyer(MockMvcRequestBuilders.post("/api/v1/citoyens/" + awa.getId() + "/competences"),
                "{\"competenceId\":\"" + competence.getId() + "\",\"niveau\":\"INTERMEDIAIRE\",\"parcours\":\"FORMEL\"}", t, 201));

        // 2. justificatif
        MockMultipartFile fichier = new MockMultipartFile("fichier", "diplome.pdf", "application/pdf", "%PDF-1.4 e2e".getBytes());
        String preuve = id(mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/citoyens/me/citoyen-competences/" + cc + "/preuves")
                .file(fichier).param("type", "DIPLOME").header("Authorization", t))
                .andExpect(status().is(201)).andReturn());

        // 3. le parcours recommande l'examen documentaire
        String parcours = corps(get("/api/v1/parcours-validation/competences/" + cc, t, 200));
        assertEquals("DOCUMENTAIRE", JsonPath.read(parcours, "$.recommandee"));

        // 4. envoi à l'examen : sans CV tant que rien n'est validé
        get("/api/v1/citoyens/me/cv", t, 409);
        envoyer(MockMvcRequestBuilders.post("/api/v1/evaluateur/demandes"),
                "{\"citoyenCompetenceId\":\"" + cc + "\",\"preuveId\":\"" + preuve + "\",\"type\":\"DIPLOME_VERIFIE\"}", t, 201);

        // 5. l'administrateur est prévenu, voit le justificatif, et approuve
        assertTrue(alerte(admin.getId(), "Justificatif à examiner"));
        String liste = corps(get("/api/v1/admin/validations", jeton(admin), 200));
        java.util.List<String> avecJustificatif = JsonPath.read(liste, "$[?(@.preuveUrl != null)].id");
        String validation = avecJustificatif.get(0);
        envoyer(MockMvcRequestBuilders.patch("/api/v1/admin/validations/" + validation + "/statut"),
                "{\"statut\":\"APPROUVEE\"}", jeton(admin), 200);

        // 6. la compétence est validée, le citoyen est prévenu, l'alerte se lit puis disparaît
        String competencesDeAwa = corps(get("/api/v1/citoyens/" + awa.getId() + "/competences", t, 200));
        assertEquals("VALIDEE", JsonPath.read(competencesDeAwa, "$[0].etat"));
        assertTrue(alerte(awa.getId(), "est validée"));
        String alertes = corps(get("/api/v1/notifications/utilisateur/" + awa.getId() + "/non-lues", t, 200));
        String liaison = JsonPath.read(alertes, "$[0].destinataireId");
        assertEquals(validation, JsonPath.read(alertes, "$[0].referenceId"), "l'alerte pointe vers la validation");
        appel(MockMvcRequestBuilders.patch("/api/v1/notifications/" + liaison + "/marquer-lue"), t, 200);
        assertEquals("[]", corps(get("/api/v1/notifications/utilisateur/" + awa.getId() + "/non-lues", t, 200)));

        // 7. CV disponible : compétence validée + preuve
        MvcResult cv = get("/api/v1/citoyens/me/cv", t, 200);
        assertEquals("application/pdf", cv.getResponse().getContentType());
    }

    // ------------------------------------------------------------------ workflow 2

    @Test
    void workflow2_evaluationParUnCentre() throws Exception {
        // mise en place par l'administration : centre actif, agréé, gestionnaire rattaché
        Citoyen gestionnaire = citoyen("Gestionnaire", true);
        Centre centre = new Centre();
        centre.setNom("Centre e2e");
        centre.setCommune(commune);
        centre.setAdministrateur(gestionnaire.getUtilisateur());
        centre = centres.saveAndFlush(centre);
        CentreCompetence agrement = new CentreCompetence();
        agrement.setCentre(centre);
        agrement.setCompetence(competence);
        agrement.setDateDebut(LocalDate.now().minusDays(1));
        agrements.saveAndFlush(agrement);

        // le gestionnaire désigne un évaluateur (citoyen ordinaire) qui est prévenu
        Citoyen evaluateur = citoyen("Evaluateur", true);
        envoyer(MockMvcRequestBuilders.post("/api/v1/centre-admin/evaluateurs"),
                "{\"citoyenId\":\"" + evaluateur.getId() + "\"}", jeton(gestionnaire), 200);
        assertTrue(alerte(evaluateur.getId(), "Vous êtes évaluateur"));
        String sesCentres = corps(get("/api/v1/evaluateur/mes-centres", jeton(evaluateur), 200));
        assertTrue(sesCentres.contains("Centre e2e"), "l'espace évaluateur existe pour le citoyen désigné");

        // un citoyen (secteur informel) est orienté vers le centre
        Citoyen moussa = citoyen("Moussa", true);
        String t = jeton(moussa);
        citoyenCompetences.findAll().stream().filter(c -> c.getCitoyen().getId().equals(moussa.getId())).forEach(citoyenCompetences::delete);
        citoyenCompetences.flush();
        String cc = id(envoyer(MockMvcRequestBuilders.post("/api/v1/citoyens/" + moussa.getId() + "/competences"),
                "{\"competenceId\":\"" + competence.getId() + "\",\"niveau\":\"INTERMEDIAIRE\",\"parcours\":\"INFORMEL\"}", t, 201));
        String parcours = corps(get("/api/v1/parcours-validation/competences/" + cc, t, 200));
        assertEquals("PRATIQUE_CENTRE", JsonPath.read(parcours, "$.recommandee"));

        String demande = id(envoyer(MockMvcRequestBuilders.post("/api/v1/evaluateur/demandes"),
                "{\"citoyenCompetenceId\":\"" + cc + "\",\"type\":\"VALIDATION_PRATIQUE\",\"centreId\":\"" + centre.getId() + "\"}", t, 201));

        // l'évaluateur est prévenu, voit la demande, décide avec un niveau
        assertTrue(alerte(evaluateur.getId(), "Nouvelle demande d'évaluation"));
        String aTraiter = corps(get("/api/v1/evaluateur/demandes", jeton(evaluateur), 200));
        assertTrue(aTraiter.contains(demande));
        envoyer(MockMvcRequestBuilders.patch("/api/v1/evaluateur/demandes/" + demande + "/decision"),
                "{\"statut\":\"APPROUVEE\",\"niveauValide\":\"EXPERT\"}", jeton(evaluateur), 200);

        // décision rendue : compétence validée, citoyen prévenu, historique de l'évaluateur
        String competencesDeMoussa = corps(get("/api/v1/citoyens/" + moussa.getId() + "/competences", t, 200));
        assertEquals("VALIDEE", JsonPath.read(competencesDeMoussa, "$[0].etat"));
        assertTrue(alerte(moussa.getId(), "est validée"));
        assertTrue(corps(get("/api/v1/evaluateur/historique", jeton(evaluateur), 200)).contains(demande));
        assertFalse(corps(get("/api/v1/evaluateur/demandes", jeton(evaluateur), 200)).contains(demande));

        // sécurité : un citoyen sans désignation n'a ni espace ni droit de décision
        assertEquals("[]", corps(get("/api/v1/evaluateur/mes-centres", t, 200)));
    }

    // ------------------------------------------------------------------ workflow 3

    @Test
    void workflow3_rechercheEtMiseEnRelation() throws Exception {
        Citoyen a = citoyen("Aminata", true);
        Citoyen b = citoyen("Bakary", true);
        Citoyen incomplet = citoyen("Incomplet", false);
        Utilisateur orgEnAttente = compte(Role.ORGANISATION);
        Organisation oa = new Organisation();
        oa.setUtilisateur(orgEnAttente);
        oa.setNom("Org en attente");
        organisations.saveAndFlush(oa);
        Utilisateur orgOk = compte(Role.ORGANISATION);
        Organisation ob = new Organisation();
        ob.setUtilisateur(orgOk);
        ob.setNom("Org approuvée");
        ob.setStatut(StatutOrganisation.APPROUVEE);
        organisations.saveAndFlush(ob);

        // recherche : ni soi-même, ni un profil incomplet ; un profil incomplet ne cherche pas
        String resultats = corps(get("/api/v1/talents/recherche", jeton(a), 200));
        assertTrue(resultats.contains(b.getId().toString()));
        assertFalse(resultats.contains(a.getId().toString()));
        assertFalse(resultats.contains(incomplet.getId().toString()));
        get("/api/v1/talents/recherche", jeton(incomplet), 409);

        // organisation : seulement une fois approuvée
        get("/api/v1/talents/recherche", jeton(orgEnAttente), 409);
        assertTrue(corps(get("/api/v1/talents/recherche", jeton(orgOk), 200)).contains(a.getId().toString()));

        // demande de A vers B ; un profil incomplet ne peut pas en envoyer
        String demande = id(envoyer(MockMvcRequestBuilders.post("/api/v1/demandes-mise-en-relation"),
                "{\"demandeurId\":\"" + a.getId() + "\",\"destinataireId\":\"" + b.getId()
                        + "\",\"statut\":\"EN_ATTENTE\",\"message\":\"Bonjour\"}", jeton(a), 201));
        envoyer(MockMvcRequestBuilders.post("/api/v1/demandes-mise-en-relation"),
                "{\"demandeurId\":\"" + incomplet.getId() + "\",\"destinataireId\":\"" + b.getId()
                        + "\",\"statut\":\"EN_ATTENTE\",\"message\":\"Bonjour\"}", jeton(incomplet), 409);

        // B est prévenu et l'alerte ouvre la demande ; A la voit dans ses envoyées, B dans ses reçues
        String alertes = corps(get("/api/v1/notifications/utilisateur/" + b.getId() + "/non-lues", jeton(b), 200));
        assertEquals(demande, JsonPath.read(alertes, "$[0].referenceId"));
        assertTrue(corps(get("/api/v1/demandes-mise-en-relation/mes-demandes", jeton(a), 200)).contains(demande));
        assertTrue(corps(get("/api/v1/demandes-mise-en-relation/mes-demandes", jeton(b), 200)).contains(demande));

        // avant acceptation : B reste anonyme pour A, et son profil complet est refusé
        assertTrue(corps(get("/api/v1/talents/recherche", jeton(a), 200)).contains("\"enRelation\":false"));
        get("/api/v1/talents/" + b.getId() + "/profil", jeton(a), 404);
        // l'émetteur ne peut pas répondre à sa propre demande
        envoyer(MockMvcRequestBuilders.patch("/api/v1/demandes-mise-en-relation/" + demande + "/reponse"),
                "{\"statut\":\"ACCEPTEE\"}", jeton(a), 403);

        // B accepte ; A est prévenu
        envoyer(MockMvcRequestBuilders.patch("/api/v1/demandes-mise-en-relation/" + demande + "/reponse"),
                "{\"statut\":\"ACCEPTEE\"}", jeton(b), 200);
        assertTrue(alerte(a.getId(), "acceptée"));

        // après acceptation : nom dans la recherche, profil complet visible, coordonnées transmises
        String apres = corps(get("/api/v1/talents/recherche", jeton(a), 200));
        assertTrue(apres.contains("Bakary Test"), "contact établi : plus anonyme");
        String profil = corps(get("/api/v1/talents/" + b.getId() + "/profil", jeton(a), 200));
        assertEquals("Bakary", JsonPath.read(profil, "$.prenom"));
        assertTrue(corps(get("/api/v1/demandes-mise-en-relation/" + demande, jeton(a), 200)).contains("contact"));

        // un tiers n'a rien : ni profil, ni demande des autres
        get("/api/v1/talents/" + b.getId() + "/profil", jeton(orgOk), 404);
        get("/api/v1/demandes-mise-en-relation/" + demande, jeton(incomplet), 403);
    }
}
