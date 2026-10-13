ALTER TABLE validation
    ADD COLUMN date_demande DATETIME NULL;

UPDATE validation
SET date_demande = COALESCE(date_validation, CURRENT_TIMESTAMP)
WHERE date_demande IS NULL;

ALTER TABLE validation
    MODIFY COLUMN date_demande DATETIME NOT NULL;
