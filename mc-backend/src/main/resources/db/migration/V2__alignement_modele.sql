ALTER TABLE citoyen
    ADD COLUMN commune_id CHAR(36) NULL;

ALTER TABLE citoyen
    ADD CONSTRAINT fk_citoyen_commune
        FOREIGN KEY (commune_id) REFERENCES commune(id);

CREATE INDEX idx_citoyen_commune ON citoyen(commune_id);

ALTER TABLE suivi_talent RENAME TO suivi_besoin_talent;

ALTER TABLE demande_mise_en_relation
    DROP FOREIGN KEY fk_demande_relation_suivi;

ALTER TABLE demande_mise_en_relation
    RENAME COLUMN suivi_talent_id TO suivi_besoin_talent_id;

ALTER TABLE demande_mise_en_relation
    ADD CONSTRAINT fk_demande_relation_suivi_besoin_talent
        FOREIGN KEY (suivi_besoin_talent_id) REFERENCES suivi_besoin_talent(id);
