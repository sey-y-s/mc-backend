# Étape 4 — Validation et URLs canoniques

## Objectif A — API de validation
- La création citoyen passe par `/api/v1/citoyens/{citoyenId}/competences/{ccId}/demandes-validation` et utilise `methodeCode` ainsi que `evaluateurId` optionnel.
- La méthode `MAITRE_ARTISAN` exige un évaluateur habilité et conserve la demande en `EN_ATTENTE`.
- La création tient compte des validations expirées (`A_REVALIDER`) et évite une transition inutile lorsque la compétence est déjà `EN_COURS`.
- Ajout des routes citoyen pour la liste, la consultation, le complément et l'annulation des demandes. L'annulation crée une décision `ANNULEE`, met à jour la compétence et clôture la demande dans la transaction du service.
- Les routes personnel sont à plat sous `/api/v1/demandes-validation`. Les décisions utilisent `POST /{id}/decisions` (`201`) et restent immuables ; aucune route PUT/PATCH/DELETE sur les décisions.
- L'espace évaluateur courant est sous `/api/v1/evaluateurs/me`, avec profil, habilitations et demandes assignées filtrables par statut.
- Ajout des filtres `statut`, `methodeCode`, `competenceId`, de la restriction aux demandes assignées pour les évaluateurs, et des évaluations pratiques en lecture.

## Objectif B — URLs canoniques
- Les contrôleurs concernés utilisent uniquement les préfixes `/api/v1/...`.
- Suppression des alias `/regions` du contrôleur des territoires.
- Mise à jour des règles publiques GET pour les opportunités et catégories d'opportunités en `/api/v1/...`, tout en conservant les communes en lecture publique.

## Vérification
- Vérification statique des anciens préfixes et des routes de modification/suppression des décisions.
- Compilation et tests automatisés non exécutés dans cet environnement : Maven et le JAR du Maven Wrapper ne sont pas disponibles, et le projet cible Java 25 alors que l'environnement dispose de Java 21.
