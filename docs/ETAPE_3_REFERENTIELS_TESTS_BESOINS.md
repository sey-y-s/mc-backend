# Étape 3 — Référentiels, tests numériques et compétences des besoins

## Objectif A — Accès et sécurité
- Les lectures GET des référentiels territoires, secteurs d'activité, métiers, méthodes de validation et nomenclatures sont publiques. Les autres méthodes HTTP restent soumises à authentification.
- La consultation des questions d'un test exige une demande de validation appartenant au citoyen connecté, utilisant `TEST_NUMERIQUE`, au statut `EN_ATTENTE` ou `EN_COURS`, et portant sur la compétence du test. Les administrateurs peuvent consulter les questions sans demande.
- La soumission d'une tentative est réservée au rôle `CITOYEN` et applique les mêmes contrôles de demande.
- Les réponses de métadonnées des tests incluent le nombre de questions sans renvoyer leur contenu. Les options publiques sont nettoyées des marqueurs de correction.

## Objectif B — `besoin_competence`
- Colonnes renommées : `quantite` devient `nombre_requis` et `niveau_minimum` devient `niveau_requis` nullable.
- La clé primaire `id`, l'unicité `(besoin_id, competence_id)` et le nom de contrainte `ck_besoin_comp_niveau` sont conservés.
- Les requêtes de matching et l'indicateur Skills-Gap utilisent les nouveaux noms. Le niveau requis absent ne fixe aucun seuil minimal.
- Routes ajoutées : `GET` et `POST /api/v1/besoins/{besoinId}/competences`, `PUT` et `DELETE /api/v1/besoins/{besoinId}/competences/{competenceId}`.

## Vérification
- L'archive a été vérifiée avec `unzip -t`.
- La compilation et les tests automatisés n'ont pas été exécutés : Maven n'est pas installé et le JAR du Maven Wrapper est absent de l'archive.
- **La base doit être recréée** car le contenu de `V1__schema_initial.sql` a changé. Ne pas appliquer cette version à une base existante sans stratégie de migration adaptée.
