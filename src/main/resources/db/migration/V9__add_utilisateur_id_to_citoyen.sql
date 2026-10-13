-- V9__add_utilisateur_id_to_citoyen.sql
ALTER TABLE citoyen ADD COLUMN utilisateur_id VARCHAR(36) NOT NULL;

ALTER TABLE citoyen
    ADD CONSTRAINT fk_citoyen_utilisateur_v9
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id);