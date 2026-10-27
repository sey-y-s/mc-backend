# Dictionnaire de données

Généré depuis le schéma réel (migrations V1 à V5, base MariaDB). Une section par table : rôle, puis colonnes.

**Lecture :** `PK` clé primaire · `UQ` valeur unique · `→ table.colonne` clé étrangère · `NULL` facultatif.

Pour le régénérer : appliquer les migrations sur une base vide, puis interroger `information_schema` (colonnes, types, clés).


## Sommaire

- [`audit_log`](#audit_log) — Journal des actions sensibles (qui, quoi, résultat).
- [`besoin`](#besoin) — Besoin de recrutement ou de compétences publié par une organisation.
- [`besoin_competence`](#besoin_competence) — Compétences demandées par un besoin (niveau minimum, quantité).
- [`candidature_opportunite`](#candidature_opportunite) — Candidature d’un citoyen à une opportunité.
- [`categorie_opportunite`](#categorie_opportunite) — Catégories d’opportunités (formation, bourse…).
- [`centre`](#centre) — Centre de formation ou d’évaluation agréé.
- [`centre_competence`](#centre_competence) — Compétences pour lesquelles un centre est agréé.
- [`citoyen`](#citoyen) — Profil du citoyen (passeport de compétences, commune de référence, disponibilité).
- [`citoyen_competence`](#citoyen_competence) — Compétence déclarée par un citoyen, avec niveau et état de validation.
- [`code_verification`](#code_verification) — Codes OTP de vérification du téléphone ou de l’e-mail (hachés, avec compteur d’essais).
- [`commune`](#commune) — Communes rattachées à une région.
- [`competence`](#competence) — Compétences, rattachées à un métier.
- [`competence_modalite`](#competence_modalite) — Règles de validation d’une compétence (formelle ou informelle).
- [`consultation_profil`](#consultation_profil) — Trace des consultations d’un profil citoyen.
- [`contestation`](#contestation) — Contestation d’une décision ou d’une donnée de profil par un citoyen.
- [`demande_actualisation_localisation`](#demande_actualisation_localisation) — Suggestions de changement de commune issues de la détection ponctuelle, à confirmer.
- [`demande_mise_en_relation`](#demande_mise_en_relation) — Demande de contact d’une organisation vers un talent, avec réponse.
- [`evaluateur_centre`](#evaluateur_centre) — Évaluateur rattaché à un centre.
- [`evaluateur_habilitation`](#evaluateur_habilitation) — Compétences qu’un évaluateur est habilité à valider.
- [`experience`](#experience) — Expériences professionnelles ou de formation d’un citoyen.
- [`experience_competence`](#experience_competence) — Lien entre une expérience et les compétences qu’elle illustre.
- [`indicateur_skills_gap`](#indicateur_skills_gap) — Écart offre/demande de compétences par région (données de démonstration).
- [`jeton_renouvellement`](#jeton_renouvellement) — Jetons de renouvellement de session (empreinte SHA-256, rotation par famille).
- [`media_realisation`](#media_realisation) — Fichiers (photos, documents) d’une réalisation.
- [`metier`](#metier) — Métiers, rattachés à un secteur.
- [`notification`](#notification) — Notification émise.
- [`notification_destinataire`](#notification_destinataire) — Destinataires et état de lecture d’une notification.
- [`opportunite`](#opportunite) — Opportunités publiées (formation, bourse, programme…).
- [`organisation`](#organisation) — Profil de l’organisation (identité juridique, statut de validation).
- [`organisation_document`](#organisation_document) — Pièces justificatives déposées par une organisation.
- [`portfolio_realisation`](#portfolio_realisation) — Réalisations du portfolio d’un citoyen.
- [`preferences_utilisateur`](#preferences_utilisateur) — Préférences de l’application (mode économie de données, notifications).
- [`preuve`](#preuve) — Preuve (fichier, lien, attestation) appuyant une compétence.
- [`proposition_reponse`](#proposition_reponse) — Propositions de réponse d’une question.
- [`question`](#question) — Questions d’un test numérique.
- [`region`](#region) — Régions, district et cercles du Mali (hiérarchie par parent_id).
- [`reset_password_token`](#reset_password_token) — Codes à usage unique de réinitialisation du mot de passe (stockés hachés).
- [`resultat_test`](#resultat_test) — Résultat d’un citoyen à un test numérique.
- [`secteur_activite`](#secteur_activite) — Secteurs d’activité du référentiel.
- [`suivi_besoin_talent`](#suivi_besoin_talent) — Suivi d’un talent retenu pour un besoin.
- [`test_numerique`](#test_numerique) — Test de positionnement lié à une compétence.
- [`utilisateur`](#utilisateur) — Compte de connexion (téléphone et/ou e-mail, mot de passe haché, rôle, vérification OTP).
- [`validation`](#validation) — Demande et décision de validation d’une compétence.

## audit_log

Journal des actions sensibles (qui, quoi, résultat).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `created_at` | `datetime(6)` | — |
| `actor_id` | `varchar(100)` | NULL |
| `action` | `varchar(80)` | — |
| `target_type` | `varchar(80)` | — |
| `target_id` | `varchar(100)` | NULL |
| `result_code` | `varchar(24)` | — |

## besoin

Besoin de recrutement ou de compétences publié par une organisation.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `organisation_id` | `char(36)` | → organisation.id |
| `titre` | `varchar(200)` | — |
| `description` | `text` | NULL |
| `statut` | `varchar(30)` | — |
| `date_debut` | `date` | NULL |
| `date_fin` | `date` | NULL |
| `date_creation` | `datetime` | — |
| `latitude` | `double` | NULL |
| `longitude` | `double` | NULL |
| `region_id` | `char(36)` | → region.id · NULL |
| `commune_id` | `char(36)` | → commune.id · NULL |

## besoin_competence

Compétences demandées par un besoin (niveau minimum, quantité).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `besoin_id` | `char(36)` | → besoin.id |
| `competence_id` | `char(36)` | → competence.id |
| `niveau_minimum` | `varchar(30)` | — |
| `quantite` | `int(11)` | — |

## candidature_opportunite

Candidature d’un citoyen à une opportunité.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `opportunite_id` | `char(36)` | → opportunite.id |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `message` | `text` | NULL |
| `statut` | `varchar(20)` | — |
| `locale` | `tinyint(1)` | défaut 0 |
| `date_candidature` | `datetime` | — |

## categorie_opportunite

Catégories d’opportunités (formation, bourse…).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `nom` | `varchar(150)` | UQ |
| `description` | `text` | NULL |

## centre

Centre de formation ou d’évaluation agréé.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `nom` | `varchar(150)` | — |
| `description` | `text` | NULL |
| `adresse` | `varchar(255)` | NULL |
| `telephone` | `varchar(30)` | NULL |
| `administrateur_id` | `char(36)` | → utilisateur.id · NULL |
| `statut` | `varchar(20)` | défaut 'ACTIF' |
| `commune_id` | `char(36)` | → commune.id · NULL |

## centre_competence

Compétences pour lesquelles un centre est agréé.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `centre_id` | `char(36)` | → centre.id |
| `competence_id` | `char(36)` | → competence.id |
| `actif` | `tinyint(1)` | défaut 1 |
| `date_debut` | `date` | NULL |
| `date_fin` | `date` | NULL |

## citoyen

Profil du citoyen (passeport de compétences, commune de référence, disponibilité).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK · → utilisateur.id |
| `nom` | `varchar(100)` | — |
| `prenom` | `varchar(100)` | — |
| `sexe` | `varchar(20)` | NULL |
| `photo_url` | `varchar(500)` | NULL |
| `disponibilite` | `varchar(30)` | — |
| `code_passeport` | `char(14)` | UQ |
| `commune_id` | `char(36)` | → commune.id · NULL |
| `gps_consenti` | `tinyint(1)` | défaut 0 |
| `gps_consenti_le` | `datetime` | NULL |
| `localisation_source` | `varchar(20)` | NULL |
| `localisation_confirmee_le` | `datetime` | NULL |

## citoyen_competence

Compétence déclarée par un citoyen, avec niveau et état de validation.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `competence_id` | `char(36)` | → competence.id |
| `niveau` | `varchar(30)` | — |
| `etat` | `varchar(30)` | — |
| `date_declaration` | `datetime` | — |
| `parcours` | `varchar(20)` | NULL |
| `niveau_valide` | `varchar(30)` | NULL |
| `validee_le` | `datetime` | NULL |
| `valide_jusqu_au` | `date` | NULL |

## code_verification

Codes OTP de vérification du téléphone ou de l’e-mail (hachés, avec compteur d’essais).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `utilisateur_id` | `char(36)` | → utilisateur.id |
| `canal` | `varchar(20)` | — |
| `code_hash` | `varchar(100)` | — |
| `expire_le` | `datetime` | — |
| `utilise_le` | `datetime` | NULL |
| `tentatives` | `int(11)` | défaut 0 |
| `cree_le` | `datetime` | — |

## commune

Communes rattachées à une région.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `region_id` | `char(36)` | → region.id |
| `nom` | `varchar(100)` | — |
| `description` | `text` | NULL |
| `cercle_id` | `char(36)` | → region.id · NULL |
| `latitude` | `double` | NULL |
| `longitude` | `double` | NULL |

## competence

Compétences, rattachées à un métier.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `metier_id` | `char(36)` | → metier.id |
| `nom` | `varchar(200)` | — |
| `description` | `text` | NULL |
| `parcours_possibles` | `varchar(30)` | défaut 'FORMEL,INFORMEL' |

## competence_modalite

Règles de validation d’une compétence (formelle ou informelle).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `competence_id` | `char(36)` | → competence.id |
| `modalite` | `varchar(30)` | — |
| `ordre` | `int(11)` | défaut 0 |
| `seuil_reussite` | `int(11)` | NULL |
| `pieces_admissibles` | `varchar(300)` | NULL |
| `profils_habilites` | `varchar(300)` | NULL |
| `criteres` | `varchar(500)` | NULL |
| `actif` | `tinyint(1)` | défaut 1 |

## consultation_profil

Trace des consultations d’un profil citoyen.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `utilisateur_id` | `char(36)` | → utilisateur.id |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `date_consultation` | `datetime` | — |

## contestation

Contestation d’une décision ou d’une donnée de profil par un citoyen.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `type_objet` | `varchar(30)` | — |
| `validation_id` | `char(36)` | → validation.id · NULL |
| `champ_concerne` | `varchar(60)` | NULL |
| `motif` | `text` | — |
| `statut` | `varchar(20)` | défaut 'OUVERTE' |
| `reponse` | `text` | NULL |
| `traitee_par_id` | `char(36)` | → utilisateur.id · NULL |
| `nouvelle_decision_id` | `char(36)` | → validation.id · NULL |
| `date_creation` | `datetime` | — |
| `date_traitement` | `datetime` | NULL |

## demande_actualisation_localisation

Suggestions de changement de commune issues de la détection ponctuelle, à confirmer.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `commune_suggeree_id` | `char(36)` | → commune.id |
| `statut` | `varchar(20)` | défaut 'EN_ATTENTE' |
| `date_creation` | `datetime` | — |
| `date_reponse` | `datetime` | NULL |
| `en_attente_cle` | `char(36)` | UQ · NULL |

## demande_mise_en_relation

Demande de contact d’une organisation vers un talent, avec réponse.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `demandeur_id` | `char(36)` | → utilisateur.id |
| `destinataire_id` | `char(36)` | → citoyen.id |
| `suivi_besoin_talent_id` | `char(36)` | → suivi_besoin_talent.id · NULL |
| `statut` | `varchar(30)` | — |
| `message` | `text` | NULL |
| `date_demande` | `datetime` | — |
| `date_reponse` | `datetime` | NULL |
| `en_attente_cle` | `char(36)` | UQ · NULL |

## evaluateur_centre

Évaluateur rattaché à un centre.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `centre_id` | `char(36)` | → centre.id |
| `designe_par_id` | `char(36)` | → utilisateur.id |
| `specialite` | `varchar(200)` | NULL |
| `actif` | `tinyint(1)` | défaut 1 |
| `date_designation` | `datetime` | — |
| `date_fin_habilitation` | `date` | NULL |

## evaluateur_habilitation

Compétences qu’un évaluateur est habilité à valider.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `evaluateur_centre_id` | `char(36)` | → evaluateur_centre.id |
| `competence_id` | `char(36)` | → competence.id |

## experience

Expériences professionnelles ou de formation d’un citoyen.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `titre` | `varchar(200)` | — |
| `description` | `text` | NULL |
| `entreprise` | `varchar(200)` | NULL |
| `date_debut` | `date` | — |
| `date_fin` | `date` | NULL |
| `en_cours` | `tinyint(1)` | défaut 0 |

## experience_competence

Lien entre une expérience et les compétences qu’elle illustre.

| Colonne | Type | Contraintes |
|---|---|---|
| `experience_id` | `char(36)` | PK · → experience.id |
| `citoyen_competence_id` | `char(36)` | PK · → citoyen_competence.id |

## indicateur_skills_gap

Écart offre/demande de compétences par région (données de démonstration).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `region_id` | `char(36)` | → region.id |
| `competence_id` | `char(36)` | → competence.id |
| `periode_debut` | `date` | — |
| `periode_fin` | `date` | — |
| `date_calcul` | `datetime` | — |
| `demande_estimee` | `int(11)` | — |
| `offre_disponible` | `int(11)` | — |
| `ecart` | `int(11)` | — |
| `effectif_contributeurs` | `int(11)` | NULL |

## jeton_renouvellement

Jetons de renouvellement de session (empreinte SHA-256, rotation par famille).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `utilisateur_id` | `char(36)` | → utilisateur.id |
| `famille_id` | `char(36)` | — |
| `empreinte` | `char(64)` | UQ |
| `cree_le` | `datetime` | — |
| `expire_le` | `datetime` | — |
| `utilise_le` | `datetime` | NULL |
| `revoque` | `tinyint(1)` | défaut 0 |

## media_realisation

Fichiers (photos, documents) d’une réalisation.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `portfolio_realisation_id` | `char(36)` | → portfolio_realisation.id |
| `type` | `varchar(30)` | — |
| `url` | `varchar(500)` | — |
| `nom_fichier` | `varchar(255)` | NULL |

## metier

Métiers, rattachés à un secteur.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `secteur_activite_id` | `char(36)` | → secteur_activite.id |
| `nom` | `varchar(150)` | — |
| `description` | `text` | NULL |

## notification

Notification émise.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `titre` | `varchar(200)` | — |
| `message` | `text` | — |
| `type` | `varchar(40)` | — |
| `date_creation` | `datetime` | — |

## notification_destinataire

Destinataires et état de lecture d’une notification.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `notification_id` | `char(36)` | → notification.id |
| `utilisateur_id` | `char(36)` | → utilisateur.id |
| `lu` | `tinyint(1)` | défaut 0 |
| `date_reception` | `datetime` | — |
| `date_lecture` | `datetime` | NULL |

## opportunite

Opportunités publiées (formation, bourse, programme…).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `categorie_id` | `char(36)` | → categorie_opportunite.id |
| `titre` | `varchar(200)` | — |
| `description` | `text` | — |
| `statut` | `varchar(30)` | — |
| `type` | `varchar(40)` | — |
| `date_publication` | `datetime` | NULL |
| `date_expiration` | `date` | NULL |
| `region_id` | `char(36)` | → region.id · NULL |
| `commune_id` | `char(36)` | → commune.id · NULL |
| `date_diffusion` | `datetime` | NULL |

## organisation

Profil de l’organisation (identité juridique, statut de validation).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK · → utilisateur.id |
| `nom` | `varchar(150)` | — |
| `description` | `text` | NULL |
| `statut` | `varchar(30)` | — |
| `motif_statut` | `text` | NULL |
| `latitude` | `double` | NULL |
| `longitude` | `double` | NULL |
| `date_position` | `datetime` | NULL |
| `type_organisation` | `varchar(40)` | NULL |
| `sigle` | `varchar(30)` | NULL |
| `forme_juridique` | `varchar(80)` | NULL |
| `secteur_activite` | `varchar(150)` | NULL |
| `date_creation` | `date` | NULL |
| `effectif` | `int(11)` | NULL |
| `numero_rccm` | `varchar(80)` | NULL |
| `nif` | `varchar(40)` | NULL |
| `numero_inps` | `varchar(40)` | NULL |
| `numero_recepisse` | `varchar(80)` | NULL |
| `numero_accord_cadre` | `varchar(80)` | NULL |
| `reference_acte` | `varchar(150)` | NULL |
| `adresse` | `varchar(255)` | NULL |
| `commune_id` | `char(36)` | → commune.id · NULL |
| `site_web` | `varchar(255)` | NULL |
| `responsable_nom` | `varchar(150)` | NULL |
| `responsable_fonction` | `varchar(100)` | NULL |
| `responsable_telephone` | `varchar(30)` | NULL |

## organisation_document

Pièces justificatives déposées par une organisation.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `organisation_id` | `char(36)` | → organisation.id |
| `type_document` | `varchar(40)` | — |
| `nom_fichier` | `varchar(255)` | — |
| `chemin_fichier` | `varchar(500)` | — |
| `type_contenu` | `varchar(100)` | NULL |
| `taille_octets` | `bigint(20)` | NULL |
| `statut` | `varchar(20)` | — |
| `commentaire` | `text` | NULL |
| `date_depot` | `datetime` | — |

## portfolio_realisation

Réalisations du portfolio d’un citoyen.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `titre` | `varchar(200)` | — |
| `description` | `text` | NULL |
| `date_realisation` | `date` | NULL |
| `lien_url` | `varchar(500)` | NULL |

## preferences_utilisateur

Préférences de l’application (mode économie de données, notifications).

| Colonne | Type | Contraintes |
|---|---|---|
| `utilisateur_id` | `char(36)` | PK · → utilisateur.id |
| `low_data_mode` | `tinyint(1)` | défaut 0 |
| `notifications_enabled` | `tinyint(1)` | défaut 1 |

## preuve

Preuve (fichier, lien, attestation) appuyant une compétence.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_competence_id` | `char(36)` | → citoyen_competence.id |
| `type` | `varchar(40)` | — |
| `titre` | `varchar(200)` | — |
| `description` | `text` | NULL |
| `fichier_url` | `varchar(500)` | NULL |
| `date_depot` | `datetime` | — |

## proposition_reponse

Propositions de réponse d’une question.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `question_id` | `char(36)` | → question.id |
| `texte` | `text` | — |
| `correcte` | `tinyint(1)` | défaut 0 |

## question

Questions d’un test numérique.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `test_id` | `char(36)` | → test_numerique.id |
| `texte` | `text` | — |
| `type` | `varchar(30)` | — |
| `ordre` | `int(11)` | — |

## region

Régions, district et cercles du Mali (hiérarchie par parent_id).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `nom` | `varchar(100)` | UQ |
| `description` | `text` | NULL |
| `code_stable` | `varchar(32)` | UQ · NULL |
| `niveau` | `varchar(16)` | défaut 'REGION' |
| `latitude` | `double` | NULL |
| `longitude` | `double` | NULL |
| `parent_id` | `char(36)` | → region.id · NULL |

## reset_password_token

Codes à usage unique de réinitialisation du mot de passe (stockés hachés).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `utilisateur_id` | `char(36)` | → utilisateur.id |
| `code_hash` | `varchar(100)` | — |
| `expire_le` | `datetime` | — |
| `utilise` | `tinyint(1)` | défaut 0 |
| `tentatives` | `int(11)` | défaut 0 |
| `date_creation` | `datetime` | — |

## resultat_test

Résultat d’un citoyen à un test numérique.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `test_id` | `char(36)` | → test_numerique.id |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `score` | `decimal(5,2)` | — |
| `reussi` | `tinyint(1)` | — |
| `date_passage` | `datetime` | — |
| `preuve_id` | `char(36)` | UQ · → preuve.id · NULL |

## secteur_activite

Secteurs d’activité du référentiel.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `nom` | `varchar(150)` | UQ |
| `description` | `text` | NULL |

## suivi_besoin_talent

Suivi d’un talent retenu pour un besoin.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `organisation_id` | `char(36)` | → organisation.id |
| `besoin_id` | `char(36)` | → besoin.id |
| `citoyen_id` | `char(36)` | → citoyen.id |
| `statut` | `varchar(30)` | — |
| `score_correspondance` | `decimal(5,2)` | NULL |
| `details_correspondance` | `longtext` | NULL |
| `note_interne` | `text` | NULL |
| `date_creation` | `datetime` | — |
| `date_mise_a_jour` | `datetime` | — |

## test_numerique

Test de positionnement lié à une compétence.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `competence_id` | `char(36)` | → competence.id |
| `titre` | `varchar(200)` | — |
| `description` | `text` | NULL |
| `actif` | `tinyint(1)` | défaut 1 |

## utilisateur

Compte de connexion (téléphone et/ou e-mail, mot de passe haché, rôle, vérification OTP).

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `telephone` | `varchar(30)` | UQ |
| `email` | `varchar(255)` | UQ · NULL |
| `password_hash` | `varchar(255)` | — |
| `actif` | `tinyint(1)` | défaut 1 |
| `date_creation` | `datetime` | — |
| `role` | `varchar(30)` | — |
| `conditions_acceptees_le` | `datetime` | NULL |
| `cree_par_id` | `char(36)` | → utilisateur.id · NULL |
| `consentement_recueilli_le` | `datetime` | NULL |
| `telephone_verifie_le` | `datetime` | NULL |
| `email_verifie_le` | `datetime` | NULL |

## validation

Demande et décision de validation d’une compétence.

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `char(36)` | PK |
| `citoyen_competence_id` | `char(36)` | → citoyen_competence.id |
| `preuve_id` | `char(36)` | → preuve.id · NULL |
| `validee_par_id` | `char(36)` | → utilisateur.id |
| `centre_id` | `char(36)` | → centre.id · NULL |
| `type` | `varchar(40)` | — |
| `statut` | `varchar(30)` | — |
| `date_validation` | `datetime` | NULL |
| `commentaire` | `text` | NULL |
| `date_expiration` | `date` | NULL |
| `date_demande` | `datetime` | — |
| `niveau_valide` | `varchar(30)` | NULL |
| `decision_corrigee_id` | `char(36)` | → validation.id · NULL |
