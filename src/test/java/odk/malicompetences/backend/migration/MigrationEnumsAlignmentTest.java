package odk.malicompetences.backend.migration;

import odk.malicompetences.backend.besoin.StatutBesoin;
import odk.malicompetences.backend.citoyen.Sexe;
import odk.malicompetences.backend.common.statut.*;
import odk.malicompetences.backend.competence.NiveauCompetence;
import odk.malicompetences.backend.miseenrelation.StatutContact;
import odk.malicompetences.backend.opportunite.StatutOpportunite;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.talent.StatutSuiviBesoinTalent;
import odk.malicompetences.backend.territoire.NiveauTerritoire;
import odk.malicompetences.backend.testnumerique.TypeQuestion;
import odk.malicompetences.backend.utilisateur.Role;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Garde-fou SANS base de donnees : chaque contrainte CHECK de V1__schema_initial.sql qui liste des valeurs
 * ('A','B'...) doit correspondre EXACTEMENT a un enum Java. Ajouter une valeur d'un cote sans l'autre,
 * ou une nouvelle contrainte non declaree ici, fait echouer ce test.
 */
class MigrationEnumsAlignmentTest {

    private static final Path V1 = Path.of("src/main/resources/db/migration/V1__schema_initial.sql");

    /** nom de contrainte -> enum Java. */
    private static final Map<String, Class<? extends Enum<?>>> CHECKS = new LinkedHashMap<>();
    static {
        CHECKS.put("ck_utilisateur_statut", StatutCompte.class);
        CHECKS.put("ck_code_verif_canal", CanalVerification.class);
        CHECKS.put("ck_code_verif_usage", UsageCodeVerification.class);
        CHECKS.put("ck_territoire_niveau", NiveauTerritoire.class);
        CHECKS.put("ck_metier_comp_niveau", NiveauCompetence.class);
        CHECKS.put("ck_citoyen_sexe", Sexe.class);
        CHECKS.put("ck_citoyen_activite", StatutActivite.class);
        CHECKS.put("ck_localisation_source", SourceLocalisation.class);
        CHECKS.put("ck_cc_statut", StatutValidationCompetence.class);
        CHECKS.put("ck_cc_niveau_declare", NiveauCompetence.class);
        CHECKS.put("ck_cc_niveau_valide", NiveauCompetence.class);
        CHECKS.put("ck_cc_situation", SituationCompetence.class);
        CHECKS.put("ck_preuve_type", TypePreuveCitoyen.class);
        CHECKS.put("ck_preuve_statut", StatutPreuve.class);
        CHECKS.put("ck_centre_statut", StatutCentre.class);
        CHECKS.put("ck_domaine_centre_exclusif", TypeDomaineCentre.class);
        CHECKS.put("ck_evaluateur_statut", StatutEvaluateur.class);
        CHECKS.put("ck_habilitation_statut", StatutHabilitation.class);
        CHECKS.put("ck_question_type", TypeQuestion.class);
        CHECKS.put("ck_demande_statut", StatutDemandeValidation.class);
        CHECKS.put("ck_decision_validation", DecisionValidation.class);
        CHECKS.put("ck_decision_niveau", NiveauCompetence.class);
        CHECKS.put("ck_contestation_type", TypeObjetContestation.class);
        CHECKS.put("ck_contestation_statut", StatutContestation.class);
        CHECKS.put("ck_organisation_statut", StatutOrganisation.class);
        CHECKS.put("ck_justificatif_statut", StatutJustificatif.class);
        CHECKS.put("ck_besoin_statut", StatutBesoin.class);
        CHECKS.put("ck_besoin_comp_niveau", NiveauCompetence.class);
        CHECKS.put("ck_opportunite_statut", StatutOpportunite.class);
        CHECKS.put("ck_appareil_plateforme", PlateformeAppareil.class);
        CHECKS.put("ck_suivi_statut", StatutSuiviBesoinTalent.class);
        CHECKS.put("ck_mise_relation_statut", StatutContact.class);
        CHECKS.put("ck_actualisation_statut", StatutActualisationLocalisation.class);
    }

