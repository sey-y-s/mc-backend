# Plan de portage du backend vers le schéma V1.1

Les migrations ont été **remplacées** (nouveau `V1` + données `V2` à `V4`). Le code Java existant décrit encore l'ancien
modèle : **l'application ne démarre pas** tant que les entités ne sont pas portées (`ddl-auto: validate`). C'est voulu :
mieux vaut un échec bruyant qu'un schéma silencieusement faux.

## Démarrer
1. Créer une **base neuve** (`malicompetences_v1` par défaut). Ne pas réutiliser l'ancienne base : l'historique Flyway ne correspond plus.
2. Variables : `JWT_SECRET` (obligatoire), `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_PROFILE` (`mysql` par défaut, `maria` possible).
3. Aucun test existant n'a jamais touché le vrai schéma (H2 + `create-drop`). Utiliser les garde-fous ci-dessous.

## Garde-fous en place
| Test | Ce qu'il empêche | Base requise |
|---|---|---|
| `MigrationEnumsAlignmentTest` | Un enum Java qui diverge d'un `CHECK` du SQL, un `CHECK` sans enum, le retour du stockage GPS ou des snapshots, `CENTRE_ADMIN` inséré en base | Non |
| `SchemaMigrationTest` | Entité ≠ table réelle (colonne oubliée, mauvais type). **Échoue tant que le portage n'est pas fini** | Oui : `TEST_DB_URL=jdbc:mariadb://localhost:3306/malicompetences_test TEST_DB_USER=root TEST_DB_PASSWORD=root mvn test` (base vide dédiée) |
| `common/regle/*Test`, `ContactCompteTest`, `OtpTest` | Transitions interdites, effet d'une décision, habilitations, portée des besoins, masquage statistique, délais, contact vide, OTP | Non |

## Briques prêtes (à utiliser, pas à réécrire)
`common.statut.*` (enums = vocabulaire du SQL) · `common.regle.Transitions` · `EffetDecisionValidation` · `PolitiqueHabilitation` ·
`PorteeBesoin` · `MasquageStatistique` · `ReglesLocalisation` · `ReglesMiseEnRelation` · `common.validation.ContactCompte` ·
`common.otp.*` (OTP simulé en console, interdit en profil `prod`).

## Ordre de portage
Les phases P1 à P7 sont intégrées dans l'archive de travail. La validation finale reste la compilation, les tests unitaires et `SchemaMigrationTest` sur une base MariaDB/MySQL de test vide.

| Phase | Contenu | Terminé quand |
|---|---|---|
| **P1** | Comptes, OTP, rôles multiples, refresh, audit, sécurité, administration | Inscription → OTP → connexion → `/auth/me` fonctionnent ; super admin créé depuis l'environnement |
| **P2** | Territoires (hiérarchie), catalogue métiers/compétences N:N | Les référentiels V2–V4 se lisent par l'API |
| **P3** | Citoyen (id propre, `identifiant_public`), compétences, expériences N:N, preuves, localisation | Un citoyen se crée, déclare des compétences, dépose une preuve |
| **P4** | **Validation** (demande/décision), évaluateurs/habilitations, centres, tests | Une décision `VALIDEE` met à jour la compétence dans la même transaction (test d'atomicité) |
| **P5** | Organisations, besoins/zones, talents, mise en relation | Un besoin `OUVERT` produit de la demande ; une mise en relation suit les transitions |
| **P6** | Notifications (1 ligne par destinataire), opportunités | Une notification est dédupliquée |
| **P7** | Statistiques calculées à la demande, masquage | Offre/demande/écart calculés avec seuil ; plus de package `skillsGap` |

Les contrôles statiques ont été effectués sur les références des classes historiques ; la compilation et les tests doivent encore être confirmés localement.

## Éléments historiques supprimés
`validation/Validation|StatutValidation|TypeValidation` · `evaluateur/EvaluateurCentre` · `testnumerique/PropositionReponse*` et `ResultatTest*` ·
`auth/ResetPasswordToken*`, `CodeSender`, `LogCodeSender` · `notification/NotificationDestinataire*` · `citoyen/Disponibilite` ·
`competence/EtatCompetence` · `preuve/TypePreuve` · package `portfolio` (garder la logique de stockage de fichiers) · package `skillsGap`.

## Décisions à trancher
1. **Valider les règles proposées** : `Transitions` (tables de transitions) et `EffetDecisionValidation` (effet de REJETEE, COMPLEMENT_REQUIS, ANNULEE ; durée de validité d'une compétence, `app.regles.validite-competence-mois`). Ce sont des propositions testées, pas des faits.
2. **Détecter la commune par GPS** : le schéma ne contient ni coordonnées ni contours de communes. Option recommandée : l'application mobile fait le géocodage inverse et n'envoie que le **code de commune** (le serveur ne voit alors aucune coordonnée).
3. **Numéros étrangers** (diaspora) : `NumeroTelephone` n'accepte que +223 et 8 chiffres.
4. **Communes réelles** : seules les 6 communes de Bamako sont réelles ; les autres sont des données de démonstration rattachées aux régions. Charger le référentiel officiel (cercles et communes).
5. **Opportunités** : les données de démonstration de l'ancien V11 n'ont pas été migrées (elles exigent un auteur `cree_par_utilisateur_id`).
6. **Changement d'API** : la disponibilité est un booléen par compétence ; les rôles sont multiples ; l'id de citoyen est distinct de l'id du compte. Les applications Flutter et Angular doivent utiliser ces contrats V1.1.
7. **Sécurité** : `InsererSuperAdmin` contient un mot de passe en dur. Le considérer comme compromis et le changer partout où il a servi.

## Données de référence
Les identifiants de `V2`–`V4` sont des UUID v5 déterministes (identiques dans tous les environnements). Corrections faites sur les anciens seeds :
questions du test « Irrigation » mal rattachées, propositions de réponse dédoublonnées par texte global, descriptions « compétence de démonstration » retirées.


## État de l'intégration actuelle

- P1 à P5 : portage intégré dans l'archive de travail précédente.
- P6 : notifications persistées une fois par destinataire avec déduplication ; opportunités alignées sur `opportunite` et `categorie_opportunite`.
- P7 : Skills-Gap calculé à la demande ; le package historique `skillsGap` a été supprimé. La demande agrégée utilise `besoin_competence.quantite`, nom réel de la colonne V1.1.
- Nettoyage transversal : suppression des classes historiques ciblées et des méthodes de compatibilité encore utilisées par les anciens DTO ; les DTO des besoins utilisent désormais `dateLimite`.
- Vérification automatisée : les références source aux anciens types ciblés et les marqueurs `TODO-V1.1` ne sont plus présents. La compilation et les tests restent à exécuter localement ; Maven n'est pas disponible/téléchargeable dans cet environnement.
