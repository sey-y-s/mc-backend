# Migrations de base de données (Flyway)

| Fichier | Contenu |
|---|---|
| `V1__schema_initial.sql` | Tout le schéma (43 tables), dans l'ordre des dépendances |
| `V2__referentiel_territoires.sql` | Régions et communes |
| `V3__referentiel_competences.sql` | Secteurs, métiers, compétences |
| `V4__referentiel_tests_numeriques.sql` | Tests, questions, propositions |
| `V5__referentiel_categories_opportunites.sql` | Catégories d'opportunités |
| `db/dev-data/V100__donnees_demonstration.sql` | Opportunités et indicateurs d'exemple (profil `dev` seulement) |

## Règles

- **Base neuve obligatoire** : l'historique a été remplacé. Supprimer l'ancienne base avant le premier démarrage.
- **Ne jamais modifier un fichier déjà appliqué** (Flyway contrôle les empreintes). Toute évolution = un nouveau fichier
  `V6__description.sql`, `V7__…`, dans l'ordre.
- Une migration = une intention (ajout de table, de colonne, de données de référence…).
- Les données de démonstration restent hors de `db/migration` : elles ne doivent jamais partir en production.
- `ddl-auto: validate` : Hibernate vérifie que les entités correspondent au schéma. Une entité modifiée sans migration
  empêche le démarrage.

## Démarrage

```bash
# production / base propre
java -jar app.jar --spring.profiles.active=prod,mysql

# développement avec données de démonstration
java -jar app.jar --spring.profiles.active=dev,mysql
```

Les identifiants des données de référence sont fixes : ils sont identiques dans tous les environnements.