    private static final Pattern CONSTRAINT = Pattern.compile("CONSTRAINT (ck_\\w+) CHECK \\((.*)\\)[,]?\\s*$", Pattern.MULTILINE);
    private static final Pattern LITTERAL = Pattern.compile("'([A-Z][A-Z_]+)'");

    private static String sql() throws IOException {
        return Files.readString(V1);
    }

    private static Set<String> noms(Class<? extends Enum<?>> type) {
        Set<String> s = new TreeSet<>();
        Arrays.stream(type.getEnumConstants()).forEach(e -> s.add(e.name()));
        return s;
    }

    @Test
    void chaqueCheckAvecValeursCorrespondAUnEnum() throws IOException {
        // Les CHECK s'etendent sur plusieurs lignes pour certaines tables : on lit le texte jusqu'a la contrainte suivante.
        String texte = sql();
        Matcher m = Pattern.compile("CONSTRAINT (ck_\\w+) CHECK").matcher(texte);
        int verifies = 0;
        while (m.find()) {
            String nom = m.group(1);
            int debut = m.end();
            int fin = texte.length();
            Matcher suivant = Pattern.compile("CONSTRAINT |\\) ENGINE=").matcher(texte);
            if (suivant.find(debut)) fin = suivant.start();
            Set<String> valeurs = new TreeSet<>();
            Matcher l = LITTERAL.matcher(texte.substring(debut, fin));
            while (l.find()) valeurs.add(l.group(1));
            if (valeurs.isEmpty()) continue;                       // CHECK numerique ou de dates : rien a comparer
            Class<? extends Enum<?>> type = CHECKS.get(nom);
            assertNotNull(type, "Contrainte " + nom + " listant " + valeurs + " : declarer l'enum correspondant dans CHECKS");
            assertEquals(noms(type), valeurs, "Enum " + type.getSimpleName() + " different de " + nom);
            verifies++;
        }
        assertEquals(CHECKS.size(), verifies, "Une contrainte declaree dans CHECKS n'existe plus (ou n'a plus de valeurs) dans le SQL");
    }

    @Test
    void methodesDeValidationInsereesCorrespondentALEnum() throws IOException {
        String texte = sql();
        int i = texte.indexOf("INSERT INTO methode_validation");
        assertTrue(i > 0);
        String bloc = texte.substring(i, texte.indexOf(';', i));
        Set<String> codes = new TreeSet<>();
        Matcher l = Pattern.compile("UUID\\(\\), '([A-Z_]+)'").matcher(bloc);
        while (l.find()) codes.add(l.group(1));
        assertEquals(noms(MethodeValidationCode.class), codes);
    }

    @Test
    void rolesInsereesPlusCentreAdminDeduitCorrespondentALEnum() throws IOException {
        String texte = sql();
        int i = texte.indexOf("INSERT INTO role");
        String bloc = texte.substring(i, texte.indexOf(';', i));
        Set<String> codes = new TreeSet<>();
        Matcher l = Pattern.compile("UUID\\(\\), '([A-Z_]+)'").matcher(bloc);
        while (l.find()) codes.add(l.group(1));
        assertFalse(codes.contains("CENTRE_ADMIN"), "CENTRE_ADMIN se deduit de centre_administrateur : ne pas l'inserer");
        codes.add("CENTRE_ADMIN");
        assertEquals(noms(Role.class), codes);
    }

    @Test
    void lesTablesSupprimeesNeReviennentPas() throws IOException {
        String texte = sql();
        assertFalse(texte.contains("CREATE TABLE observation_position"), "Aucune coordonnee GPS ne doit etre stockee");
        assertFalse(texte.contains("CREATE TABLE indicateur_snapshot"), "Statistiques calculees a la demande en V1");
    }
}
