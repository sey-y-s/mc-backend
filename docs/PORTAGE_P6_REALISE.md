# Portage P6 — Notifications et opportunités

## Notifications
- Une ligne de `notification` représente un destinataire ; le modèle `NotificationDestinataire` n'est plus utilisé.
- La consultation et le marquage comme lu sont limités au propriétaire, sauf pour ADMIN/SUPER_ADMIN.
- La déduplication est vérifiée par couple utilisateur/clé et renforcée par la contrainte unique SQL.
- `appareil_notification` est mappée ; l'envoi push reste hors périmètre de cette phase.

## Opportunités
- Entités, DTO, mapper, repository, service et contrôleur alignés sur `categorie_opportunite` et `opportunite`.
- Les routes publiques ne retournent que les opportunités publiées et non expirées.
- La création prend l'auteur depuis l'utilisateur authentifié ; la gestion des annonces est réservée à SUPER_ADMIN.
- Correction du tri JPQL : la propriété est `publieeLe` (colonne `publiee_le`), pas `datePublication`.

## Validation
- Test de propriété/déduplication adapté au modèle à une table.
- La compilation et les tests ne sont pas exécutés dans cet environnement ; ils restent à confirmer dans l'environnement de développement.
