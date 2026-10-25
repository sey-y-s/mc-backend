# Audit des références de l'ancien modèle avant build

## Suppressions et remplacements

- Ancien modèle de validation (`Validation`, `StatutValidation`, `TypeValidation`) : remplacé par les demandes et décisions V1.1.
- Affectation historique `EvaluateurCentre` : remplacée par les habilitations.
- Notifications par association (`NotificationDestinataire`) : remplacées par une notification par utilisateur.
- Anciennes entités/références ciblées supprimées : `Disponibilite`, `EtatCompetence`, `ResetPasswordToken*`, `CodeSender`, `LogCodeSender`, `PropositionReponse*`, `ResultatTest*`, ainsi que les packages `skillsGap` et `portfolio` lorsqu'ils étaient présents.
- Suppression des adaptateurs de compatibilité du modèle utilisateur, du territoire, de la compétence, de la compétence citoyen et des besoins.
- Les besoins exposent désormais `dateLimite` au lieu des anciennes dates de début/fin.
- Le centre mappe les colonnes V1.1 `email`, `commune_id`, `statut` et `cree_le`; la création exige une commune active.

## Contrôles statiques réalisés

- Aucun fichier source portant les noms des anciennes classes ciblées n'est resté dans `src/main/java`.
- Aucune référence Java aux anciennes classes ciblées n'a été trouvée dans `src/main/java` et `src/test/java`.
- Aucun marqueur `TODO-V1.1` ne reste dans les sources Java.
- Vérification statique approximative des colonnes JPA contre les migrations Flyway : aucune colonne manquante détectée hors relations inverses `mappedBy`.

## Limite

Ces contrôles ne remplacent pas `mvn test` ni `SchemaMigrationTest`. Le wrapper Maven n'a pas pu télécharger Maven dans cet environnement ; la compilation n'est donc pas certifiée. La validation finale doit se faire sur une base de test MariaDB/MySQL vide.
