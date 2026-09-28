package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.commun.GestionnaireErreursApi;
import odk.malicompetences.backend.commun.ConflitMetierException;
import odk.malicompetences.backend.commun.RessourceIntrouvableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CitoyenControllerTest {

    @Mock
    private CitoyenService citoyenService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new CitoyenController(citoyenService))
                .setControllerAdvice(new GestionnaireErreursApi())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void creerRetourne400QuandLesChampsObligatoiresSontInvalides() throws Exception {
        mockMvc.perform(post("/api/citoyens/utilisateur/{utilisateurId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":" ","prenom":"","sexe":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête invalide"))
                .andExpect(jsonPath("$.errors.nom").exists())
                .andExpect(jsonPath("$.errors.prenom").exists())
                .andExpect(jsonPath("$.errors.sexe").exists());

        verifyNoInteractions(citoyenService);
    }

    @Test
    void trouverRetourne404QuandLeCitoyenEstAbsent() throws Exception {
        UUID citoyenId = UUID.randomUUID();
        when(citoyenService.trouverParId(citoyenId))
                .thenThrow(new RessourceIntrouvableException("Citoyen introuvable"));

        mockMvc.perform(get("/api/citoyens/{id}", citoyenId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ressource introuvable"))
                .andExpect(jsonPath("$.detail").value("Citoyen introuvable"));
    }

    @Test
    void creerRetourne409QuandLeProfilExisteDeja() throws Exception {
        UUID utilisateurId = UUID.randomUUID();
        when(citoyenService.creer(eq(utilisateurId), any(CitoyenRequest.class)))
                .thenThrow(new ConflitMetierException("Le profil citoyen existe déjà"));

        mockMvc.perform(post("/api/citoyens/utilisateur/{utilisateurId}", utilisateurId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Awa","prenom":"Traore","sexe":"FEMME"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflit métier"))
                .andExpect(jsonPath("$.detail").value("Le profil citoyen existe déjà"));
    }

}
