> **OBSOLÈTE (V1.1)** : ce document décrit l'ancien modèle. Référence actuelle : `docs/dictionnaire-donnees.md` et `docs/PLAN_PORTAGE_V1_1.md`.

# Corrections du modèle métier — MaliCompétences

## Gestion des centres et évaluateurs

- `CENTRE_ADMIN` est le seul rôle de gestionnaire de centre.
- Un compte gestionnaire est rattaché à un seul centre via `Centre.administrateur`.
- Le rôle `CENTRE_ADMIN` doit être attribué par l'opération d'affectation au centre, pas par la création ou la modification générique d'un utilisateur.
- `EVALUATEUR` n'est plus un rôle de compte. Un évaluateur reste un `CITOYEN`; ses droits d'évaluation proviennent de son affectation active `EvaluateurCentre`.
- L'ancienne valeur de rôle `EVALUATEUR` est convertie en `CITOYEN` par migration. Les données d'affectation sont conservées.

## Territoires

- L'entité Java et ses DTO s'appellent désormais `Territoire`, `TerritoireRequest` et `TerritoireResponse`.
- Pour éviter une migration de données inutile, la table SQL historique `region`, la colonne `region_id` et les routes `/api/v1/regions` restent temporairement en place.
- Les associations JPA de `Commune` et `IndicateurSkillsGap` utilisent désormais le nom conceptuel `territoire`.
- La définition de l'écart Skills-Gap est `offreDisponible - demandeEstimee`. La migration recalcule les valeurs existantes.

## Consultations de profil

- L'historique est enregistré lors de l'ouverture d'un profil talent, et non lors de la simple recherche.
- Le visiteur est dérivé du jeton d'authentification; le client ne peut plus fournir librement son propre `utilisateurId`.
- Un citoyen peut consulter l'historique de son propre profil via `GET /api/v1/citoyens/me/consultations`.
- Les entrées indiquent le nom du visiteur, son type et la date de consultation.

## Identifiants publics des talents

- Les espaces talent et organisation utilisent `Citoyen.identifiantPublic`, distinct de l'ID interne du compte.
- Les réponses de recherche, matching, suivi et mise en relation utilisent cet identifiant public et un pseudonyme.
- Le nom du talent n'est exposé dans la réponse de mise en relation qu'après acceptation. Les notes internes de l'organisation ne sont jamais renvoyées au citoyen.
- La migration `V14` attribue un identifiant public à chaque citoyen existant.

## Mise en relation

- Le demandeur est déduit de la session; les champs `demandeurId` et `statut` éventuellement reçus dans le corps ne contrôlent pas la création.
- Seule une organisation approuvée peut initier une demande pour un besoin ouvert.
- Seul le citoyen destinataire peut accepter ou refuser une demande en attente.
- Une seule demande peut être en attente par suivi; les demandes passées restent conservées. Une nouvelle demande après refus est bloquée pendant 30 jours.
- Le statut du suivi est piloté par le flux : `A_L_ETUDE` à la création, `CONTACT_EN_COURS` à la demande, `MISE_EN_RELATION` après acceptation, retour à `A_L_ETUDE` après refus. L'organisation peut archiver manuellement un suivi.

## Migrations

- `V13__coherence_gestion_centre.sql` normalise les rôles, impose l'unicité du gestionnaire par centre, indexe les consultations et recalcule le Skills-Gap.
- `V14__identifiant_public_talent.sql` ajoute les identifiants publics et l'index utile aux demandes liées aux suivis.
- **Avant de déployer V13**, vérifier qu'aucun compte n'est affecté à plusieurs centres. La migration échoue volontairement en présence de doublons afin de ne pas choisir arbitrairement le centre à conserver.


## RCCM des organisations

Le profil organisation contient `numeroRccm` (colonne `organisation.numero_rccm`, longueur maximale de 80 caractères). Le champ est facultatif à l’inscription et modifiable depuis le profil. Sa présence facilite le contrôle administratif, mais ne constitue pas à elle seule une vérification automatique de l’entreprise. La migration correspondante est `V15__numero_rccm_organisation.sql`.

## Gestionnaire de centre dans le portail web

Le rôle `CENTRE_ADMIN` est attribué exclusivement via l’affectation d’un gestionnaire à un centre par un Super Admin. Il ne peut pas être attribué dans le sélecteur générique de rôles. Un compte ne peut gérer qu’un centre ; le retrait de l’affectation le ramène au rôle `CITOYEN`. L’espace `/app/centre-admin` permet ensuite de désigner et d’activer/suspendre les évaluateurs de ce centre.
