> **OBSOLÈTE (V1.1)** : ce document décrit l'ancien modèle. Référence actuelle : `docs/dictionnaire-donnees.md` et `docs/PLAN_PORTAGE_V1_1.md`.

# Jeu de données de test Swagger

Base vérifiée le 6 octobre 2026 sur `http://localhost:8080`.

## 1. Ouvrir Swagger et s’authentifier

Ouvrir [Swagger UI](http://localhost:8080/swagger-ui/index.html).

La plupart des routes exigent un JWT. La documentation expose maintenant le bouton **Authorize** :

1. Exécuter `POST /api/v1/auth/register` avec un téléphone et un email jamais utilisés :

```json
{
  "telephone": "20261006001",
  "email": "swagger.citoyen.20261006001@example.test",
  "password": "TestSwagger2026!",
  "role": "CITOYEN"
}
```

Cette inscription crée uniquement un compte dans la table `utilisateur`. Elle ne crée pas encore de profil dans `citoyen`. La route d’inscription est publique : ne lui envoyez pas d’en-tête `Authorization`.

2. Exécuter `POST /api/v1/auth/login` :

```json
{
  "identifiant": "swagger.citoyen.20261006001@example.test",
  "password": "TestSwagger2026!"
}
```

3. Copier la valeur `token` de la réponse, cliquer **Authorize**, coller le jeton et valider. Les routes d’authentification restent publiques. Les routes métier demandent le jeton.

> Si vous relancez l’inscription, changez le téléphone et l’email : ils doivent être uniques.

## 2. Créer les données dans cet ordre

Les UUID sont générés par le serveur. Après chaque POST, copiez le champ `id` renvoyé et remplacez les marqueurs `{{..._ID}}` ci-dessous par ces UUID. Ne saisissez pas littéralement les marqueurs.

### Région puis commune

`POST /api/regions`

```json
{
  "nom": "Region Swagger 20261006"
}
```

`POST /api/communes`

```json
{
  "regionId": "{{REGION_ID}}",
  "nom": "Commune Swagger 20261006"
}
```

### Citoyens et organisation

Le compte créé par `auth/register` sert à ouvrir Swagger. Son UUID est visible dans `GET /api/utilisateurs` en recherchant le téléphone choisi. Pour disposer d’un second citoyen et d’un compte organisation, créer deux utilisateurs :

`POST /api/utilisateurs` (répéter avec un téléphone/email différents pour chaque utilisateur)

```json
{
  "telephone": "20261006002",
  "email": "swagger.organisation.20261006002@example.test",
  "password": "TestSwagger2026!",
  "role": "ORGANISATION",
  "actif": true
}
```

Pour obtenir l’UUID du compte inscrit sans passer par Swagger, une requête SQL de lecture suffit : `SELECT id, telephone, email, role FROM utilisateur WHERE email = 'swagger.citoyen.20261006001@example.test';`. Cet UUID sert dans l’URL `POST /api/v1/citoyens/utilisateur/{utilisateurId}`. La table `citoyen` ne contient pas de colonne `email`; son `id` est aussi celui du compte associé.

`POST /api/v1/citoyens/utilisateur/{{UTILISATEUR_CITOYEN_ID}}`

```json
{
  "nom": "Traore",
  "prenom": "Aminata",
  "sexe": "FEMME",
  "photoUrl": "",
  "disponibilite": "DISPONIBLE",
  "communeId": "{{COMMUNE_ID}}"
}
```

Créer un deuxième profil citoyen de la même manière avec l’UUID d’un autre utilisateur. `POST /api/organisations` :

```json
{
  "utilisateurId": "{{UTILISATEUR_ORGANISATION_ID}}",
  "nom": "Organisation Swagger 20261006",
  "description": "Organisation fictive de test",
  "statut": "APPROUVEE"
}
```

`POST /api/centres` (ressource indépendante) :

```json
{
  "nom": "Centre Swagger 20261006",
  "description": "Centre fictif de test",
  "adresse": "Bamako",
  "telephone": "20261006003"
}
```

### Catégorie et opportunité

`POST /api/categories-opportunites`

```json
{
  "nom": "Formation Swagger 20261006",
  "description": "Catégorie fictive pour les essais"
}
```

`POST /api/opportunites`

```json
{
  "categorieId": "{{CATEGORIE_ID}}",
  "titre": "Formation de test Swagger",
  "description": "Opportunité fictive pour tester les opérations REST.",
  "statut": "PUBLIEE",
  "type": "FORMATION_GRATUITE",
  "dateExpiration": "2027-12-31"
}
```

### Besoin, suivi talent et mise en relation

`POST /api/besoins` : le tableau `competences` peut être vide pour tester la relation avec l’organisation sans référentiel de compétences.

```json
{
  "organisationId": "{{ORGANISATION_ID}}",
  "titre": "Besoin Swagger 20261006",
  "description": "Besoin fictif lié à l’organisation de test.",
  "statut": "OUVERT",
  "dateDebut": "2026-10-06",
  "dateFin": "2027-01-31",
  "competences": []
}
```

Quand le référentiel de compétences sera alimenté, remplacer le tableau vide par exemple par :

```json
"competences": [
  {
    "competenceId": "{{COMPETENCE_ID}}",
    "niveauRequis": "INTERMEDIAIRE",
    "nombreRequis": 2
  }
]
```

Les identifiants `citoyenId` renvoyés par les endpoints talent sont des identifiants publics opaques, pas les identifiants internes du compte.

`POST /api/suivi-talents` :

```json
{
  "organisationId": "{{ORGANISATION_ID}}",
  "besoinId": "{{BESOIN_ID}}",
  "citoyenId": "{{IDENTIFIANT_PUBLIC_TALENT}}",
  "statut": "A_L_ETUDE",
  "scoreCorrespondance": null,
  "detailsCorrespondance": "Correspondance fictive de test",
  "noteInterne": "Test Swagger"
}
```

`POST /api/demandes-mise-en-relation` (le serveur déduit le demandeur du jeton ; `destinataireId` est l’identifiant public du talent) :

```json
{
  "destinataireId": "{{IDENTIFIANT_PUBLIC_TALENT}}",
  "suiviBesoinTalentId": "{{SUIVI_TALENT_ID}}",
  "message": "Bonjour, je souhaite échanger au sujet de ce besoin."
}
```

### Notification

`POST /api/notifications` :

```json
{
  "titre": "Notification Swagger",
  "message": "Message fictif pour tester la lecture et le marquage comme lu.",
  "type": "SYSTEME",
  "destinatairesIds": ["{{UTILISATEUR_ID}}"]
}
```

Ensuite appeler `GET /api/notifications/utilisateur/{{UTILISATEUR_ID}}` et `GET /api/notifications/utilisateur/{{UTILISATEUR_ID}}/non-lues`. Pour marquer la notification comme lue, envoyer `PATCH /api/notifications/{{DESTINATAIRE_ID_DE_LA_REPONSE}}/marquer-lue` : la réponse de liste contient le champ `destinataireId` à utiliser.

### Portfolio, média et expérience

`POST /api/v1/citoyens/{{CITOYEN_ID}}/portfolio`

```json
{
  "citoyenId": "{{CITOYEN_ID}}",
  "titre": "Réalisation Swagger",
  "description": "Réalisation fictive pour vérifier le portfolio.",
  "dateRealisation": "2026-10-06",
  "localisation": "Bamako",
  "medias": [],
  "lienUrl": "https://example.test/realisation"
}
```

`POST /api/v1/portfolio-realisations/{{REALISATION_ID}}/medias` pour une référence URL :

```json
{
  "type": "IMAGE",
  "urlMedia": "https://example.test/image.jpg",
  "legende": "Image fictive de test"
}
```

Pour tester l’envoi réel d’un fichier, utiliser la variante `multipart/form-data` de la même route dans Swagger et choisir un petit fichier de test.

`POST /api/v1/citoyens/{{CITOYEN_ID}}/experiences`

```json
{
  "citoyenId": "{{CITOYEN_ID}}",
  "titre": "Expérience Swagger",
  "description": "Expérience fictive.",
  "entreprise": "Atelier de test",
  "dateDebut": "2025-01-01",
  "dateFin": "2025-12-31",
  "enCours": false,
  "citoyenCompetenceIds": []
}
```

## 3. Tester GET, PUT et DELETE

Pour les ressources qui possèdent ces opérations dans Swagger :

1. faire le POST et conserver l’`id` de la réponse;
2. faire le GET par ID et le GET de liste lorsqu’il existe;
3. envoyer le même objet au PUT en modifiant un texte (par exemple ajouter `- MODIF` au titre/nom);
4. refaire un GET par ID pour constater la modification;
5. faire le DELETE sur l’ID créé puis vérifier dans le GET de liste que l’élément a disparu. Un GET par ID sur une ressource supprimée peut actuellement renvoyer 500 sur certains services, car ils lèvent une `RuntimeException` au lieu de l’exception 404 dédiée.

Routes CRUD complètes dans le Swagger actuel : utilisateurs, régions, communes, centres, organisations, opportunités, suivi talents, demandes de mise en relation, indicateurs skills-gap, réalisations portfolio, médias, expériences et preuves. Supprimer les enfants avant leurs parents (ex. média puis réalisation, commune puis région) pour respecter les clés étrangères.

## 4. Limites découvertes dans l’API actuelle

- Le `GET /api/v1/competences` fonctionne mais renvoie actuellement `[]`. Il n’existe pas de POST Swagger pour créer une compétence. Les relations compétence citoyen, compétences d’un besoin, indicateurs Skills Gap, preuves/validations liées à une compétence et tests numériques ne peuvent donc pas être reliées à une compétence existante pour le moment.

Voici la couverture actuelle par table SQL (POST/GET/PUT/DELETE; PATCH est indiqué explicitement) :

| Table | Opérations exposées |
|---|---|
| `region`, `commune`, `centre`, `utilisateur`, `organisation`, `opportunite` | CRUD |
| `portfolio_realisation`, `media_realisation`, `experience`, `preuve`, `suivi_besoin_talent`, `demande_mise_en_relation`, `indicateur_skills_gap` | CRUD |
| `citoyen` | Création via utilisateur, GET par ID, PUT; pas de liste ni DELETE |
| `categorie_opportunite` | POST, GET liste/par ID, DELETE; pas de PUT |
| `besoin` | POST, GET liste/par ID, DELETE; pas de PUT |
| `besoin_competence` | Créée en enfant d’un besoin; pas de route propre |
| `citoyen_competence` | POST et GET dans le profil citoyen; pas de PUT/DELETE |
| `competence` | GET liste/par ID seulement; liste actuellement vide |
| `metier`, `secteur_activite`, `consultation_profil` | Aucune route de contrôleur |
| `notification` | POST et GET par utilisateur; pas de PUT/DELETE |
| `notification_destinataire` | Créée par POST notification, consultée via l’utilisateur et modifiée par PATCH « marquer lue »; pas de CRUD propre |
| `validation` | POST, GET liste/par ID, PATCH du statut; pas de PUT/DELETE |
| `test_numerique`, `question`, `proposition_reponse` | GET seulement; pas de POST/PUT/DELETE |
| `resultat_test` | POST d’un résultat et GET des résultats d’un citoyen; pas de PUT/DELETE |

Les tests numériques n’exposent que GET des tests/questions/propositions et POST des résultats. Les tests, questions et propositions doivent être préchargés en base, et leurs tests ont eux-mêmes besoin d’une compétence.

Voici les corps de requête pour les relations qui deviendront testables après l’ajout de données de référence :

`POST /api/v1/citoyens/{{CITOYEN_ID}}/competences`

```json
{
  "competenceId": "{{COMPETENCE_ID}}",
  "niveau": "INTERMEDIAIRE"
}
```

`POST /api/v1/citoyen-competences/{{CITOYEN_COMPETENCE_ID}}/preuves`

```json
{
  "citoyenCompetenceId": "{{CITOYEN_COMPETENCE_ID}}",
  "type": "CERTIFICAT",
  "titre": "Certificat de test",
  "description": "Pièce fictive de test Swagger",
  "fichierUrl": "https://example.test/certificat.pdf"
}
```

`POST /api/v1/citoyen-competences/{{CITOYEN_COMPETENCE_ID}}/validations`

```json
{
  "citoyenCompetenceId": "{{CITOYEN_COMPETENCE_ID}}",
  "preuveId": "{{PREUVE_ID}}",
  "valideeParId": "{{CITOYEN_DESIGNE_EVALUATEUR_ID}}",
  "centreId": "{{CENTRE_ID}}",
  "type": "PARTENAIRE_ATTESTE",
  "statut": "EN_ATTENTE",
  "commentaire": "Validation fictive de test",
  "dateExpiration": "2027-12-31"
}
```

`POST /api/indicateurs/skills-gap`

```json
{
  "regionId": "{{REGION_ID}}",
  "competenceId": "{{COMPETENCE_ID}}",
  "periodeDebut": "2026-01-01",
  "periodeFin": "2026-12-31",
  "demandeEstimee": 25,
  "offreDisponible": 18
}
```

`POST /api/v1/tests-numeriques/{{TEST_ID}}/resultats` (la clé de `reponses` est l’ID d’une question; sa valeur est la liste d’IDs de propositions sélectionnées) :

```json
{
  "citoyenId": "{{CITOYEN_ID}}",
  "reponses": {
    "{{QUESTION_ID}}": ["{{PROPOSITION_ID}}"]
  }
}
```

La route d’ajout de média accepte soit une URL, soit un fichier multipart; le fichier doit être choisi dans le champ de fichier de Swagger pour tester le stockage réel.

Ces routes manquantes ne peuvent pas être remplacées par des payloads JSON : il faut ajouter les opérations API absentes ou initialiser les données de référence en base pour tester ces relations. Le code Swagger a été compilé; après redémarrage du backend, le bouton **Authorize** permet d’envoyer le JWT sur les routes protégées.


Pour un compte organisation, `numeroRccm` est un champ facultatif (80 caractères maximum) transmis à l’inscription et dans la mise à jour de `/api/v1/organisations/me`. Il est renvoyé dans le profil et les réponses d’administration des organisations.
