# Portage P7 — Skills-Gap calculé à la demande

- Les indicateurs sont calculés à partir des compétences validées et non expirées, des besoins ouverts non expirés et des zones associées.
- Le masquage statistique est appliqué avec seuil minimal et masquage complémentaire.
- L'API expose les résultats agrégés, les critères de calcul et les limites de couverture ; les exports CSV neutralisent les formules.
- Le package historique `skillsGap` et son test lié au stockage d'indicateurs ont été supprimés/remplacés.
- Le calcul de la demande utilise la colonne réelle `besoin_competence.quantite`.
- L'espace reste désactivé par défaut et requiert les deux paramètres de configuration de confidentialité.

La compilation et les tests d'intégration SQL restent à exécuter localement.
