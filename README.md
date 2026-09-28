# Mali Competences Backend

API backend Spring Boot pour la gestion des citoyens, compétences, organisations et opportunités.

## Prérequis

- Java 25
- MariaDB ou MySQL accessible localement

## Configuration

Choisissez un profil de base de données :

- `maria` : configuration dans `src/main/resources/application-maria.yml`
- `mysql` : configuration dans `src/main/resources/application-mysql.yml`

Avant de lancer l'application, définissez les identifiants de la base dans l'environnement. Par exemple, dans PowerShell :

```powershell
$env:DB_USERNAME = "malicompetences_app"
$env:DB_PASSWORD = "<mot-de-passe-local>"
```

Le profil MariaDB utilise par défaut `jdbc:mariadb://localhost:3306/malicompetences` ; le profil MySQL utilise `jdbc:mysql://localhost:3306/malicompetences`. Vous pouvez remplacer cette URL avec `DB_URL`. Utilisez un compte de base de données dédié avec les privilèges nécessaires ; ne stockez pas les identifiants dans les fichiers versionnés.

Flyway applique les migrations et Hibernate vérifie ensuite que le schéma correspond aux entités (`ddl-auto: validate`). La migration initiale crée les tables du modèle ; utilisez une base de développement neuve lors de sa première exécution. Cette version corrige la migration `V1` : si Flyway a déjà enregistré `V1` dans une base, ne lancez pas cette version contre cette base, car son empreinte peut différer. Dans ce cas, conservez l'ancienne migration et préparez une migration corrective versionnée avant déploiement ; ne faites pas de `flyway repair` sans examiner l'historique et sauvegarder la base.

## Lancer l'application

Depuis PowerShell, avec MariaDB :

```powershell
$env:DB_USERNAME = "malicompetences_app"
$env:DB_PASSWORD = "<mot-de-passe-local>"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=maria"
```

Pour MySQL, remplacez `maria` par `mysql`.

L'API écoute sur le port `8080`. La documentation OpenAPI est disponible à `/swagger-ui.html`.

## Accès API

L'API n'impose pas d'authentification HTTP. Les routes citoyen et les endpoints Actuator/OpenAPI sont donc accessibles sans identifiants. Réservez cette configuration aux tests locaux ou à un réseau de confiance ; ne l'exposez pas sur Internet sans ajouter une protection adaptée.

## Tests

Exécuter les tests unitaires de l'API citoyen :

```powershell
.\mvnw.cmd "-Dtest=CitoyenServiceTest,CitoyenControllerTest" test
```

La suite complète contient aussi un test de chargement du contexte Spring avec le profil MariaDB et nécessite les variables DB_* ainsi qu'une base accessible.
