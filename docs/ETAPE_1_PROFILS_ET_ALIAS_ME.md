# Étape 1 — Création des profils, identifiants de session et alias `me`

## Modifications

- `POST /api/v1/citoyens` crée le profil du compte connecté, réservé au rôle `CITOYEN`.
- `POST /api/v1/citoyens/utilisateur/{utilisateurId}` reste disponible uniquement au rôle `ADMIN`.
- `POST /api/v1/organisations` crée le profil du compte connecté, réservé au rôle `ORGANISATION`. Le statut est forcé à `EN_ATTENTE`; l'identifiant du compte n'est plus accepté dans `OrganisationRequest`.
- `GET /api/v1/auth/me` renvoie les identifiants des profils citoyen, organisation, centre et évaluateur lorsqu'ils existent.
- `CitoyenCompetence` initialise `declareeLe` et `modifieeLe` à la création et actualise `modifieeLe` à chaque mise à jour.
- `CitoyenIdResolver` résout `me` à partir du compte authentifié ou convertit une valeur UUID.
- Les routes de compétences citoyennes et d'expériences acceptent désormais `me` dans le segment `{citoyenId}`.
- Le module compétences citoyennes expose `GET /{id}` et `PATCH /{id}`. Le PATCH ne modifie que `niveauDeclare`, `situation` et `disponibilite` lorsqu'une valeur est fournie.
- La suppression d'une compétence est refusée en conflit si son statut n'est ni `NON_VERIFIEE` ni `REJETEE`, ou si une demande de validation existe, quel que soit le statut de cette demande.

## Vérifications

- Les références au champ `OrganisationRequest.utilisateurId()` ont été supprimées du code principal.
- Le repository de demandes de validation est utilisé pour protéger l'historique des compétences.
- Le build Maven n'a pas pu être exécuté dans cet environnement : le wrapper n'a pas pu télécharger Maven 3.9.16 depuis Maven Central.
