# Portage P5 — organisations, besoins, talents et mise en relation

## Changements intégrés

- Les profils organisation sont recherchés par `utilisateur_id` pour les routes `/me` et le tableau de bord. L'identifiant du compte n'est plus confondu avec l'identifiant propre de l'organisation.
- La création d'un profil organisation démarre en `EN_ATTENTE`. Les changements de statut passent par `Transitions.ORGANISATION` et l'API d'administration exige un motif pour un rejet ou une suspension. Une organisation rejetée repasse en attente lorsqu'elle modifie et redépose son profil.
- Les justificatifs d'organisation sont déposables et consultables par leur propriétaire. Les administrateurs peuvent les examiner. La clé de stockage privé n'est jamais renvoyée dans les réponses API. Un justificatif déposé n'approuve pas automatiquement l'organisation.
- Les besoins sont rattachés à leur organisation propriétaire, exigent au moins une compétence et respectent `Transitions.BESOIN`. La date de fin historique est mappée sur `date_limite`, seule date portée par le schéma V1.1. Un besoin ouvert dont la date limite est passée ne peut plus être utilisé pour le matching ou une nouvelle mise en relation.
- Les zones géographiques d'un besoin sont gérées par `PUT/GET /api/v1/besoins/{id}/zones`. Chaque ligne contient une commune ou un territoire, jamais les deux. Une liste vide correspond à une portée nationale. Les zones ne peuvent être modifiées que tant que le besoin est un brouillon.
- La recherche de talents et le matching ne renvoient que les profils publics. Les noms, contacts et identifiants internes ne sont pas exposés. Le matching ne compte que les compétences validées et non expirées ; seules les preuves visibles contribuent aux informations affichées.
- Les consultations de profils sont enregistrées côté serveur avec l'organisation et, facultativement, le besoin. Il n'existe plus de route qui restitue l'historique de consultation au talent.
- La mise en relation est réservée aux organisations approuvées. Le service applique le délai de 30 jours après un refus, les transitions de demande/suivi et l'unicité des demandes en attente. La réponse du citoyen est exposée par `PATCH /{id}/reponse`; l'identité du citoyen n'est révélée dans la réponse qu'après acceptation. L'annulation conserve l'historique.
- Le retrait d'un besoin via l'ancienne route DELETE l'annule désormais au lieu de supprimer son historique.

## Routes P5 ajoutées ou consolidées

- `PATCH /api/v1/organisations/{id}/statut`
- `GET /api/v1/organisations/me/justificatifs`
- `POST /api/v1/organisations/me/justificatifs`
- `GET /api/v1/organisations/admin/{organisationId}/justificatifs`
- `PATCH /api/v1/organisations/admin/justificatifs/{id}`
- `GET /api/v1/besoins/{besoinId}/zones`
- `PUT /api/v1/besoins/{besoinId}/zones`
- `PATCH /api/v1/demandes-mise-en-relation/{id}/reponse`
- `PATCH /api/v1/demandes-mise-en-relation/{id}/message`

## Points à vérifier dans l'environnement de développement

La compilation n'a pas pu être lancée ici : le Maven Wrapper n'a pas réussi à télécharger Maven depuis Maven Central et Maven n'est pas installé dans l'environnement. Il reste à lancer la compilation, les tests existants et `SchemaMigrationTest` sur une base de test neuve. Vérifier aussi les contrats API consommés par Angular/Flutter, notamment la date limite des besoins et les nouvelles routes de zones et de justificatifs.

Le mapping `besoin_competence` suit le SQL effectivement présent dans `V1__schema_initial.sql` (`quantite`, clé primaire `id` et unicité `(besoin_id, competence_id)`). Le rapport ne renomme pas ces colonnes sans migration SQL correspondante.
