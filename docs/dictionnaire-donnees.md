# MaliCompétences V1.1 — Dictionnaire de données

Ce dictionnaire décrit le schéma réellement appliqué par `src/main/resources/db/migration/V1__schema_initial.sql` du backend. Principe directeur : **une V1 simple et utile**, avec une base cohérente et un backend fiable plutôt qu'une base qui contrôle tout. Ce qui n'est pas indispensable au lancement est listé en section 11.

## Convention générale
- Clés primaires : `CHAR(36)` contenant des UUID. Dates techniques : `DATETIME(6)`.
- Fichiers : uniquement une clé de stockage privé dans la base, jamais une URL publique permanente.
- Téléphones : format international normalisé (ex. `+22370000000`).
- Les statuts sont des chaînes contrôlées par des `CHECK` SQL **et** par l'application.
- Base créée séparément des anciennes bases. Le fichier SQL est directement utilisable comme migration Flyway `V1__schema_initial.sql` (MySQL 8.0.16+ / MariaDB 10.5+ : les `CHECK` doivent être appliqués).
- Niveaux de maîtrise (`DEBUTANT`, `INTERMEDIAIRE`, `EXPERT`) : mêmes valeurs partout (déclaré, validé, attendu, requis).

## 1. Comptes et sécurité

| Table | Rôle | Champs principaux |
|---|---|---|
| `utilisateur` | Compte de connexion | `telephone` unique facultatif, `email` unique facultatif (**au moins l'un des deux obligatoire**, `CHECK`), `mot_de_passe_hash`, `statut`, `telephone_verifie_le`, `email_verifie_le`, `conditions_acceptees_le`, `cree_par_utilisateur_id` |
| `role` | Catalogue des rôles | `code` unique, `libelle` |
| `utilisateur_role` | Rôles attribués | `utilisateur_id`, `role_id`, `attribue_le` |
| `preference_utilisateur` | Préférences | `mode_economie_donnees`, `notifications_actives` |
| `code_verification` | OTP (téléphone ou email) | `canal` (`TELEPHONE`/`EMAIL`), `destinataire`, `usage_code`, hash, expiration, utilisation, tentatives |
| `session_renouvellement` | Refresh tokens | hash du jeton, expiration, révocation |
| `journal_audit` | Traçabilité des actions sensibles | acteur, action, ressource, `resultat`, détails JSON, IP, date |

Rôles insérés dans la table `role` : `CITOYEN`, `ORGANISATION`, `ADMIN`, `SUPER_ADMIN`, `INSTITUTION`, `AGENT_TERRAIN`. **`CENTRE_ADMIN` n'est volontairement pas inséré** : ce rôle se déduit de la table `centre_administrateur` (un compte rattaché à un centre est administrateur de ce centre). Il existe dans l'enum Java `Role` pour les autorisations Spring, mais jamais dans `utilisateur_role`. L'habilitation d'évaluateur n'est **pas** un rôle global : elle est enregistrée séparément.

Statuts de compte : `EN_ATTENTE` (avant vérification de l'OTP), `ACTIF`, `SUSPENDU`, `FERME`.

**OTP.** Le backend génère le code, ne conserve que son hash, et l'envoie par le canal choisi. Tant qu'aucun service SMS/email n'est branché, une implémentation de simulation affiche le code dans la console du serveur (à interdire en production). Usages : `INSCRIPTION`, `RECUPERATION_MOT_DE_PASSE`, `CHANGEMENT_CONTACT`. Durée de validité et nombre maximal de tentatives : configurables.

`cree_par_utilisateur_id` sert à tracer un compte ouvert par un agent de terrain pour une personne qui ne peut pas s'inscrire seule.

## 2. Géographie et référentiels

| Table | Rôle | Champs principaux |
|---|---|---|
| `territoire` | Région, district ou cercle | `code`, `nom`, `niveau` (`REGION`/`DISTRICT`/`CERCLE`), `parent_id`, `actif` |
| `commune` | Commune rattachée à un cercle ou au district | territoire, `code` (obligatoire), nom |
| `secteur_activite` | Secteurs économiques | code, nom, description |
| `metier` | Métiers | secteur, code, nom, description, source |
| `competence` | Catalogue des compétences | code, nom, description, source |
| `metier_competence` | Compétences associées aux métiers (M:N) | métier, compétence, principale, niveau attendu |
| `source_referentiel` | Provenance/version d'un référentiel | organisme, référentiel, version, date d'import |
| `methode_validation` | Méthodes disponibles | code, libellé, description |
| `competence_methode_validation` | Méthodes possibles pour une compétence | compétence, méthode, actif |

Hiérarchie : `REGION` et `DISTRICT` n'ont pas de parent ; un `CERCLE` a une région pour parent. Une commune se rattache au niveau le plus fin disponible : cercle, district ou, **provisoirement, région** (les cercles ne sont pas encore chargés ; les communes de démonstration sont rattachées aux régions). Le backend vérifie la hiérarchie. Pour agréger par région, remonter la chaîne `commune → territoire → parent`.

Une compétence peut être liée à plusieurs métiers. Au chargement des anciennes données, les compétences de même nom sous plusieurs métiers sont **dédoublonnées** (une seule compétence, plusieurs liens `metier_competence`).

## 3. Citoyens et compétences déclarées

| Table | Rôle | Champs principaux |
|---|---|---|
| `citoyen` | Profil citoyen lié à un compte | nom, prénom, **`identifiant_public`** (opaque), date de naissance et sexe facultatifs, `statut_activite`, `profil_public`, `gps_consenti`, `gps_consenti_le` |
| `localisation_profil` | Commune de référence confirmée | citoyen, commune, source, date |
| `citoyen_competence` | Compétence déclarée | citoyen, compétence, `niveau_declare`, **`niveau_valide`**, `statut_validation`, **`situation`**, `disponibilite`, **`derniere_methode_id`**, **`validee_le`**, **`valide_jusqu_au`** |
| `experience` | Expérience du citoyen | intitulé, description, dates facultatives, `en_cours`, employeur/client |
| `experience_competence` | **Compétences démontrées par une expérience (M:N)** | expérience, compétence du citoyen |
| `preuve` | Pièce justificative ou réalisation | compétence du citoyen, type, titre, clé de stockage privé, statut, **`visible_profil`** |

- `citoyen.id` est **distinct** de `utilisateur.id` : ne jamais comparer l'identifiant du jeton à un identifiant de citoyen sans passer par `citoyen.utilisateur_id`.
- `identifiant_public` est le seul identifiant montré aux organisations (pseudonymisation).
- `sexe` : `HOMME`, `FEMME`, `AUTRE`, `NON_RENSEIGNE` (valeurs déjà utilisées par les applications).
- `gps_consenti` / `gps_consenti_le` enregistrent le consentement à la **détection ponctuelle** de la commune ; ils ne servent jamais à suivre le citoyen (voir section 9).
- `statut_activite` : `NON_RENSEIGNE`, `EN_ACTIVITE`, `SANS_ACTIVITE`, `EN_FORMATION`, `ETUDIANT`, `EN_RECONVERSION`, `RETRAITE`, `AUTRE`.
- `situation` d'une compétence : `PRATIQUEE` (exercée actuellement) ou `NON_PRATIQUEE` (maîtrisée mais non exercée). Ensemble avec `disponibilite`, cela répond à « ce qu'il sait faire / ce qu'il fait actuellement ».
- Statuts de validation : `NON_VERIFIEE`, `EN_COURS`, `VALIDEE`, `REJETEE`, `A_REVALIDER`. **Une déclaration n'est jamais une validation.** `derniere_methode_id` indique sur quoi repose la confiance accordée (document, test, centre, portfolio, maître artisan).
- Les **réalisations** montrées sur le profil sont des `preuve` de type `PORTFOLIO`, `PHOTO` ou `VIDEO` avec `visible_profil = TRUE`. Elles restent servies par le backend (contrôle d'accès), jamais par URL publique.
- Contrôle backend obligatoire et transactionnel : une expérience et les compétences qui lui sont liées (`experience_competence`) appartiennent au **même citoyen**.
- Types de preuve : `DIPLOME`, `ATTESTATION`, `PHOTO`, `VIDEO`, `PORTFOLIO`, `AUTRE`. Statuts : `EN_ATTENTE`, `VERIFIEE`, `REJETEE`, `ARCHIVEE`. Un diplôme ou une formation suivie se déposent comme preuve (pas de table de formations en V1).

## 4. Centres, évaluateurs et habilitations

| Table | Rôle | Champs principaux |
|---|---|---|
| `centre` | Centre autorisé | nom, **description**, contacts, commune, statut (`EN_ATTENTE`/`ACTIF`/`SUSPENDU`) |
| `centre_administrateur` | **Compte gestionnaire d'un centre** | `utilisateur_id` (clé primaire : un compte ne gère qu'un centre), centre, attribué par/le |
| `domaine_centre` | Domaine de formation du centre | centre et exactement un domaine : secteur ou métier |
| `centre_competence_validation` | Compétences que le centre peut évaluer | centre, compétence, période, actif (historique possible) |
| `evaluateur` | Personne pouvant recevoir des habilitations | citoyen associé, statut (`EN_ATTENTE`/`ACTIF`/`SUSPENDU`) |
| `habilitation_evaluateur` | Autorisation ciblée | évaluateur, compétence, méthode, centre facultatif, autorité, période, statut (`EN_ATTENTE`/`ACTIVE`/`SUSPENDUE`/`REVOQUEE`) |

Le rôle `CENTRE_ADMIN` **découle** de `centre_administrateur` : on l'obtient en rattachant un compte à un centre (par un administrateur national), on le perd en retirant le rattachement. Il ne s'attribue jamais par un sélecteur de rôles générique, pour éviter deux systèmes d'autorisation contradictoires.

## 5. Validation des compétences

| Table | Rôle | Champs principaux |
|---|---|---|
| `demande_validation` | Demande de validation par une méthode | compétence du citoyen, méthode, **centre**, **évaluateur assigné**, **message du citoyen**, statut, dates |
| `decision_validation` | Historique **immuable** | demande, décideur, décision, **`niveau_valide`**, motif, date |
| `test_numerique` | Version d'un test | compétence, titre, version, `seuil_reussite` (défaut 60) |
| `question` | Question d'une version de test | énoncé, type, options JSON, réponse attendue JSON, pondération, ordre |
| `tentative_test` | Tentative conservée | demande, test, score, **`seuil_applique`**, réussite, réponses, dates |
| `evaluation_pratique` | Évaluation pratique | demande, évaluateur, **centre facultatif**, score, grille, observations |
| `contestation` | **Contestation ou demande de correction** | citoyen, type d'objet, décision visée, **`champ_concerne`** (champ du profil contesté, facultatif), motif, statut, réponse |

Méthodes : `DOCUMENT`, `TEST_NUMERIQUE`, `EVALUATION_CENTRE`, `PORTFOLIO`, `MAITRE_ARTISAN`. Les méthodes sont alternatives ; **aucune ne suppose un document** (un maître artisan ou un test n'exigent pas de pièce). Le portfolio est une pièce à examiner, pas une validation automatique.

- Statuts de demande : `EN_ATTENTE`, `EN_COURS`, `COMPLEMENT_REQUIS`, `VALIDEE`, `REJETEE`, `ANNULEE`. Décisions : `VALIDEE`, `REJETEE`, `COMPLEMENT_REQUIS`, `ANNULEE`.
- La compétence d'une tentative ou d'une évaluation se **déduit** de la demande (plus de colonne redondante, donc pas d'incohérence possible).
- `evaluation_pratique` couvre `EVALUATION_CENTRE` (centre renseigné) et `MAITRE_ARTISAN` (centre facultatif).
- `seuil_applique` fige le seuil réellement utilisé : modifier plus tard un test n'altère pas les anciens résultats. Un test publié ne se modifie pas ; on crée une nouvelle version.
- Types de question : `CHOIX_UNIQUE`, `CHOIX_MULTIPLE`, `VRAI_FAUX`, `TEXTE_LIBRE` (ce dernier exige une correction manuelle).
- **Immuabilité des décisions** : la base ne l'impose pas (pas de trigger) ; le backend n'expose aucune opération de modification ni de suppression sur `decision_validation`. Toute correction crée une **nouvelle** décision.
- Contestation : `OUVERTE`, `EN_EXAMEN`, `ACCEPTEE`, `REJETEE`. Une contestation acceptée aboutit à une **nouvelle** décision, jamais à la modification de l'ancienne.

## 6. Organisations et besoins

| Table | Rôle | Champs principaux |
|---|---|---|
| `organisation` | Profil d'une organisation | raison sociale, nom commercial, description, RCCM/NIF facultatifs, contacts, commune, `statut_verification`, `motif_statut` |
| `justificatif_organisation` | Pièces de vérification | type, référence, clé privée, dates, statut, vérificateur, motif de rejet |
| `besoin` | Besoin publié | titre, description, statut, date limite, date de publication |
| `besoin_competence` | Compétences demandées | besoin, compétence, niveau requis, **`nombre_requis`** |
| `besoin_zone` | Portée géographique | besoin, soit commune, soit territoire (région, district ou cercle) ; sans doublon |

- Statuts de vérification : `EN_ATTENTE`, `APPROUVEE`, `REJETEE`, `SUSPENDUE`. Un numéro RCCM/NIF ou un document fourni ne signifie pas qu'il a été authentifié.
- Statuts de besoin : `BROUILLON`, `OUVERT`, `CLOTURE`, `ANNULE`. **Le besoin n'a plus d'effectif global** : la quantité vit dans `besoin_competence.nombre_requis`, seule source du calcul de la demande.
- Un besoin sans zone est traité comme national.

## 7. Opportunités

| Table | Rôle | Champs principaux |
|---|---|---|
| `categorie_opportunite` | Catégories | nom, description |
| `opportunite` | Formation, bourse, programme, concours… | catégorie, titre, description, organisme, lien externe, contact, territoire facultatif, statut, dates, auteur |

Catégories initiales : Formations et apprentissage, Bourses, Programmes, Concours et appels, Accompagnement. Statuts : `BROUILLON`, `PUBLIEE`, `EXPIREE`, `ARCHIVEE`. Une opportunité dont la date d'expiration est passée n'est jamais affichée, même si le statut `EXPIREE` n'a pas encore été positionné. Seuls des comptes autorisés (administrateurs, centres, institutions) publient.

## 8. Notifications, suivi et mise en relation

| Table | Rôle | Champs principaux |
|---|---|---|
| `notification` | Notification persistante | destinataire, événement, titre, contenu, ressource, dates, clé anti-doublon |
| `appareil_notification` | Appareil push | compte, hash du jeton, plateforme (`ANDROID`/`IOS`/`WEB`), actif |
| `suivi_besoin_talent` | Suivi interne d'un talent pour un besoin | besoin, citoyen, statut, `note_interne`, auteur |
| `demande_mise_en_relation` | Demande de contact | suivi, statut, message, dates, **`en_attente_cle`** (colonne générée) |
| `consultation_profil` | Consultations de profil | organisation, citoyen, besoin facultatif, date |

- La notification en base est la référence ; le push n'est qu'un canal de livraison.
- Statuts de suivi : `A_L_ETUDE`, `CONTACT_EN_COURS`, `MISE_EN_RELATION`, `ARCHIVE`. Statuts de demande : `EN_ATTENTE`, `ACCEPTEE`, `REFUSEE`, `ANNULEE`.
- `en_attente_cle` impose **en base** une seule demande en attente par suivi (à mapper en lecture seule côté JPA). L'historique des demandes passées est conservé.
- Le recrutement reste externe à la plateforme.

## 9. Localisation et statistiques

| Table | Rôle | Champs principaux |
|---|---|---|
| `localisation_profil` | **Source officielle** de la commune de référence (une ligne par citoyen) | citoyen, commune, source, date de confirmation |
| `demande_actualisation_localisation` | Proposition de changement de commune | citoyen, commune suggérée, statut, dates, `en_attente_cle` (colonne générée) |

Il n'existe **ni table de positions GPS ni table d'indicateurs** en V1.

**Localisation.** La commune de référence est confirmée par le citoyen et actualisable à tout moment (à l'ouverture du profil, sur signalement d'un déménagement, ou en renseignant volontairement une nouvelle commune). Une suggestion ponctuelle peut s'appuyer sur le GPS, sans suivi permanent :
- GPS consenti et application ouverte → la position est reçue **temporairement**, convertie en commune, comparée à la commune de référence, puis oubliée ;
- communes identiques → aucune action ; différentes → proposition de confirmation (`demande_actualisation_localisation`) ;
- refus ou voyage → la commune de référence est conservée ; confirmation → `localisation_profil` est mise à jour ;
- GPS refusé ou indisponible → l'application fonctionne normalement.

Règles : le GPS n'est ni obligatoire ni un moyen de suivre les déplacements ; il ne prouve pas un déménagement (il indique où se trouve le téléphone un instant) ; **la commune de référence ne change jamais automatiquement** ; **aucune coordonnée n'est stockée**, ni journalisée, ni présente dans les traces techniques ou messages d'erreur ; une seule demande en attente par citoyen (garantie en base par `en_attente_cle`) et un délai configurable avant une nouvelle suggestion (calculé à partir de `repondue_le`).

**Statistiques.** Offre, demande et écart sont **calculés à la demande** (règles 5 à 8). Les statistiques publiques masquent les groupes trop petits (seuil initial : 5 personnes distinctes, avec masquage complémentaire pour éviter la déduction par différence) et affichent leur date de calcul, leurs critères et les limites de couverture. Des snapshots pourront être ajoutés plus tard si un historique des chiffres devient nécessaire.

## 10. Règles métier

Certaines règles sont garanties par la base (contraintes `CHECK`, clés uniques, colonnes générées) ; **les autres sont à faire respecter par le backend**, qui ne doit pas supposer que la base les contrôle. Le package `common.regle` du backend en contient une implémentation pure et testée.

### Garanties par la base
- au moins un contact (téléphone ou email) par compte ; unicité de chaque contact ;
- valeurs des statuts, niveaux et types (`CHECK`) ; un domaine de centre = un secteur **ou** un métier ; une zone de besoin = une commune **ou** un territoire ;
- une seule demande de mise en relation en attente par suivi ; une seule demande d'actualisation de localisation en attente par citoyen ;
- une seule compétence par citoyen et par compétence du catalogue ; pas de doublon de zone ou de domaine.

### À faire respecter par le backend
1. **Décision de validation** : une décision favorable met à jour `citoyen_competence` (`statut_validation`, `niveau_valide`, `derniere_methode_id`, `validee_le`) **dans la même transaction** que l'enregistrement de la décision. Une compétence `VALIDEE` a toujours un niveau validé, une date de validation et une décision cohérente. Les décisions ne sont jamais modifiées ni supprimées.
2. **Évaluation** : l'évaluateur est actif, son habilitation est `ACTIVE`, couvre la compétence **et** la méthode, n'est pas expirée, et il n'évalue pas sa propre compétence. Le centre est actif et agréé pour la compétence ; le test d'une tentative porte sur la même compétence que la demande.
3. **Appartenance** : une expérience et les compétences qui lui sont liées appartiennent au même citoyen ; l'identifiant du jeton est un identifiant de **compte**, jamais de citoyen.
4. **Hiérarchie territoriale** : un cercle appartient à une région ; une commune appartient à un cercle, au district de Bamako ou (provisoirement) à une région.
5. **Offre** par compétence = citoyens **distincts** ayant cette compétence `VALIDEE` et non expirée, avec les filtres d'activité/disponibilité explicitement définis.
6. **Demande** par compétence = somme de `nombre_requis` des besoins `OUVERT` et non expirés. Un besoin `BROUILLON`, `CLOTURE`, `ANNULE` ou expiré n'est pas compté.
7. **Zones de besoin qui se chevauchent** : un besoin sans zone est **national** et n'entre que dans la demande nationale ; un besoin zoné est compté **une seule fois** par compétence et par territoire, même si plusieurs de ses zones le couvrent.
8. **Écart** = offre − demande, calculé à la demande. Ne jamais additionner les totaux par compétence pour compter des personnes uniques.
9. **Inscription** : au moins un contact réellement renseigné (une chaîne vide n'en est pas un), compte `EN_ATTENTE` jusqu'à validation de l'OTP, conditions acceptées. Une inscription assistée trace l'agent (`cree_par_utilisateur_id`) et recueille le consentement de la personne.
10. **Confidentialité** : contrôle serveur de l'accès aux justificatifs, réponses de test, notes internes et réalisations non publiques ; aucune coordonnée GPS stockée ni journalisée.
11. **Transitions de statuts** : table de transitions explicite et testée pour les demandes de validation, compétences, besoins, mises en relation, suivis, contestations, organisations et actualisations de localisation.
12. **À configurer et tester** : déduplication des notifications, seuils statistiques, durée de l'OTP et nombre de tentatives, délai de suggestion de localisation, délai avant une nouvelle mise en relation après un refus (30 jours dans l'ancien backend), durée de validité d'une compétence validée.
13. Une compétence non renseignée n'est pas une compétence inexistante ; l'absence de diplôme n'est pas l'absence de savoir-faire (libellés et statistiques).

## 11. Reporté après la V1 (volontairement exclu)

- Stockage des positions GPS et suivi de localisation (supprimés : détection ponctuelle sans conservation).
- Table d'indicateurs (`indicateur_snapshot`) : calcul à la demande ; à ajouter si un historique devient nécessaire.
- Objectifs professionnels et besoins de formation des citoyens (et alertes d'opportunités selon leurs intérêts).
- Table dédiée aux formations suivies (couvertes par les `preuve` de type diplôme/attestation).
- Tables séparées pour le portfolio et les médias (couverts par `preuve` + `visible_profil`).
- Photo de profil, code passeport de l'ancien modèle.
- Disponibilité à plusieurs valeurs (l'ancien enum à 4 valeurs devient un booléen par compétence).
- Rôle et écrans complets de l'agent de terrain (rôle et traçabilité prévus ; parcours à venir).
- Modération multi-étapes des opportunités, analyses temporelles avancées, intégrations institutionnelles.

## 12. Ordre de création et données initiales

`V1__schema_initial.sql` crée les tables dans l'ordre des dépendances : sécurité, géographie/référentiels, citoyens/compétences, centres/évaluateurs, validation, organisations/besoins, opportunités, notifications, suivi, localisation, puis les données de base (rôles, méthodes, catégories d'opportunités).

Migrations de données : `V2` (19 régions, District de Bamako, communes), `V3` (8 secteurs, 24 métiers, 72 compétences, liens métier-compétence, méthodes par défaut), `V4` (8 tests numériques, 32 questions). Identifiants déterministes (UUID v5). Les communes des régions sont des **données de démonstration** ; seules les 6 communes de Bamako sont réelles.
