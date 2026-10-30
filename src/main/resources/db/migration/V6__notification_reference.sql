-- Une notification peut pointer vers l'objet concerné (demande de mise en relation, validation...).
ALTER TABLE notification ADD COLUMN reference_id char(36) DEFAULT NULL;
