# Étape 2 — Sécurisation OTP et mots de passe

## Changements

- `verifyOtp` n'accepte que `INSCRIPTION` (`OTP_USAGE_INVALIDE` sinon) et exige que le compte associé soit `EN_ATTENTE` avant de le passer à `ACTIF`.
- Les contrôles de politique OTP utilisent le compteur avant incrémentation. Le compteur est ensuite incrémenté et sauvegardé avant la comparaison du code ; `noRollbackFor = IllegalArgumentException.class` conserve les tentatives après un code erroné.
- `resendOtp` n'envoie un code que pour un compte existant et le statut attendu par l'usage. Les usages non pris en charge, comptes absents, statuts incorrects et délais de renvoi non écoulés retournent silencieusement.
- `demanderReinitialisation` utilise la normalisation commune à la connexion, retourne silencieusement si le compte n'est pas actif, est inconnu ou si le délai de renvoi n'est pas écoulé. `POST /forgot-password` répond toujours `202 Accepted`.
- Ajout de `identifiantNormalise(String)` pour la normalisation commune : email en minuscules, téléphone via `NumeroTelephone.normaliser` avec repli sur la saisie nettoyée.
- `PUT /auth/mot-de-passe` autorise tout compte authentifié ; l'identifiant est exclusivement lu depuis le jeton. `ChangementMotDePasseRequest` ne contient plus `utilisateurId`.
- `ResetPasswordRequest.nouveauMotDePasse` accepte les anciens noms JSON `motDePasse` et `password` via `@JsonAlias`.
- Les autres routes et structures de réponse n'ont pas été modifiées. `PolitiqueOtp` et `SecurityConfig` sont inchangés.

## Vérifications

- Relecture statique des méthodes d'authentification et recherche des accès à `request.utilisateurId()` / `request.motDePasse()` dans le package `auth` : aucune référence obsolète trouvée.
- Compilation et tests non exécutés dans cet environnement : Maven n'est pas installé et le wrapper Maven n'est pas fourni dans l'archive.
