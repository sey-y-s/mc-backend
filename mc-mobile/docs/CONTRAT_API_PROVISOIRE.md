> **Mise à jour (octobre 2026)** — État réel du backend, vérifié dans le code :
> - Disponibles : `POST /api/v1/auth/{register,login,logout,forgot-password,reset-password}`, `PUT /api/v1/auth/mot-de-passe`,
>   `GET /api/talents/recherche`, `GET /api/talents/{id}`, `/api/utilisateurs/{id}/contact`, `/api/utilisateurs/me/preferences`.
> - Publics (sans jeton) : `GET /api/v1/regions`, `GET /api/v1/communes`, `GET /api/opportunites`, `GET /api/categories-opportunites`.
> - Validations du citoyen connecté : `GET /api/citoyens/me/validations` ; preuves : `/api/preuves/{id}`, `/api/citoyens/me/preuves` (sans `/v1`).
> - Absent : renouvellement de jeton (`/refresh`). Un 401 ferme la session côté mobile.
> - Mot de passe oublié : le code (6 chiffres, valable 15 min, 5 essais) est écrit dans la console du backend tant qu'aucune passerelle SMS/email n'est branchée (`CodeSender`).

# Contrat API mobile — écarts à confirmer

Ce document distingue les routes réellement présentes dans le backend au moment de l’intégration des routes provisoires utilisées par l’application. Les adresses restent centralisées dans `lib/core/network/api_endpoints.dart`. Les réponses d’erreur passent par `AppFailure`.

## Notifications

Routes backend confirmées :

- `GET /api/notifications/utilisateur/{utilisateurId}`
- `GET /api/notifications/utilisateur/{utilisateurId}/non-lues`
- `PATCH /api/notifications/{notificationDestinataireId}/marquer-lue`

Il n’existe pas de route groupée « tout marquer comme lu » ni de pagination serveur. Le mobile pagine les réponses localement et marque les notifications non lues une à une. Dans la réponse, `id` est l’identifiant de la notification et `destinataireId` celui du lien à passer à la route de lecture.

## Opportunités

Routes backend confirmées : `GET /api/opportunites` et `GET /api/opportunites/{id}`. La liste n’est pas paginée côté serveur ; le mobile filtre et pagine localement. La configuration de sécurité du backend autorise actuellement un chemin `/api/v1/opportunites`, différent de la route du contrôleur `/api/opportunites`. Vérifier l’accès authentifié avant d’activer le mode API en production.

## Recherche de talents

Le backend actuel ne fournit ni recherche anonymisée ni route métier. Les chemins `GET /api/v1/citoyens/recherche`, `GET /api/v1/citoyens/recherche/{id}` et `GET /api/v1/metiers` sont provisoires et nécessitent un endpoint backend qui renvoie uniquement des profils anonymisés et des filtres paginés.

Le mobile ne conserve dans ses modèles que l’identifiant opaque, les compétences, la région, la disponibilité, des compteurs et des titres de portfolio. Les champs nom, téléphone, email et photo sont ignorés. Cette protection d’affichage côté client ne remplace pas un filtrage de confidentialité côté serveur.

## Mises en relation

Routes backend confirmées sous `/api/demandes-mise-en-relation` : GET liste, GET détail, POST création, PUT mise à jour et DELETE. La liste n’est pas paginée et le DTO contient toujours les noms complets des deux parties. Le mobile filtre la liste par utilisateur connecté et ne garde/affiche les noms qu’après acceptation. Les coordonnées téléphoniques et email ne figurent pas dans le DTO actuel et ne seront donc pas affichées.

Le backend ne vérifie pas actuellement que la personne connectée participe à la demande avant lecture/modification et n’applique pas de contrôle objet/role à ces routes. Il faut ajouter cette autorisation côté serveur avant toute mise en production. Le nom de propriété `SuiviBesoinTalentId` du DTO Java est atypique et doit être stabilisé (idéalement `suiviBesoinTalentId`).

## Tests numériques

Routes confirmées :

- `GET /api/v1/tests-numeriques/competence/{competenceId}`
- `GET /api/v1/tests-numeriques/{id}`
- `GET /api/v1/tests-numeriques/{id}/questions`
- `GET /api/v1/tests-numeriques/questions/{questionId}/propositions`
- `POST /api/v1/tests-numeriques/{id}/resultats`
- `GET /api/v1/tests-numeriques/resultats/citoyen/{citoyenId}`

Administration (rôle `ADMIN` ou `SUPER_ADMIN`) :

- `GET /api/admin/tests-numeriques`
- `POST /api/admin/tests-numeriques`
- `PUT /api/admin/tests-numeriques/{id}`
- `DELETE /api/admin/tests-numeriques/{id}` (archivage logique)
- `GET /api/admin/competences`

