# Portage P4 — validation et évaluateurs

## Changements intégrés

- Le parcours principal utilise désormais `DemandeValidation` et `DecisionValidationEntity`. Les anciennes classes `Validation`, `ValidationService`, `ValidationRepository`, `ValidationMapper`, leurs DTO et les contrôleurs historiques ont été retirés.
- Une décision est ajoutée à l'historique ; aucun endpoint de modification ou suppression de décision n'est exposé.
- La décision, la mise à jour de `CitoyenCompetence`, la clôture de la demande et la notification sont exécutées dans la même transaction.
- `PolitiqueHabilitation.verifier` est appelé avant l'assignation et la décision. Le centre doit aussi être actif et avoir un agrément valable pour la compétence et la date de l'évaluation.
- Les habilitations sont ciblées par évaluateur, compétence, méthode, centre éventuel et période. Aucun évaluateur n'est choisi automatiquement.
- Les administrateurs peuvent créer des dossiers évaluateur, modifier leur statut, créer/activer des habilitations et créer des agréments centre-compétence. Les agréments sont ajoutés comme de nouvelles périodes plutôt que de remplacer les anciennes.
- Le citoyen peut transmettre un complément demandé ; la demande repasse à `EN_COURS` et l'évaluateur assigné est notifié.
- La table `competence_methode_validation` est vérifiée à la création de la demande.
- Les tests unitaires obsolètes qui dépendaient de l'ancien modèle ont été remplacés par des tests de la règle d'effet d'une décision.

## Routes P4

- `POST /api/v1/demandes-validation`
- `GET /api/v1/demandes-validation/mes-demandes`
- `POST /api/v1/demandes-validation/{id}/assignation` (ADMIN / SUPER_ADMIN)
- `POST /api/v1/demandes-validation/{id}/decision` (évaluateur assigné et habilité)
- `POST /api/v1/demandes-validation/{id}/complement` (propriétaire de la demande)
- `GET /api/v1/demandes-validation/{id}/decisions` (propriétaire, évaluateur assigné ou administrateur)
- `GET /api/v1/evaluateur/mes-habilitations`
- `GET /api/v1/evaluateur/demandes`
- `POST /api/v1/admin/evaluateurs` et `PATCH /api/v1/admin/evaluateurs/{id}/statut`
- `POST /api/v1/admin/habilitations` et `PATCH /api/v1/admin/habilitations/{id}/statut`
- `GET/POST /api/v1/admin/agrements-centres`

## Vérification

La compilation et les tests n'ont pas pu être exécutés dans cet environnement : le Maven Wrapper n'a pas pu télécharger Maven depuis Maven Central. Une compilation et une exécution des tests restent nécessaires dans l'environnement de développement avant fusion.
