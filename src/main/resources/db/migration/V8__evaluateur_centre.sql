
ALTER TABLE centre ADD COLUMN administrateur_id CHAR(36) NULL;
ALTER TABLE centre ADD CONSTRAINT fk_centre_administrateur
    FOREIGN KEY (administrateur_id) REFERENCES utilisateur(id);

CREATE TABLE evaluateur_centre (
                                   id CHAR(36) NOT NULL,
                                   citoyen_id CHAR(36) NOT NULL,
                                   centre_id CHAR(36) NOT NULL,
                                   designe_par_id CHAR(36) NOT NULL,
                                   specialite VARCHAR(200),
                                   actif BOOLEAN NOT NULL DEFAULT TRUE,
                                   date_designation DATETIME NOT NULL,
                                   CONSTRAINT pk_evaluateur_centre PRIMARY KEY (id),
                                   CONSTRAINT uk_evaluateur_centre UNIQUE (citoyen_id, centre_id),
                                   CONSTRAINT fk_evaluateur_centre_citoyen
                                       FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
                                   CONSTRAINT fk_evaluateur_centre_centre
                                       FOREIGN KEY (centre_id) REFERENCES centre(id),
                                   CONSTRAINT fk_evaluateur_centre_designe_par
                                       FOREIGN KEY (designe_par_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB;
