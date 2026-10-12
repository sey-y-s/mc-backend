-- Données de départ : régions du Mali et communes du District de Bamako.
-- Insertion idempotente : une ligne déjà présente (même nom) n'est pas dupliquée.
-- Les autres communes peuvent être ajoutées via l'espace d'administration (POST /api/v1/communes).

INSERT INTO region (id, nom)
SELECT UUID(), 'Kayes' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Kayes');

INSERT INTO region (id, nom)
SELECT UUID(), 'Koulikoro' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Koulikoro');

INSERT INTO region (id, nom)
SELECT UUID(), 'Sikasso' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Sikasso');

INSERT INTO region (id, nom)
SELECT UUID(), 'Ségou' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Ségou');

INSERT INTO region (id, nom)
SELECT UUID(), 'Mopti' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Mopti');

INSERT INTO region (id, nom)
SELECT UUID(), 'Tombouctou' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Tombouctou');

INSERT INTO region (id, nom)
SELECT UUID(), 'Gao' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Gao');

INSERT INTO region (id, nom)
SELECT UUID(), 'Kidal' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Kidal');

INSERT INTO region (id, nom)
SELECT UUID(), 'Taoudénit' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Taoudénit');

INSERT INTO region (id, nom)
SELECT UUID(), 'Ménaka' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'Ménaka');

INSERT INTO region (id, nom)
SELECT UUID(), 'District de Bamako' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM region WHERE nom = 'District de Bamako');

INSERT INTO commune (id, region_id, nom)
SELECT UUID(), r.id, 'Commune I' FROM region r
WHERE r.nom = 'District de Bamako'
  AND NOT EXISTS (SELECT 1 FROM commune c WHERE c.region_id = r.id AND c.nom = 'Commune I');

INSERT INTO commune (id, region_id, nom)
SELECT UUID(), r.id, 'Commune II' FROM region r
WHERE r.nom = 'District de Bamako'
  AND NOT EXISTS (SELECT 1 FROM commune c WHERE c.region_id = r.id AND c.nom = 'Commune II');

INSERT INTO commune (id, region_id, nom)
SELECT UUID(), r.id, 'Commune III' FROM region r
WHERE r.nom = 'District de Bamako'
  AND NOT EXISTS (SELECT 1 FROM commune c WHERE c.region_id = r.id AND c.nom = 'Commune III');

INSERT INTO commune (id, region_id, nom)
SELECT UUID(), r.id, 'Commune IV' FROM region r
WHERE r.nom = 'District de Bamako'
  AND NOT EXISTS (SELECT 1 FROM commune c WHERE c.region_id = r.id AND c.nom = 'Commune IV');

INSERT INTO commune (id, region_id, nom)
SELECT UUID(), r.id, 'Commune V' FROM region r
WHERE r.nom = 'District de Bamako'
  AND NOT EXISTS (SELECT 1 FROM commune c WHERE c.region_id = r.id AND c.nom = 'Commune V');

INSERT INTO commune (id, region_id, nom)
SELECT UUID(), r.id, 'Commune VI' FROM region r
WHERE r.nom = 'District de Bamako'
  AND NOT EXISTS (SELECT 1 FROM commune c WHERE c.region_id = r.id AND c.nom = 'Commune VI');