L’éditeur mobile admin n’envoie que des questions `CHOIX_MULTIPLE`, avec au
moins deux propositions et au moins une bonne réponse. Le backend contrôle
également ces règles et renvoie l’identifiant de compétence pour l’édition.

Il n’existe pas de liste globale : le mobile collecte les tests des compétences du citoyen, puis pagine localement. La soumission attend `citoyenId` et une map `reponses` (question UUID vers liste de propositions UUID). Le mobile ignore systématiquement le champ `correcte` si l’API le renvoie. Le backend ne calcule pas actuellement les réponses libres `TEXTE_LIBRE`; elles ne sont pas incluses dans le score envoyé. La réponse de résultat inclut `preuveId`, utilisé uniquement comme lien vers `/preuves/{id}`.

## Génération du CV

`POST /api/v1/citoyens/me/photo` accepte la photo du citoyen connecté en multipart (`fichier`) et la stocke parmi les médias du backend. `GET /api/v1/citoyens/me/cv` renvoie ensuite un PDF en pièce jointe. Le serveur déduit l’identifiant du citoyen du jeton et rassemble ses coordonnées, sa photo, toutes ses expériences, ses compétences à l’état `VALIDEE` et ses résultats de tests numériques. Le mobile lance le téléchargement depuis le passeport et propose l’enregistrement du PDF sur l’appareil. Les photos JPEG et PNG sont intégrées au PDF ; sinon le CV affiche les initiales du citoyen.

## Portfolio

La liste et les opérations CRUD existent sous `/api/v1/citoyens/{citoyenId}/portfolio`. Les médias sont gérés sous `/api/v1/portfolio-realisations/{realisationId}/medias`, mais le contrôleur actuel attend un objet JSON `type`, `urlMedia`, `legende` et n’expose pas d’envoi multipart. Le DTO de réalisation renvoyé par le backend n’inclut pas non plus `lienUrl`, même si la requête l’accepte.

La route d’envoi multipart utilisée par le mobile est provisoire. Pour l’activer, ajouter un endpoint multipart qui reçoit le fichier et retourne un objet média avec une URL exploitable. Le champ `FilePickerField` sélectionne/compresse/valide le fichier et affiche l’aperçu ; le repository effectue l’envoi avec progression.

## Paramètres et authentification

Le backend fournit `GET /api/utilisateurs/{id}` et `PUT /api/utilisateurs/{id}`. Le PUT actuel attend l’objet utilisateur complet, y compris mot de passe et rôle : le mobile ne l’utilise pas pour modifier des coordonnées. Les routes `PUT /api/utilisateurs/{id}/contact` et `PUT /api/v1/auth/mot-de-passe` sont provisoires et doivent être implémentées avec les validations nécessaires. Les préférences mode économie de données et notifications sont locales dans SharedPreferences.

Le login mobile est aligné sur `POST /api/v1/auth/login` avec `{identifiant, password}` et accepte le champ de réponse `token`. L’inscription mobile attend encore un contrat à harmoniser : le client envoie actuellement les champs du formulaire directement, tandis que la création backend du compte/citoyen suit un parcours différent et peut ne pas retourner de jeton.

## Activation mode API

Par défaut, l’application utilise les repositories API (`USE_MOCKS=false`). Pour tester l’interface hors ligne avec des données fictives, lancer `flutter run --dart-define=USE_MOCKS=true`.

Dans le navigateur et sur les appareils de bureau, l’URL par défaut est `http://localhost:8080`. Sur l’émulateur Android, l’URL par défaut est `http://10.0.2.2:8080`. Sur un téléphone physique, passer l’adresse de la machine qui héberge le backend avec `--dart-define=API_BASE_URL=http://<adresse-du-backend>:8080`.

Les routes signalées comme provisoires ci-dessus doivent être ajoutées au backend avant que leurs actions puissent fonctionner avec des données serveur. Les listes et détails affichent leur état d’erreur avec une action Réessayer si une route est indisponible. Les formulaires affichent l’échec de l’action lorsque le serveur ne fournit pas encore la route. En production, `APP_ENV=prod` force toujours le mode API.

## Espace d’administration mobile

Le panneau `/admin` est affiché uniquement pour les rôles `ADMIN` et
`SUPER_ADMIN` du jeton JWT ; la sécurité effective reste appliquée par le
backend. La gestion des comptes et rôles (`/api/admin/users`) est réservée au
`SUPER_ADMIN`. Les opportunités utilisent les routes CRUD `/api/opportunites`
et `/api/categories-opportunites`, les validations et organisations proposent
uniquement les changements de statut exposés par leurs routes admin.

Les validations ne sont pas reliées aux catégories d’opportunités (formation,
concours, bourse, etc.) dans le modèle ni dans l’API backend. Le menu des
opportunités ne peut donc pas filtrer les validations sans ajouter d’abord cette
relation métier côté serveur.
