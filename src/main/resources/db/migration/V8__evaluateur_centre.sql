<<<<<<< HEAD

ALTER TABLE centre ADD COLUMN administrateur_id CHAR(36) NULL;
ALTER TABLE centre ADD CONSTRAINT fk_centre_administrateur
    FOREIGN KEY (administrateur_id) REFERENCES utilisateur(id);

=======
-- V8__evaluateur_centre.sql

-- 1. Ajout de la colonne administrateur dans la table centre (sans forcer une collation différente de la table utilisateur)
ALTER TABLE centre ADD COLUMN administrateur_id CHAR(36) NULL;
ALTER TABLE centre ADD CONSTRAINT fk_centre_administrateur FOREIGN KEY (administrateur_id) REFERENCES utilisateur(id);

-- 2. Création de la table evaluateur_centre
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
CREATE TABLE evaluateur_centre (
                                   id CHAR(36) NOT NULL,
                                   citoyen_id CHAR(36) NOT NULL,
                                   centre_id CHAR(36) NOT NULL,
                                   designe_par_id CHAR(36) NOT NULL,
<<<<<<< HEAD
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
=======
                                   specialite VARCHAR(200) DEFAULT NULL,
                                   actif BOOLEAN NOT NULL DEFAULT TRUE,
                                   date_designation DATETIME NOT NULL,

                                   CONSTRAINT pk_evaluateur_centre PRIMARY KEY (id),
                                   CONSTRAINT uk_evaluateur_centre UNIQUE (citoyen_id, centre_id),

                                   CONSTRAINT fk_evaluateur_centre_citoyen
                                       FOREIGN KEY (citoyen_id) REFERENCES citoyen(id)
                                           ON UPDATE CASCADE ON DELETE CASCADE,

                                   CONSTRAINT fk_evaluateur_centre_centre
                                       FOREIGN KEY (centre_id) REFERENCES centre(id)
                                           ON UPDATE CASCADE ON DELETE CASCADE,

                                   CONSTRAINT fk_evaluateur_centre_designe_par
                                       FOREIGN KEY (designe_par_id) REFERENCES utilisateur(id)
                                           ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
