package odk.malicompetences.backend.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Applique VRAIMENT les migrations Flyway sur MySQL/MariaDB puis fait valider par Hibernate que chaque
 * entite correspond au schema (ddl-auto=validate). C'est le test qui detecte une colonne oubliee, un type
 * ou un nom de table errone. Les autres tests utilisent H2 + create-drop et ne voient JAMAIS le vrai schema.
 *
 * <p>Il est ignore tant que TEST_DB_URL n'est pas defini. Utiliser une base VIDE dediee (jamais celle de
 * developpement : Flyway la migrerait) :
 * <pre>
 *   TEST_DB_URL=jdbc:mariadb://localhost:3306/malicompetences_test TEST_DB_USER=root TEST_DB_PASSWORD=root mvn test
 * </pre>
 * La compilation et les tests doivent être exécutés après chaque intégration ; ce test valide l'alignement final entre les entités et le schéma réel.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=${TEST_DB_URL}",
        "spring.datasource.username=${TEST_DB_USER:root}",
        "spring.datasource.password=${TEST_DB_PASSWORD:}",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=CHAR"
})
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class SchemaMigrationTest {

    @Test
    void migrationsAppliqueesEtEntitesConformesAuSchema() {
        // Le chargement du contexte (Flyway puis validation Hibernate) EST le test.
    }
}
