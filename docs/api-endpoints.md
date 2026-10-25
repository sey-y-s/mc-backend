# MaliCompétences V1.1 — Signature des endpoints

Document de référence entre le backend et les clients (Flutter, Angular). Il décrit l'API **cible**, telle qu'elle apparaîtra dans Swagger quand le projet sera terminé. Il ne décrit pas l'API actuelle.

Il s'appuie sur `malicompetences_v1_1_schema.sql` et `malicompetences_v1_1_dictionnaire.md`. Toute modification d'un chemin ou d'un contrat se discute et se met à jour ici d'abord.

---

## 0. Conventions

### 0.1 Généralités
- **Base** : `/api/v1` pour toutes les routes (l'ancien code mélange `/api`, `/api/v1`, `/api/utilisateurs`…). JSON UTF-8, sauf fichiers.
- **Noms** : ressources au **pluriel**, en **français**, en **kebab-case** (`demandes-validation`). Les routes d'authentification gardent leurs noms existants (`register`, `login`, `forgot-password`…).
- **Champs JSON** : `camelCase`. **Identifiants** : UUID. **Dates** : ISO 8601 (`2026-10-22`, `2026-10-22T10:00:00`). **Énumérations** : exactement les valeurs du SQL (voir `GET /nomenclatures`).
- **Documentation** : `GET /v3/api-docs`, `GET /swagger-ui.html`. **Santé** : `GET /actuator/health` (public).

### 0.2 Authentification et accès
- `Authorization: Bearer <accessToken>`. Le jeton identifie un **compte** (`utilisateurId`), jamais un citoyen.
- Légende des accès : `PUBLIC` · `AUTH` (tout compte connecté) · `CIT` (citoyen) · `ORG` (organisation) · `CENTRE` (administrateur de **son** centre) · `EVAL` (évaluateur dont l'habilitation couvre la demande) · `AGENT` · `INST` · `ADMIN` (ADMIN ou SUPER_ADMIN) · `SUPER` (SUPER_ADMIN seul).
- Un accès marqué `CIT` ou `ORG` signifie « le propriétaire de la ressource ». `ADMIN` passe partout, sauf mention contraire.
- Un compte `EN_ATTENTE`, `SUSPENDU` ou `FERME` ne peut appeler que `/auth/*`.

### 0.3 Hiérarchie des ressources
1. On **imbrique** quand l'enfant n'existe pas sans son parent et que l'identifiant du parent est connu : `/citoyens/{citoyenId}/competences/{citoyenCompetenceId}/preuves`.
2. **Profondeur maximale : 3 niveaux de ressources** après la racine (`/citoyens/me/competences/{id}/demandes-validation/{id}` = 3 niveaux). Une **action** finale (`/annulation`, `/fichier`…) ne compte pas comme niveau. Au-delà, on passe à une route à plat avec son identifiant (exemple : `/demandes-validation/{demandeId}/tentatives`).
3. **Alias `me`** : toute route `/citoyens/me/...` existe aussi sous `/citoyens/{citoyenId}/...` (propriétaire, ADMIN, ou AGENT ayant créé le compte). Même règle pour `/organisations/me`, `/centres/me`, `/evaluateurs/me`, `/agents/me`. Ces routes sont **listées une seule fois, avec `me`**.
4. `{citoyenId}` est l'identifiant du **profil citoyen** (`citoyen.id`), pas celui du compte. `GET /auth/me` renvoie tous les identifiants utiles (`utilisateurId`, `citoyenId`, `organisationId`, `centreId`, `evaluateurId`).
5. Les routes **à plat** servent les vues transverses du personnel (évaluateurs, administrateurs) ; l'accès est contrôlé côté serveur, pas par le chemin.
6. Les organisations ne voient jamais l'identifiant interne d'un citoyen, seulement son `identifiantPublic`.

### 0.4 Verbes et statuts HTTP
| Besoin | Forme | Réponse |
|---|---|---|
| Lire | `GET` | `200` |
| Créer | `POST /collection` | `201` + en-tête `Location` + ressource |
| Remplacer (représentation modifiable complète) | `PUT` | `200` |
| Modifier partiellement | `PATCH` | `200` |
| Supprimer | `DELETE` | `204` |
| **Changer d'état** | `POST /{id}/{action}` (`publication`, `cloture`, `annulation`, `acceptation`…) | `200` + ressource mise à jour |
| Traitement différé | `POST` | `202` |

- Les **changements de statut ne passent jamais par `PATCH {statut}`** : une action nommée par transition, validée par la table de transitions du backend.
- Une **décision de validation** est une ressource créée (`POST .../decisions`) : jamais modifiée ni supprimée (`PUT`, `PATCH`, `DELETE` → `405`).
- `DELETE` sur un référentiel utilisé → `409` ; utiliser `PATCH {actif: false}`.

### 0.5 Listes, pagination, filtres
- Paramètres : `page` (défaut 0), `size` (défaut 20, max 100), `sort=champ,asc|desc`, `q` (recherche textuelle, 2 caractères minimum).
- Réponse paginée : `{ "content": [...], "page": 0, "size": 20, "totalElements": 137, "totalPages": 7 }`.
- Les filtres sont des paramètres de requête nommés comme les champs (`statut`, `communeId`…). Plusieurs valeurs : `statut=EN_ATTENTE,EN_COURS`.

### 0.6 Erreurs
Format actuel conservé et complété (champs ajoutés, aucun retiré) :
```json
{ "timestamp": "2026-10-22T10:00:00", "status": 409, "error": "Conflict",
  "message": "Transition interdite pour StatutBesoin : CLOTURE -> OUVERT",
  "path": "/api/v1/besoins/…/publication",
  "code": "TRANSITION_INTERDITE", "details": [ { "champ": "email", "message": "Adresse email invalide" } ] }
```
| Statut | Usage |
|---|---|
| `400` | Données invalides (`details` pour les champs) |
| `401` | Jeton absent, expiré ou invalide |
| `403` | Accès interdit, compte non vérifié, habilitation insuffisante (`code` stable : `HABILITATION_EXPIREE`, `AUTO_EVALUATION`, `ORGANISATION_NON_VERIFIEE`…) |
| `404` | Ressource inconnue **ou non visible** pour l'appelant |
| `409` | Conflit d'état ou de contrainte (transition interdite, doublon, ressource référencée) |
| `422` | Règle métier non satisfaite (compétence déjà validée, méthode non active…) |
| `429` | Trop de tentatives (OTP, connexion) |

### 0.7 Fichiers
- Aucune **URL publique** de fichier : les fichiers se téléchargent uniquement par une route `.../fichier` qui vérifie l'accès.
- Dépôt : `multipart/form-data` avec la partie `fichier` et la partie `metadonnees` (JSON). Taille et types limités par configuration (`MEDIA_MAX_FILE_SIZE`).

### 0.8 Confidentialité (s'applique à toutes les routes)
- Aucune coordonnée GPS n'est stockée ni journalisée.
- Les réponses attendues des tests ne sont jamais renvoyées au candidat.
- Les statistiques publiques masquent les petits effectifs (voir section 13).

---

## 1. Authentification et session

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| POST | `/auth/register` | PUBLIC | Inscription. Corps : `telephone?`, `email?` (au moins un), `password`, `role` (`CITOYEN` ou `ORGANISATION` seulement), `conditionsAcceptees` (obligatoire). Crée un compte `EN_ATTENTE` et envoie un OTP. `201` |
| POST | `/auth/verify-otp` | PUBLIC | Valide un OTP : `{ destinataire, code, usage }`. Pour `INSCRIPTION`, active le compte et renvoie les jetons |
| POST | `/auth/resend-otp` | PUBLIC | Renvoie un OTP : `{ destinataire, usage }`. `202`. Limité en fréquence (`429`) |
| POST | `/auth/login` | PUBLIC | `{ identifiant (téléphone ou email), password }` → `{ accessToken, refreshToken, expiresIn, utilisateur }`. `403 COMPTE_NON_VERIFIE` si `EN_ATTENTE` |
| POST | `/auth/refresh` | PUBLIC | `{ refreshToken }` → nouveau couple de jetons (rotation) |
| POST | `/auth/logout` | AUTH | Révoque le refresh token fourni. `204` |
| POST | `/auth/logout-all` | AUTH | Révoque toutes les sessions du compte. `204` |
| POST | `/auth/forgot-password` | PUBLIC | `{ identifiant }`. `202` **toujours** (ne révèle pas l'existence du compte) |
| POST | `/auth/reset-password` | PUBLIC | `{ identifiant, code, nouveauMotDePasse }`. Révoque les sessions |
| PUT | `/auth/mot-de-passe` | AUTH | `{ ancienMotDePasse, nouveauMotDePasse }`. Révoque les autres sessions |
| GET | `/auth/me` | AUTH | Session : compte, statut, `roles` (dont `CENTRE_ADMIN` déduit), `citoyenId`, `organisationId`, `centreId`, `evaluateurId` (quand ils existent) |

## 2. Compte courant (`/utilisateurs/me`)

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/utilisateurs/me` | AUTH | Données du compte (contacts masqués partiellement, vérifications, conditions acceptées) |
| POST | `/utilisateurs/me/contacts/changements` | AUTH | Demande de changement de contact : `{ canal, valeur }` → OTP vers la **nouvelle** valeur. `202` |
| POST | `/utilisateurs/me/contacts/changements/verification` | AUTH | `{ code }` : applique le changement. Un compte garde toujours au moins un contact |
| GET | `/utilisateurs/me/preferences` | AUTH | `{ modeEconomieDonnees, notificationsActives }` |
| PUT | `/utilisateurs/me/preferences` | AUTH | Remplace les préférences |
| POST | `/utilisateurs/me/acceptations-conditions` | AUTH | Enregistre l'acceptation des conditions (date serveur). `201` |
| GET | `/utilisateurs/me/appareils` | AUTH | Appareils de notification du compte |
| POST | `/utilisateurs/me/appareils` | AUTH | Enregistre un appareil : `{ jeton, plateforme }` (`ANDROID`, `IOS`, `WEB`). Le jeton est haché côté serveur. `201` |
| PUT | `/utilisateurs/me/appareils/{appareilId}` | AUTH | Met à jour le jeton ou `actif` |
| DELETE | `/utilisateurs/me/appareils/{appareilId}` | AUTH | Désinscrit l'appareil. `204` |
| GET | `/utilisateurs/me/export-donnees` | AUTH | *(optionnel V1.1)* Export des données personnelles (JSON) |
| POST | `/utilisateurs/me/fermeture` | AUTH | *(optionnel V1.1)* Demande de fermeture du compte (statut `FERME`, anonymisation à définir) |

## 3. Gestion des comptes (personnel)

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/utilisateurs` | ADMIN | Liste. Filtres : `q`, `statut`, `role` |
| POST | `/utilisateurs` | ADMIN | Crée un compte de personnel (`ADMIN`, `INSTITUTION`, `AGENT_TERRAIN`). Créer un `ADMIN` ou `SUPER_ADMIN` exige `SUPER`. Le compte est activé par OTP |
| GET | `/utilisateurs/{utilisateurId}` | ADMIN | Détail |
| POST | `/utilisateurs/{utilisateurId}/activation` | ADMIN | Statut → `ACTIF` |
| POST | `/utilisateurs/{utilisateurId}/suspension` | ADMIN | Statut → `SUSPENDU` (révoque les sessions). Un SUPER_ADMIN n'est suspendu que par un SUPER |
| POST | `/utilisateurs/{utilisateurId}/fermeture` | ADMIN | Statut → `FERME` |
| GET | `/utilisateurs/{utilisateurId}/roles` | ADMIN | Rôles du compte |
| PUT | `/utilisateurs/{utilisateurId}/roles/{role}` | ADMIN | Attribue un rôle. **`CENTRE_ADMIN` refusé** (voir `/centres/{id}/administrateurs`). `SUPER_ADMIN` : `SUPER` seul |
| DELETE | `/utilisateurs/{utilisateurId}/roles/{role}` | ADMIN | Retire un rôle. `204`. Impossible de retirer le dernier `SUPER_ADMIN` |

### Inscription assistée (agent de terrain)
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| POST | `/agents/me/inscriptions-assistees` | AGENT | Crée compte + profil pour une personne : contact, nom, prénom, `communeId`, `consentementRecu: true` (obligatoire). Le compte porte `creePar`. Un OTP est envoyé à la personne. `201` |
| GET | `/agents/me/inscriptions-assistees` | AGENT | Comptes créés par l'agent |
| GET | `/agents/me/inscriptions-assistees/{citoyenId}` | AGENT | Détail d'une inscription de l'agent |

## 4. Référentiels (lecture `PUBLIC`, écriture `ADMIN`)

### 4.1 Nomenclatures
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/nomenclatures` | PUBLIC | Toutes les énumérations (niveaux, statuts, sexe, types de preuve, méthodes…) pour éviter de les coder en dur dans les clients |
| GET | `/nomenclatures/{nom}` | PUBLIC | Une énumération (ex. `statuts-validation-competence`) |

### 4.2 Territoires et communes
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/territoires` | PUBLIC | Filtres : `niveau` (`REGION`, `DISTRICT`, `CERCLE`), `parentId`, `actif`, `q` |
| GET | `/territoires/{territoireId}` | PUBLIC | Détail avec parent |
| GET | `/territoires/code/{code}` | PUBLIC | Par code (ex. `ML-REG-01`) |
| GET | `/territoires/{territoireId}/enfants` | PUBLIC | Sous-territoires directs |
| GET | `/territoires/{territoireId}/communes` | PUBLIC | Communes du territoire. `recursive=true` inclut les descendants |
| POST | `/territoires` | ADMIN | Crée un territoire (hiérarchie validée). `201` |
| PUT | `/territoires/{territoireId}` | ADMIN | Remplace |
| PATCH | `/territoires/{territoireId}` | ADMIN | Modifie (ex. `actif`) |
| DELETE | `/territoires/{territoireId}` | ADMIN | `409` s'il est référencé |
| GET | `/communes` | PUBLIC | Filtres : `territoireId`, `q`, `actif` |
| GET | `/communes/{communeId}` | PUBLIC | Détail avec chaîne des territoires parents |
| GET | `/communes/code/{code}` | PUBLIC | Par code |
| POST | `/territoires/{territoireId}/communes` | ADMIN | Crée une commune dans un territoire. `201` |
| PUT | `/communes/{communeId}` | ADMIN | Remplace |
| PATCH | `/communes/{communeId}` | ADMIN | Modifie (ex. `actif`) |
| DELETE | `/communes/{communeId}` | ADMIN | `409` si référencée |

### 4.3 Secteurs, métiers, compétences
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/secteurs-activite` | PUBLIC | Liste (`actif`, `q`) |
| GET | `/secteurs-activite/{secteurId}` | PUBLIC | Détail |
| GET | `/secteurs-activite/{secteurId}/metiers` | PUBLIC | Métiers du secteur |
| POST | `/secteurs-activite` | ADMIN | Crée. `201` |
| PUT | `/secteurs-activite/{secteurId}` | ADMIN | Remplace |
| PATCH | `/secteurs-activite/{secteurId}` | ADMIN | Modifie (ex. `actif`) |
| DELETE | `/secteurs-activite/{secteurId}` | ADMIN | `409` s'il est utilisé |
| POST | `/secteurs-activite/{secteurId}/metiers` | ADMIN | Crée un métier dans un secteur. `201` |
| GET | `/metiers` | PUBLIC | Filtres : `secteurId`, `q`, `actif` |
| GET | `/metiers/{metierId}` | PUBLIC | Détail |
| PUT | `/metiers/{metierId}` | ADMIN | Remplace |
| PATCH | `/metiers/{metierId}` | ADMIN | Modifie |
| DELETE | `/metiers/{metierId}` | ADMIN | `409` s'il est utilisé |
| GET | `/metiers/{metierId}/competences` | PUBLIC | Compétences du métier (`principale`, `niveauAttendu`) |
| PUT | `/metiers/{metierId}/competences/{competenceId}` | ADMIN | Lie une compétence : `{ principale, niveauAttendu? }`. `200` |
| DELETE | `/metiers/{metierId}/competences/{competenceId}` | ADMIN | Retire le lien. `204` |
| GET | `/competences` | PUBLIC | Filtres : `q`, `metierId`, `secteurId`, `methodeCode`, `actif` |
| GET | `/competences/{competenceId}` | PUBLIC | Détail |
| GET | `/competences/{competenceId}/metiers` | PUBLIC | Métiers liés |
| POST | `/competences` | ADMIN | Crée. `201` |
| PUT | `/competences/{competenceId}` | ADMIN | Remplace |
| PATCH | `/competences/{competenceId}` | ADMIN | Modifie |
| DELETE | `/competences/{competenceId}` | ADMIN | `409` si utilisée |
| GET | `/competences/{competenceId}/methodes-validation` | PUBLIC | Méthodes **actives** pour cette compétence |
| PUT | `/competences/{competenceId}/methodes-validation/{methodeCode}` | ADMIN | Active ou désactive : `{ active }` |
| DELETE | `/competences/{competenceId}/methodes-validation/{methodeCode}` | ADMIN | Retire. `204` |

### 4.4 Méthodes et sources
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/methodes-validation` | PUBLIC | Les cinq méthodes (`DOCUMENT`, `TEST_NUMERIQUE`, `EVALUATION_CENTRE`, `PORTFOLIO`, `MAITRE_ARTISAN`) |
| GET | `/methodes-validation/{methodeCode}` | PUBLIC | Détail |
| GET | `/sources-referentiel` | ADMIN | Provenances et versions |
| POST | `/sources-referentiel` | ADMIN | Enregistre une source. `201` |
| GET | `/sources-referentiel/{sourceId}` | ADMIN | Détail |

## 5. Profil citoyen

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| POST | `/citoyens` | CIT | Crée **le** profil du compte connecté (`409` s'il existe). Corps : `nom`, `prenom`, `dateNaissance?`, `sexe?`, `statutActivite?`, `communeId?`. Génère l'`identifiantPublic`. `201` |
| GET | `/citoyens/me` | CIT | Profil complet |
| PUT | `/citoyens/me` | CIT | Remplace les champs éditables (nom, prénom, naissance, sexe, statut d'activité) |
| PUT | `/citoyens/me/visibilite` | CIT | `{ profilPublic }` : visible ou non des organisations |
| GET | `/citoyens/me/cv` | CIT | CV en PDF. Distingue compétences **déclarées** et **validées** |
| GET | `/citoyens/me/consultations` | CIT | Consultations de mon profil par des organisations (agrégées ; l'organisation n'est nommée que si elle est `APPROUVEE`) |
| GET | `/citoyens` | ADMIN | Liste. Filtres : `q`, `communeId`, `statutActivite` |
| GET | `/citoyens/{citoyenId}` | ADMIN, AGENT | Détail (l'agent n'accède qu'aux profils qu'il a créés) |

### 5.1 Localisation
La commune de référence ne change **jamais** automatiquement.

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/citoyens/me/localisation` | CIT | Commune de référence (`communeId`, `source`, `confirmeeLe`) |
| PUT | `/citoyens/me/localisation` | CIT | Le citoyen déclare sa commune (confirmée immédiatement, source `DECLARATION`) |
| PUT | `/citoyens/me/consentement-gps` | CIT | `{ consenti }` : autorise (ou retire) la détection ponctuelle |
| POST | `/citoyens/me/localisation/detections` | CIT | Détection ponctuelle : `{ codeCommune }` (de préférence déterminée par le mobile ; **aucune coordonnée n'est stockée**). Réponse : `{ decision, demande? }` avec `decision` ∈ `PROPOSER`, `RIEN_A_FAIRE`, `GPS_NON_CONSENTI`, `COMMUNE_INCONNUE`, `DEJA_EN_ATTENTE`, `DELAI_NON_ECOULE`. `201` si une demande est créée, sinon `200` |
| GET | `/citoyens/me/demandes-actualisation-localisation` | CIT | Historique. Filtre `statut` |
| GET | `/citoyens/me/demandes-actualisation-localisation/{demandeId}` | CIT | Détail |
| POST | `/citoyens/me/demandes-actualisation-localisation/{demandeId}/acceptation` | CIT | Met à jour `localisation_profil` (source `GPS_CONFIRMEE`) |
| POST | `/citoyens/me/demandes-actualisation-localisation/{demandeId}/refus` | CIT | Conserve la commune de référence |

## 6. Compétences du citoyen

Une `citoyenCompetence` est unique par couple (citoyen, compétence). Le citoyen ne modifie jamais `statutValidation`, `niveauValide`, `derniereMethode`, `valideeLe`, `valideJusquAu`.

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/citoyens/me/competences` | CIT | Filtres : `statutValidation`, `situation`, `disponibilite`, `secteurId`, `metierId` |
| POST | `/citoyens/me/competences` | CIT | Déclare : `{ competenceId, niveauDeclare?, situation?, disponibilite? }`. Statut initial `NON_VERIFIEE`. `409` si déjà déclarée. `201` |
| GET | `/citoyens/me/competences/{citoyenCompetenceId}` | CIT | Détail avec état de validation |
| PATCH | `/citoyens/me/competences/{citoyenCompetenceId}` | CIT | Modifie `niveauDeclare`, `situation`, `disponibilite` |
| DELETE | `/citoyens/me/competences/{citoyenCompetenceId}` | CIT | Retire si `NON_VERIFIEE` ou `REJETEE` et sans demande en cours. `409` sinon. `204` |
| GET | `/citoyens/me/competences/{citoyenCompetenceId}/decisions` | CIT | Historique **lecture seule** des décisions sur cette compétence |

### 6.1 Expériences
Une expérience appartient au citoyen et peut démontrer **plusieurs** compétences (contrôle : mêmes propriétaire).

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/citoyens/me/experiences` | CIT | Liste |
| POST | `/citoyens/me/experiences` | CIT | `{ intitule, description?, dateDebut?, dateFin?, enCours?, employeurOuClient?, competences?: [citoyenCompetenceId] }`. `201` |
| GET | `/citoyens/me/experiences/{experienceId}` | CIT | Détail avec compétences |
| PUT | `/citoyens/me/experiences/{experienceId}` | CIT | Remplace |
| DELETE | `/citoyens/me/experiences/{experienceId}` | CIT | `204` |
| GET | `/citoyens/me/experiences/{experienceId}/competences` | CIT | Compétences démontrées |
| PUT | `/citoyens/me/experiences/{experienceId}/competences/{citoyenCompetenceId}` | CIT | Lie une compétence |
| DELETE | `/citoyens/me/experiences/{experienceId}/competences/{citoyenCompetenceId}` | CIT | Retire le lien. `204` |

### 6.2 Preuves et réalisations
Les réalisations (ancien « portfolio ») sont des preuves de type `PORTFOLIO`, `PHOTO` ou `VIDEO` avec `visibleProfil`. **Une réalisation se rattache toujours à une compétence.**

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/citoyens/me/preuves` | CIT | Toutes mes preuves. Filtres : `type`, `statut`, `visibleProfil` |
| GET | `/citoyens/me/competences/{citoyenCompetenceId}/preuves` | CIT | Preuves de la compétence |
| POST | `/citoyens/me/competences/{citoyenCompetenceId}/preuves` | CIT | Dépôt (multipart) : `type`, `titre?`, `description?`, `referenceDocument?`, `visibleProfil?`. Statut initial `EN_ATTENTE`. `201` |
| GET | `/citoyens/me/competences/{citoyenCompetenceId}/preuves/{preuveId}` | CIT | Métadonnées |
| PATCH | `/citoyens/me/competences/{citoyenCompetenceId}/preuves/{preuveId}` | CIT | Modifie `titre`, `description`, `visibleProfil` |
| DELETE | `/citoyens/me/competences/{citoyenCompetenceId}/preuves/{preuveId}` | CIT | Archive. `204` |
| GET | `/preuves/{preuveId}` | EVAL, CENTRE, ADMIN | Métadonnées pour examen (accès limité aux évaluateurs d'une demande en cours sur cette compétence) |
| GET | `/preuves/{preuveId}/fichier` | CIT, EVAL, ADMIN | Téléchargement. Une organisation n'y accède que par `/talents/.../realisations` |
| POST | `/preuves/{preuveId}/verification` | EVAL, ADMIN | `{ statut (VERIFIEE|REJETEE), motif? }` |

### 6.3 Demandes de validation (côté citoyen)
Aucun document n'est exigé par défaut : seule la méthode demandée définit ce qui est nécessaire.

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/citoyens/me/demandes-validation` | CIT | Toutes mes demandes. Filtres : `statut`, `methodeCode` |
| GET | `/citoyens/me/competences/{citoyenCompetenceId}/demandes-validation` | CIT | Demandes de la compétence |
| POST | `/citoyens/me/competences/{citoyenCompetenceId}/demandes-validation` | CIT | `{ methodeCode, centreId?, evaluateurId?, messageCitoyen? }`. `422` si la méthode n'est pas active pour la compétence ou si la compétence est déjà `VALIDEE` non expirée. Compétence → `EN_COURS`. `201` |
| GET | `/citoyens/me/competences/{citoyenCompetenceId}/demandes-validation/{demandeId}` | CIT | Détail et historique des décisions |
| POST | `/citoyens/me/competences/{citoyenCompetenceId}/demandes-validation/{demandeId}/annulation` | CIT | Possible en `EN_ATTENTE` ou `COMPLEMENT_REQUIS` |
| POST | `/citoyens/me/competences/{citoyenCompetenceId}/demandes-validation/{demandeId}/complement` | CIT | Répond à un `COMPLEMENT_REQUIS` : `{ message?, preuvesAjoutees? }` → `EN_COURS` |

### 6.4 Tests numériques (côté candidat)
Les tentatives sont **à plat sous la demande** (`/demandes-validation/{demandeId}/tentatives`) pour tenir la limite de 3 niveaux ; le contrôle d'accès est fait côté serveur (le candidat n'accède qu'aux tentatives de ses propres demandes).

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/competences/{competenceId}/tests-numeriques` | AUTH | Tests actifs (titre, version, seuil, nombre de questions) |
| POST | `/demandes-validation/{demandeId}/tentatives` | CIT | Démarre une tentative (demande `TEST_NUMERIQUE`) : `{ testId }`. Renvoie les questions **sans** les réponses attendues. `201` |
| GET | `/demandes-validation/{demandeId}/tentatives` | CIT, EVAL, ADMIN | Tentatives de la demande : score, `seuilApplique`, `reussi`. Le candidat ne voit jamais les réponses attendues ; l'évaluateur voit les réponses données |
| GET | `/demandes-validation/{demandeId}/tentatives/{tentativeId}` | CIT, EVAL, ADMIN | Détail |
| POST | `/demandes-validation/{demandeId}/tentatives/{tentativeId}/soumission` | CIT | `{ reponses }`. Calcule le score. `TEXTE_LIBRE` → correction manuelle |
| GET | `/citoyens/me/tentatives-test` | CIT | Toutes mes tentatives, toutes demandes confondues |

### 6.5 Contestations
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/citoyens/me/contestations` | CIT | Filtre `statut` |
| POST | `/citoyens/me/contestations` | CIT | `{ typeObjet (DECISION_VALIDATION|DONNEE_PROFIL|AUTRE), decisionValidationId?, champConcerne?, motif }`. `201` |
| GET | `/citoyens/me/contestations/{contestationId}` | CIT | Détail et réponse |

## 7. Évaluation et validation (personnel)

Ces routes sont **à plat** : un évaluateur, un centre ou un administrateur travaille sur les demandes de plusieurs citoyens.

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/demandes-validation` | ADMIN, CENTRE, EVAL | Filtres : `statut`, `methodeCode`, `competenceId`, `centreId`, `evaluateurId`, `du`, `au`. Un `CENTRE` ne voit que son centre, un `EVAL` que ses demandes assignées |
| GET | `/demandes-validation/{demandeId}` | ADMIN, CENTRE, EVAL | Détail (citoyen, compétence, méthode, historique) |
| POST | `/demandes-validation/{demandeId}/assignation` | ADMIN, CENTRE | `{ evaluateurId }`. Vérifie l'habilitation |
| POST | `/demandes-validation/{demandeId}/prise-en-charge` | EVAL | `EN_ATTENTE` → `EN_COURS` |
| GET | `/demandes-validation/{demandeId}/preuves` | EVAL, ADMIN | Preuves de la compétence concernée |
| PUT | `/demandes-validation/{demandeId}/tentatives/{tentativeId}/corrections/{questionId}` | EVAL | Correction manuelle d'une question `TEXTE_LIBRE` : `{ points, commentaire? }` |
| GET | `/demandes-validation/{demandeId}/evaluations` | EVAL, CENTRE, ADMIN | Évaluations pratiques |
| POST | `/demandes-validation/{demandeId}/evaluations` | EVAL | `{ score?, observations?, grille?, realiseeLe?, centreId? }`. Vérifie habilitation, centre actif et agréé. `201` |
| GET | `/demandes-validation/{demandeId}/evaluations/{evaluationId}` | EVAL, CENTRE, ADMIN | Détail |
| GET | `/demandes-validation/{demandeId}/decisions` | EVAL, CENTRE, ADMIN | Historique |
| POST | `/demandes-validation/{demandeId}/decisions` | EVAL, ADMIN | `{ decision (VALIDEE|REJETEE|COMPLEMENT_REQUIS|ANNULEE), niveauValide?, motif? }`. **Une seule transaction** : décision + mise à jour de la compétence + statut de la demande + notification + audit. `niveauValide` obligatoire pour `VALIDEE`. `201` |
| GET | `/demandes-validation/{demandeId}/decisions/{decisionId}` | EVAL, CENTRE, ADMIN | Détail |

### 7.1 Évaluateur courant
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/evaluateurs/me` | EVAL | Profil évaluateur et statut |
| GET | `/evaluateurs/me/habilitations` | EVAL | Mes habilitations (compétence, méthode, centre, période, statut) |
| GET | `/evaluateurs/me/demandes-validation` | EVAL | Demandes qui me sont assignées. Filtre `statut` |
| GET | `/evaluateurs/me/centres` | EVAL | Centres où je suis habilité |

### 7.2 Choisir un centre ou un évaluateur (côté citoyen)
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/centres` | PUBLIC | Voir 8. Filtres `competenceId`, `methodeCode`, `communeId` |
| GET | `/competences/{competenceId}/evaluateurs` | AUTH | Évaluateurs habilités pour la compétence et la méthode (`methodeCode`, `communeId?`). Données minimales (nom, commune, centre) |

### 7.3 Gestion des évaluateurs et habilitations
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/evaluateurs` | ADMIN | Filtres : `statut`, `centreId`, `competenceId` |
| POST | `/evaluateurs` | ADMIN | Crée un évaluateur depuis un citoyen : `{ citoyenId }`. Un `CENTRE` passe par `POST /centres/{centreId}/habilitations`, qui crée l'évaluateur si besoin. `201` |
| GET | `/evaluateurs/{evaluateurId}` | ADMIN, CENTRE | Détail (pour `CENTRE` : seulement si habilité dans son centre) |
| POST | `/evaluateurs/{evaluateurId}/activation` | ADMIN | Statut → `ACTIF` |
| POST | `/evaluateurs/{evaluateurId}/suspension` | ADMIN | Statut → `SUSPENDU` |
| GET | `/evaluateurs/{evaluateurId}/habilitations` | ADMIN, CENTRE | Habilitations. Un `CENTRE` ne voit que celles de son centre |
| POST | `/evaluateurs/{evaluateurId}/habilitations` | ADMIN | `{ competenceId, methodeCode, centreId?, dateDebut?, dateFin? }` (toute méthode, y compris `MAITRE_ARTISAN` sans centre). `201` |
| GET | `/evaluateurs/{evaluateurId}/habilitations/{habilitationId}` | ADMIN, CENTRE | Détail |
| PATCH | `/evaluateurs/{evaluateurId}/habilitations/{habilitationId}` | ADMIN, CENTRE | Modifie les dates (le centre, la compétence et la méthode ne changent pas : créer une nouvelle habilitation) |
| POST | `/evaluateurs/{evaluateurId}/habilitations/{habilitationId}/activation` | ADMIN, CENTRE | → `ACTIVE` |
| POST | `/evaluateurs/{evaluateurId}/habilitations/{habilitationId}/suspension` | ADMIN, CENTRE | → `SUSPENDUE` |
| POST | `/evaluateurs/{evaluateurId}/habilitations/{habilitationId}/revocation` | ADMIN, CENTRE | → `REVOQUEE` (définitif) |

## 8. Centres

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/centres` | PUBLIC | Centres `ACTIF` (ADMIN : tous). Filtres : `communeId`, `territoireId`, `competenceId`, `secteurId`, `metierId`, `methodeCode`, `statut`, `q` |
| POST | `/centres` | ADMIN | Crée un centre (`EN_ATTENTE`). `201` |
| GET | `/centres/me` | CENTRE | Mon centre (alias de `/centres/{centreId}`) |
| GET | `/centres/{centreId}` | PUBLIC | Détail |
| PUT | `/centres/{centreId}` | ADMIN, CENTRE | Remplace les champs éditables |
| PATCH | `/centres/{centreId}` | ADMIN, CENTRE | Modifie |
| DELETE | `/centres/{centreId}` | ADMIN | `409` s'il est référencé |
| POST | `/centres/{centreId}/activation` | ADMIN | → `ACTIF` |
| POST | `/centres/{centreId}/suspension` | ADMIN | → `SUSPENDU` |
| GET | `/centres/{centreId}/administrateurs` | ADMIN, CENTRE | Comptes rattachés |
| PUT | `/centres/{centreId}/administrateurs/{utilisateurId}` | ADMIN | Rattache un compte (donne `CENTRE_ADMIN`). Un compte ne gère qu'**un** centre (`409` sinon) |
| DELETE | `/centres/{centreId}/administrateurs/{utilisateurId}` | ADMIN | Détache. `204` |
| GET | `/centres/{centreId}/domaines` | PUBLIC | Domaines de formation |
| POST | `/centres/{centreId}/domaines` | ADMIN, CENTRE | `{ typeDomaine (SECTEUR|METIER), secteurId?, metierId? }` (exactement un). `201` |
| DELETE | `/centres/{centreId}/domaines/{domaineId}` | ADMIN, CENTRE | `204` |
| GET | `/centres/{centreId}/competences-validables` | PUBLIC | Compétences que le centre peut évaluer (périodes) |
| POST | `/centres/{centreId}/competences-validables` | ADMIN | `{ competenceId, valideDu?, valideAu? }`. `201` |
| PATCH | `/centres/{centreId}/competences-validables/{agrementId}` | ADMIN | Période, `active` |
| DELETE | `/centres/{centreId}/competences-validables/{agrementId}` | ADMIN | `204` |
| GET | `/centres/{centreId}/evaluateurs` | ADMIN, CENTRE | Évaluateurs habilités dans ce centre |
| GET | `/centres/{centreId}/habilitations` | ADMIN, CENTRE | Habilitations du centre |
| POST | `/centres/{centreId}/habilitations` | ADMIN, CENTRE | Habilite un citoyen pour **ce centre** : `{ citoyenId, competenceId, methodeCode (EVALUATION_CENTRE), dateDebut?, dateFin? }`. Crée l'évaluateur si besoin. La compétence doit être agréée pour le centre. `201` |
| GET | `/centres/{centreId}/candidats-evaluateurs` | CENTRE, ADMIN | Recherche d'un citoyen à habiliter (`q` : téléphone ou email exact, ou nom) |
| GET | `/centres/{centreId}/demandes-validation` | CENTRE, ADMIN | Demandes `EVALUATION_CENTRE` du centre |

## 9. Tests numériques (administration)

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/tests-numeriques` | ADMIN | Filtres : `competenceId`, `actif` |
| POST | `/competences/{competenceId}/tests-numeriques` | ADMIN | Crée un test (version `1`) : `{ titre, description?, seuilReussite }`. `201` |
| GET | `/tests-numeriques/{testId}` | ADMIN | Détail |
| PATCH | `/tests-numeriques/{testId}` | ADMIN | Modifie. Un test déjà utilisé ne change que `actif` (sinon créer une version) |
| DELETE | `/tests-numeriques/{testId}` | ADMIN | Seulement s'il n'a jamais été utilisé |
| POST | `/tests-numeriques/{testId}/versions` | ADMIN | Copie en version suivante. `201` |
| GET | `/tests-numeriques/{testId}/questions` | ADMIN | Avec réponses attendues |
| POST | `/tests-numeriques/{testId}/questions` | ADMIN | `{ enonce, typeQuestion, options?, reponseAttendue, ponderation?, ordre }`. `201` |
| PUT | `/tests-numeriques/{testId}/questions/{questionId}` | ADMIN | Remplace (test non utilisé) |
| DELETE | `/tests-numeriques/{testId}/questions/{questionId}` | ADMIN | `204` (test non utilisé) |

## 10. Organisations

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| POST | `/organisations` | ORG | Crée **le** profil du compte (`409` s'il existe). RCCM et NIF **facultatifs**. Statut `EN_ATTENTE`. `201` |
| GET | `/organisations/me` | ORG | Profil et statut de vérification |
| PUT | `/organisations/me` | ORG | Remplace les champs éditables |
| GET | `/organisations/me/tableau-de-bord` | ORG | Synthèse (besoins, suivis, mises en relation) |
| POST | `/organisations/me/soumission` | ORG | Redépose un dossier : `REJETEE` → `EN_ATTENTE` |
| GET | `/organisations` | ADMIN | Filtres : `statutVerification`, `q` |
| GET | `/organisations/{organisationId}` | ADMIN | Détail |
| POST | `/organisations/{organisationId}/approbation` | ADMIN | `EN_ATTENTE` → `APPROUVEE` |
| POST | `/organisations/{organisationId}/rejet` | ADMIN | `{ motif }` : `EN_ATTENTE` → `REJETEE` |
| POST | `/organisations/{organisationId}/suspension` | ADMIN | `{ motif }` : `APPROUVEE` → `SUSPENDUE` |
| POST | `/organisations/{organisationId}/reactivation` | ADMIN | `SUSPENDUE` → `APPROUVEE` |

### 10.1 Justificatifs
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/organisations/me/justificatifs` | ORG | Liste |
| POST | `/organisations/me/justificatifs` | ORG | Dépôt (multipart) : `typeJustificatif`, `referenceDocument?`, `dateEmission?`, `dateExpiration?`. `201` |
| GET | `/organisations/me/justificatifs/{justificatifId}` | ORG | Métadonnées et statut |
| DELETE | `/organisations/me/justificatifs/{justificatifId}` | ORG | Seulement s'il n'est pas `VERIFIE`. `204` |
| GET | `/organisations/{organisationId}/justificatifs` | ADMIN | Justificatifs d'une organisation |
| GET | `/organisations/{organisationId}/justificatifs/{justificatifId}/fichier` | ORG, ADMIN | Téléchargement |
| POST | `/organisations/{organisationId}/justificatifs/{justificatifId}/verification` | ADMIN | `{ statut (VERIFIE|REJETE), motifRejet? }` |

## 11. Besoins, talents, suivi et mise en relation

### 11.1 Besoins
Un besoin est créé et listé **sous son organisation**, puis manipulé **à plat** par son identifiant. Seules les organisations `APPROUVEE` publient et recherchent.

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/organisations/me/besoins` | ORG | Filtres : `statut` |
| POST | `/organisations/me/besoins` | ORG | `{ titre, description?, dateLimite?, competences?, zones? }`. Statut `BROUILLON`. `201` |
| GET | `/besoins` | ADMIN | Tous les besoins. Filtres : `statut`, `organisationId`, `competenceId` |
| GET | `/besoins/{besoinId}` | ORG, ADMIN | Détail |
| PUT | `/besoins/{besoinId}` | ORG | Remplace (`BROUILLON` seulement) |
| PATCH | `/besoins/{besoinId}` | ORG | Modifie `titre`, `description`, `dateLimite` (aussi `OUVERT`) |
| DELETE | `/besoins/{besoinId}` | ORG | Seulement `BROUILLON`. `204` |
| POST | `/besoins/{besoinId}/publication` | ORG | `BROUILLON` → `OUVERT` (au moins une compétence) |
| POST | `/besoins/{besoinId}/cloture` | ORG | `OUVERT` → `CLOTURE` |
| POST | `/besoins/{besoinId}/annulation` | ORG | `BROUILLON` ou `OUVERT` → `ANNULE` |
| GET | `/besoins/{besoinId}/competences` | ORG, ADMIN | Compétences demandées |
| POST | `/besoins/{besoinId}/competences` | ORG | `{ competenceId, niveauRequis?, nombreRequis }`. `201` |
| PUT | `/besoins/{besoinId}/competences/{competenceId}` | ORG | `{ niveauRequis?, nombreRequis }` |
| DELETE | `/besoins/{besoinId}/competences/{competenceId}` | ORG | `204` |
| GET | `/besoins/{besoinId}/zones` | ORG, ADMIN | Zones ciblées (aucune = national) |
| POST | `/besoins/{besoinId}/zones` | ORG | `{ communeId }` **ou** `{ territoireId }`. `201` |
| DELETE | `/besoins/{besoinId}/zones/{zoneId}` | ORG | `204` |
| GET | `/besoins/{besoinId}/talents-suggeres` | ORG | Profils correspondant au besoin (compétences validées, zone, disponibilité) |

### 11.2 Recherche de talents
Résultats limités aux citoyens `profilPublic = true`, identifiés par `identifiantPublic`. Chaque consultation d'un profil est enregistrée (`consultation_profil`).

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/talents` | ORG | Recherche. Filtres : `competenceId`, `metierId`, `communeId`, `territoireId`, `niveauMin`, `validee`, `disponible`, `q` |
| GET | `/talents/{identifiantPublic}` | ORG | Profil partagé (nom révélé seulement après acceptation d'une mise en relation) |
| GET | `/talents/{identifiantPublic}/realisations` | ORG | Réalisations `visibleProfil` |
| GET | `/talents/{identifiantPublic}/realisations/{preuveId}/fichier` | ORG | Fichier d'une réalisation visible |

### 11.3 Suivi des talents
| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/besoins/{besoinId}/suivis` | ORG | Filtre `statut` |
| POST | `/besoins/{besoinId}/suivis` | ORG | `{ identifiantPublic, noteInterne? }`. `409` si déjà suivi. `201` |
| GET | `/besoins/{besoinId}/suivis/{suiviId}` | ORG | Détail |
| PATCH | `/besoins/{besoinId}/suivis/{suiviId}` | ORG | `{ noteInterne }` |
| DELETE | `/besoins/{besoinId}/suivis/{suiviId}` | ORG | Seulement sans demande de mise en relation. `204` |
| POST | `/besoins/{besoinId}/suivis/{suiviId}/archivage` | ORG | → `ARCHIVE` |
| POST | `/besoins/{besoinId}/suivis/{suiviId}/desarchivage` | ORG | `ARCHIVE` → `A_L_ETUDE` |

### 11.4 Mise en relation
La création est imbriquée (le suivi est le parent) ; la lecture et les actions sont à plat. Une seule demande `EN_ATTENTE` par suivi ; délai après un refus (`app.regles.delai-apres-refus-jours`).

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| POST | `/besoins/{besoinId}/suivis/{suiviId}/demandes-mise-en-relation` | ORG | `{ message? }`. `409` si une demande est en attente, `422` pendant le délai après refus. Suivi → `CONTACT_EN_COURS`. `201` |
| GET | `/organisations/me/demandes-mise-en-relation` | ORG | Filtres : `statut`, `besoinId` |
| GET | `/citoyens/me/demandes-mise-en-relation` | CIT | Demandes reçues. Filtre `statut` |
| GET | `/demandes-mise-en-relation/{demandeId}` | ORG, CIT, ADMIN | Détail (contacts révélés seulement si `ACCEPTEE`) |
| POST | `/demandes-mise-en-relation/{demandeId}/acceptation` | CIT | → `ACCEPTEE`, suivi → `MISE_EN_RELATION` |
| POST | `/demandes-mise-en-relation/{demandeId}/refus` | CIT | → `REFUSEE`, suivi → `A_L_ETUDE` |
| POST | `/demandes-mise-en-relation/{demandeId}/annulation` | ORG | → `ANNULEE` |

## 12. Opportunités

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/categories-opportunites` | PUBLIC | Catégories |
| GET | `/categories-opportunites/{categorieId}` | PUBLIC | Détail |
| POST | `/categories-opportunites` | ADMIN | `201` |
| PUT | `/categories-opportunites/{categorieId}` | ADMIN | Remplace |
| DELETE | `/categories-opportunites/{categorieId}` | ADMIN | `409` si utilisée |
| GET | `/categories-opportunites/{categorieId}/opportunites` | PUBLIC | Opportunités publiées de la catégorie |
| GET | `/opportunites` | PUBLIC | Publiées et non expirées. Filtres : `categorieId`, `territoireId`, `q`. Le personnel peut ajouter `statut` pour voir brouillons et archives |
| GET | `/opportunites/{opportuniteId}` | PUBLIC | Détail (non publiée : personnel autorisé seulement) |
| POST | `/opportunites` | ADMIN, CENTRE, INST | Crée un brouillon. `201` |
| PUT | `/opportunites/{opportuniteId}` | ADMIN, CENTRE, INST | Remplace (auteur ou ADMIN) |
| DELETE | `/opportunites/{opportuniteId}` | ADMIN, CENTRE, INST | Seulement `BROUILLON`. `204` |
| POST | `/opportunites/{opportuniteId}/publication` | ADMIN, CENTRE, INST | `BROUILLON` → `PUBLIEE` |
| POST | `/opportunites/{opportuniteId}/archivage` | ADMIN, CENTRE, INST | → `ARCHIVEE` |

## 13. Statistiques et cartographie

Aucune donnée individuelle n'est exposée. Chaque réponse inclut un bloc `meta` : `{ calculeLe, criteres, limites, seuilMasquage }`. Les cellules sous le seuil (et la cellule complémentaire) sont renvoyées avec `masque: true` et sans effectif. Offre, demande et écart sont calculés à la demande. Ces routes sont désactivées (`404`) tant que `INSTITUTION_API_ENABLED` et `INSTITUTION_CONFIDENTIALITY_REVIEWED` ne sont pas activés.

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/statistiques/synthese` | PUBLIC | Chiffres clés agrégés |
| GET | `/statistiques/couverture` | PUBLIC | Taux de couverture du recensement et limites d'interprétation |
| GET | `/statistiques/competences` | PUBLIC | Offre, demande, écart par compétence. Filtres : `territoireId`, `communeId`, `secteurId`, `metierId`, `competenceId`, `niveau`, `methodeCode` |
| GET | `/statistiques/cartographie` | PUBLIC | Répartition géographique. Paramètres : `niveau` (`REGION`, `CERCLE`, `COMMUNE`), `competenceId`/`metierId`/`secteurId`, `validee` |
| GET | `/territoires/{territoireId}/statistiques/competences` | PUBLIC | Équivalent de `/statistiques/competences?territoireId=` |
| GET | `/communes/{communeId}/statistiques/competences` | PUBLIC | Équivalent avec `communeId` |
| GET | `/statistiques/competences/export` | INST, ADMIN | Export CSV (`format=csv`) |
| GET | `/statistiques/tableau-de-bord` | INST, ADMIN | Synthèse institutionnelle (filtres supplémentaires : `statutActivite`, tranche d'âge) |

*Réservé, non livré en V1* : `GET /statistiques/evolution` (nécessite des snapshots) → `501`.

## 14. Notifications

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/notifications` | AUTH | Mes notifications. Filtres : `lue`, `typeEvenement` |
| GET | `/notifications/compteur-non-lues` | AUTH | `{ nonLues }` |
| GET | `/notifications/{notificationId}` | AUTH | Détail (propriétaire seulement) |
| POST | `/notifications/{notificationId}/lecture` | AUTH | Marque comme lue |
| POST | `/notifications/lectures` | AUTH | Marque plusieurs ou toutes comme lues : `{ ids? }` (vide = toutes) |
| POST | `/notifications/diffusions` | ADMIN | Diffusion ciblée : `{ titre, contenu, cible: { roles?, territoireId?, utilisateurIds? } }`. `202` |

Les notifications sont **créées par le serveur** (aucun `POST /notifications` public). Le push mobile n'est qu'un canal de livraison.

## 15. Contestations (traitement) et administration

| Méthode | Chemin | Accès | Description |
|---|---|---|---|
| GET | `/contestations` | ADMIN | Filtres : `statut`, `typeObjet` |
| GET | `/contestations/{contestationId}` | ADMIN | Détail |
| POST | `/contestations/{contestationId}/prise-en-charge` | ADMIN | `OUVERTE` → `EN_EXAMEN` |
| POST | `/contestations/{contestationId}/resolution` | ADMIN | `{ statut (ACCEPTEE|REJETEE), reponse }`. Une acceptation n'**édite pas** l'ancienne décision : elle appelle une nouvelle `POST .../decisions` |
| GET | `/journal-audit` | ADMIN | Filtres : `acteurId`, `action`, `typeRessource`, `ressourceId`, `du`, `au` |
| GET | `/journal-audit/{auditId}` | ADMIN | Détail |
| GET | `/administration/tableau-de-bord` | ADMIN | Demandes en attente, organisations à vérifier, contestations ouvertes, comptes à activer |

---

## Annexe A — Correspondance avec l'ancienne API

| Ancien | Nouveau | Remarque |
|---|---|---|
| `/api/utilisateurs/*`, `/api/opportunites`, `/api/categories-opportunites` | `/api/v1/...` | Préfixe unifié |
| `POST /auth/register` avec `role` libre | `POST /auth/register` | Rôle restreint à `CITOYEN`/`ORGANISATION`, OTP, conditions |
| `PUT /utilisateurs/{id}/contact` | `POST /utilisateurs/me/contacts/changements` + `.../verification` | Vérification par OTP |
| `GET/PUT /utilisateurs/me/preferences` | inchangé | |
| `PUT /admin/users/{id}/role`, `/statut` | `PUT /utilisateurs/{id}/roles/{role}`, `POST /utilisateurs/{id}/(activation\|suspension\|fermeture)` | Rôles multiples |
| `POST /citoyens/utilisateur/{utilisateurId}` | `POST /citoyens` | Le compte vient du jeton |
| `/citoyens/{citoyenId}/competences`, `/experiences` | inchangés, plus l'alias `me` | `citoyenId` ≠ id du compte |
| `/citoyen-competences/{id}/preuves`, `/citoyens/me/preuves`, `/preuves/{id}` | `/citoyens/me/competences/{id}/preuves`, `/preuves/{id}` | Imbriqué |
| `/citoyens/{id}/portfolio`, `/portfolio-realisations/*/medias`, `/portfolio-medias/{fichier}` | `preuves` de type `PORTFOLIO`/`PHOTO`/`VIDEO` | **Supprimé** ; plus d'URL de fichier publique |
| `POST /validations`, `PATCH /validations/{id}/statut`, `/admin/validations` | `POST .../demandes-validation`, `POST /demandes-validation/{id}/decisions` | Décision immuable |
| `/evaluateur/demandes`, `PATCH /evaluateur/demandes/{id}/decision` | `/evaluateurs/me/demandes-validation`, `POST /demandes-validation/{id}/decisions` | |
| `/evaluateur/mes-centres`, `/evaluateur/centres-disponibles` | `/evaluateurs/me/centres`, `GET /centres?competenceId=&methodeCode=` | |
| `/centres/{id}/evaluateurs`, `/centre-admin/evaluateurs/{citoyenId}/activation` | `/centres/{id}/habilitations`, `POST .../habilitations/{id}/(activation\|suspension\|revocation)` | Habilitation ciblée |
| `/centre-admin/mon-centre`, `/centre-admin/citoyens` | `/centres/me`, `/centres/{id}/candidats-evaluateurs` | |
| `PATCH /admin/centres/{id}/administrateur/{utilisateurId}` | `PUT /centres/{id}/administrateurs/{utilisateurId}` | |
| `/tests-numeriques/competence/{id}`, `/{id}/resultats`, `/resultats/citoyen/{id}`, `/questions/{id}/propositions` | `/competences/{id}/tests-numeriques`, `.../tentatives`, `.../soumission` | Propositions dans la question |
| `/organisations/me`, `/organisations/me/tableau-de-bord`, `PATCH /admin/organisations/{id}/statut` | inchangés, plus `.../(approbation\|rejet\|suspension\|reactivation)` | |
| `/besoins`, `PATCH /besoins/{id}/cloture` | `POST /besoins/{id}/cloture`, création sous `/organisations/me/besoins` | |
| `/talents/recherche`, `/talents/besoins/{id}`, `/talents/{id}` | `/talents`, `/besoins/{id}/talents-suggeres`, `/talents/{identifiantPublic}` | `identifiantPublic` |
| `/suivis-besoin-talent`, `/demandes-mise-en-relation` (CRUD) | `/besoins/{id}/suivis`, `.../demandes-mise-en-relation`, actions `acceptation`/`refus`/`annulation` | |
| `/notifications/utilisateur/{id}`, `PATCH /{id}/marquer-lue` | `/notifications`, `POST /{id}/lecture` | `me` implicite |
| `/institution/*`, `/territoires/indicateurs`, `/indicateurs-skills-gap` (CRUD) | `/statistiques/*` | Calcul à la demande ; saisie manuelle supprimée |
| `/admin/audit-logs`, `/admin/dashboard` | `/journal-audit`, `/administration/tableau-de-bord` | |
| `/admin/secteurs\|metiers\|competences` | `/secteurs-activite`, `/metiers`, `/competences` | Un seul jeu de routes |

## Annexe B — Points à valider avec ton collaborateur

1. **`PATCH` vs actions** : règle proposée en 0.4 (transitions = `POST /{id}/{action}`). Elle change `PATCH /besoins/{id}/cloture`, `PATCH /validations/{id}/statut`, etc.
2. **Alias `me`** et **`citoyenId` ≠ id du compte** : les clients doivent lire `citoyenId` dans `GET /auth/me`.
3. **Besoins à plat** (`/besoins/{id}`) avec création sous l'organisation : choix pour tenir la limite de 3 niveaux.
4. **Format d'erreur** : ajout de `code` et `details` (additif).
5. **Pagination** : enveloppe `content/page/size/totalElements/totalPages`.
6. **Transitions à définir** (non encore dans le code) : centre, évaluateur, habilitation, preuve, justificatif, opportunité. Les actions listées (`activation`, `suspension`, `revocation`, `verification`, `publication`, `archivage`) en supposent l'existence.
7. **Identité révélée** : à partir de quand le nom et les contacts d'un talent sont visibles pour une organisation (proposé : après `acceptation`).
8. **Statistiques publiques** : accessibles sans compte (proposé), derrière l'interrupteur de confidentialité existant.
9. **Optionnels** (`export-donnees`, `fermeture`) : à garder ou reporter.
10. **Dépôt de fichiers** : une requête `multipart` (proposé) plutôt que deux étapes.


## Portage P5 — routes ajoutées

- `PATCH /api/v1/organisations/{id}/statut` : changement de statut avec motif selon le cas.
- `GET/POST /api/v1/organisations/me/justificatifs` : consulter/déposer ses justificatifs.
- `GET /api/v1/organisations/admin/{organisationId}/justificatifs` et `PATCH /api/v1/organisations/admin/justificatifs/{id}` : contrôle administratif.
- `GET/PUT /api/v1/besoins/{besoinId}/zones` : consulter/remplacer les zones d’un besoin brouillon. Une liste vide indique une portée nationale.
- `PATCH /api/v1/demandes-mise-en-relation/{id}/reponse` : réponse du citoyen (`ACCEPTEE` ou `REFUSEE`).
- `PATCH /api/v1/demandes-mise-en-relation/{id}/message` : modification du message par l’organisation propriétaire.

Les consultations de profils restent des traces internes et ne disposent pas de route de lecture par le talent consulté.
