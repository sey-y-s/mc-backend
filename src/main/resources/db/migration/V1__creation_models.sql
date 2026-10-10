CREATE TABLE utilisateur (
                             id CHAR(36) NOT NULL,
                             telephone VARCHAR(30) NOT NULL,
                             email VARCHAR(255),
                             password_hash VARCHAR(255) NOT NULL,
                             actif BOOLEAN NOT NULL DEFAULT TRUE,
                             date_creation DATETIME NOT NULL,
                             role VARCHAR(30) NOT NULL,

                             CONSTRAINT pk_utilisateur PRIMARY KEY (id),
                             CONSTRAINT uk_utilisateur_telephone UNIQUE (telephone),
                             CONSTRAINT uk_utilisateur_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE citoyen (
                         id CHAR(36) NOT NULL,
                         nom VARCHAR(100) NOT NULL,
                         prenom VARCHAR(100) NOT NULL,
                         sexe VARCHAR(20),
                         photo_url VARCHAR(500),
                         disponibilite VARCHAR(30) NOT NULL,
                         code_passeport CHAR(14) NOT NULL,

                         CONSTRAINT uk_citoyen_code_passeport UNIQUE (code_passeport),
                         CONSTRAINT pk_citoyen PRIMARY KEY (id),
                         CONSTRAINT fk_citoyen_utilisateur
                             FOREIGN KEY (id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE organisation (
                              id CHAR(36) NOT NULL,
                              nom VARCHAR(150) NOT NULL,
                              description TEXT,
                              statut VARCHAR(30) NOT NULL,

                              CONSTRAINT pk_organisation PRIMARY KEY (id),
                              CONSTRAINT fk_organisation_utilisateur
                                  FOREIGN KEY (id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE centre (
                        id CHAR(36) NOT NULL,
                        nom VARCHAR(150) NOT NULL,
                        description TEXT,
                        adresse VARCHAR(255),
                        telephone VARCHAR(30),

                        CONSTRAINT pk_centre PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE region (
                        id CHAR(36) NOT NULL,
                        nom VARCHAR(100) NOT NULL,

                        CONSTRAINT pk_region PRIMARY KEY (id),
                        CONSTRAINT uk_region_nom UNIQUE (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE commune (
                         id CHAR(36) NOT NULL,
                         region_id CHAR(36) NOT NULL,
                         nom VARCHAR(100) NOT NULL,

                         CONSTRAINT pk_commune PRIMARY KEY (id),
                         CONSTRAINT fk_commune_region
                             FOREIGN KEY (region_id) REFERENCES region(id),
                         CONSTRAINT uk_commune_region_nom UNIQUE (region_id, nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE secteur_activite (
                                  id CHAR(36) NOT NULL,
                                  nom VARCHAR(150) NOT NULL,
                                  description TEXT,

                                  CONSTRAINT pk_secteur_activite PRIMARY KEY (id),
                                  CONSTRAINT uk_secteur_activite_nom UNIQUE (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE metier (
                        id CHAR(36) NOT NULL,
                        secteur_activite_id CHAR(36) NOT NULL,
                        nom VARCHAR(150) NOT NULL,
                        description TEXT,

                        CONSTRAINT pk_metier PRIMARY KEY (id),
                        CONSTRAINT fk_metier_secteur
                            FOREIGN KEY (secteur_activite_id)
                                REFERENCES secteur_activite(id),
                        CONSTRAINT uk_metier_secteur_nom
                            UNIQUE (secteur_activite_id, nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE competence (
                            id CHAR(36) NOT NULL,
                            metier_id CHAR(36) NOT NULL,
                            nom VARCHAR(200) NOT NULL,
                            description TEXT,

                            CONSTRAINT pk_competence PRIMARY KEY (id),
                            CONSTRAINT fk_competence_metier
                                FOREIGN KEY (metier_id)
                                    REFERENCES metier(id),
                            CONSTRAINT uk_competence_metier_nom
                                UNIQUE (metier_id, nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE citoyen_competence (
                                    id CHAR(36) NOT NULL,
                                    citoyen_id CHAR(36) NOT NULL,
                                    competence_id CHAR(36) NOT NULL,
                                    niveau VARCHAR(30) NOT NULL,
                                    etat VARCHAR(30) NOT NULL,
                                    date_declaration DATETIME NOT NULL,

                                    CONSTRAINT pk_citoyen_competence PRIMARY KEY (id),

                                    CONSTRAINT fk_citoyen_competence_citoyen
                                        FOREIGN KEY (citoyen_id)
                                            REFERENCES citoyen(id),

                                    CONSTRAINT fk_citoyen_competence_competence
                                        FOREIGN KEY (competence_id)
                                            REFERENCES competence(id),

                                    CONSTRAINT uk_citoyen_competence
                                        UNIQUE (citoyen_id, competence_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE experience (
                            id CHAR(36) NOT NULL,
                            citoyen_id CHAR(36) NOT NULL,
                            titre VARCHAR(200) NOT NULL,
                            description TEXT,
                            entreprise VARCHAR(200),
                            date_debut DATE NOT NULL,
                            date_fin DATE,
                            en_cours BOOLEAN NOT NULL DEFAULT FALSE,

                            CONSTRAINT pk_experience PRIMARY KEY (id),

                            CONSTRAINT fk_experience_citoyen
                                FOREIGN KEY (citoyen_id)
                                    REFERENCES citoyen(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE experience_competence (
                                       experience_id CHAR(36) NOT NULL,
                                       citoyen_competence_id CHAR(36) NOT NULL,

                                       CONSTRAINT pk_experience_competence
                                           PRIMARY KEY (experience_id, citoyen_competence_id),

                                       CONSTRAINT fk_experience_competence_experience
                                           FOREIGN KEY (experience_id)
                                               REFERENCES experience(id),

                                       CONSTRAINT fk_experience_competence_citoyen_competence
                                           FOREIGN KEY (citoyen_competence_id)
                                               REFERENCES citoyen_competence(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE portfolio_realisation (
    id CHAR(36) NOT NULL,
    citoyen_id CHAR(36) NOT NULL,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    date_realisation DATE,
    lien_url VARCHAR(500),

    CONSTRAINT pk_portfolio_realisation PRIMARY KEY (id),

    CONSTRAINT fk_portfolio_realisation_citoyen
        FOREIGN KEY (citoyen_id)
        REFERENCES citoyen(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE media_realisation (
    id CHAR(36) NOT NULL,
    portfolio_realisation_id CHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    url VARCHAR(500) NOT NULL,
    nom_fichier VARCHAR(255),

    CONSTRAINT pk_media_realisation PRIMARY KEY (id),

    CONSTRAINT fk_media_realisation_portfolio
        FOREIGN KEY (portfolio_realisation_id)
        REFERENCES portfolio_realisation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE preuve (
                        id CHAR(36) NOT NULL,
                        citoyen_competence_id CHAR(36) NOT NULL,
                        type VARCHAR(40) NOT NULL,
                        titre VARCHAR(200) NOT NULL,
                        description TEXT,
                        fichier_url VARCHAR(500),
                        date_depot DATETIME NOT NULL,

                        CONSTRAINT pk_preuve PRIMARY KEY (id),

                        CONSTRAINT fk_preuve_citoyen_competence
                            FOREIGN KEY (citoyen_competence_id)
                                REFERENCES citoyen_competence(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE validation (
                            id CHAR(36) NOT NULL,
                            citoyen_competence_id CHAR(36) NOT NULL,
                            preuve_id CHAR(36),
                            validee_par_id CHAR(36) NOT NULL,
                            centre_id CHAR(36),
                            type VARCHAR(40) NOT NULL,
                            statut VARCHAR(30) NOT NULL,
                            date_validation DATETIME,
                            commentaire TEXT,
                            date_expiration DATE,

                            CONSTRAINT pk_validation PRIMARY KEY (id),

                            CONSTRAINT fk_validation_citoyen_competence
                                FOREIGN KEY (citoyen_competence_id)
                                    REFERENCES citoyen_competence(id),

                            CONSTRAINT fk_validation_preuve
                                FOREIGN KEY (preuve_id)
                                    REFERENCES preuve(id),

                            CONSTRAINT fk_validation_utilisateur
                                FOREIGN KEY (validee_par_id)
                                    REFERENCES utilisateur(id),

                            CONSTRAINT fk_validation_centre
                                FOREIGN KEY (centre_id)
                                    REFERENCES centre(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE besoin (
                        id CHAR(36) NOT NULL,
                        organisation_id CHAR(36) NOT NULL,
                        titre VARCHAR(200) NOT NULL,
                        description TEXT,
                        statut VARCHAR(30) NOT NULL,
                        date_debut DATE,
                        date_fin DATE,
                        date_creation DATETIME NOT NULL,

                        CONSTRAINT pk_besoin PRIMARY KEY (id),

                        CONSTRAINT fk_besoin_organisation
                            FOREIGN KEY (organisation_id)
                                REFERENCES organisation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE besoin_competence (
                                   id CHAR(36) NOT NULL,
                                   besoin_id CHAR(36) NOT NULL,
                                   competence_id CHAR(36) NOT NULL,
                                   niveau_minimum VARCHAR(30) NOT NULL,
                                   quantite INT NOT NULL,

                                   CONSTRAINT pk_besoin_competence PRIMARY KEY (id),

                                   CONSTRAINT fk_besoin_competence_besoin
                                       FOREIGN KEY (besoin_id)
                                           REFERENCES besoin(id),

                                   CONSTRAINT fk_besoin_competence_competence
                                       FOREIGN KEY (competence_id)
                                           REFERENCES competence(id),

                                   CONSTRAINT uk_besoin_competence
                                       UNIQUE (besoin_id, competence_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE suivi_talent (
                              id CHAR(36) NOT NULL,
                              organisation_id CHAR(36) NOT NULL,
                              besoin_id CHAR(36) NOT NULL,
                              citoyen_id CHAR(36) NOT NULL,
                              statut VARCHAR(30) NOT NULL,
                              score_correspondance DECIMAL(5,2),
                              details_correspondance JSON,
                              note_interne TEXT,
                              date_creation DATETIME NOT NULL,
                              date_mise_a_jour DATETIME NOT NULL,

                              CONSTRAINT pk_suivi_talent PRIMARY KEY (id),

                              CONSTRAINT fk_suivi_talent_organisation
                                  FOREIGN KEY (organisation_id)
                                      REFERENCES organisation(id),

                              CONSTRAINT fk_suivi_talent_besoin
                                  FOREIGN KEY (besoin_id)
                                      REFERENCES besoin(id),

                              CONSTRAINT fk_suivi_talent_citoyen
                                  FOREIGN KEY (citoyen_id)
                                      REFERENCES citoyen(id),

                              CONSTRAINT uk_suivi_talent_besoin_citoyen
                                  UNIQUE (besoin_id, citoyen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE demande_mise_en_relation (
                                          id CHAR(36) NOT NULL,
                                          demandeur_id CHAR(36) NOT NULL,
                                          destinataire_id CHAR(36) NOT NULL,
                                          suivi_talent_id CHAR(36),
                                          statut VARCHAR(30) NOT NULL,
                                          message TEXT,
                                          date_demande DATETIME NOT NULL,
                                          date_reponse DATETIME,

                                          CONSTRAINT pk_demande_mise_en_relation PRIMARY KEY (id),

                                          CONSTRAINT fk_demande_relation_demandeur
                                              FOREIGN KEY (demandeur_id)
                                                  REFERENCES utilisateur(id),

                                          CONSTRAINT fk_demande_relation_destinataire
                                              FOREIGN KEY (destinataire_id)
                                                  REFERENCES citoyen(id),

                                          CONSTRAINT fk_demande_relation_suivi
                                              FOREIGN KEY (suivi_talent_id)
                                                  REFERENCES suivi_talent(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE consultation_profil (
                                     id CHAR(36) NOT NULL,
                                     utilisateur_id CHAR(36) NOT NULL,
                                     citoyen_id CHAR(36) NOT NULL,
                                     date_consultation DATETIME NOT NULL,

                                     CONSTRAINT pk_consultation_profil PRIMARY KEY (id),

                                     CONSTRAINT fk_consultation_profil_utilisateur
                                         FOREIGN KEY (utilisateur_id)
                                             REFERENCES utilisateur(id),

                                     CONSTRAINT fk_consultation_profil_citoyen
                                         FOREIGN KEY (citoyen_id)
                                             REFERENCES citoyen(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification (
                              id CHAR(36) NOT NULL,
                              titre VARCHAR(200) NOT NULL,
                              message TEXT NOT NULL,
                              type VARCHAR(40) NOT NULL,
                              date_creation DATETIME NOT NULL,

                              CONSTRAINT pk_notification PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_destinataire (
                                           id CHAR(36) NOT NULL,
                                           notification_id CHAR(36) NOT NULL,
                                           utilisateur_id CHAR(36) NOT NULL,
                                           lu BOOLEAN NOT NULL DEFAULT FALSE,
                                           date_reception DATETIME NOT NULL,
                                           date_lecture DATETIME,

                                           CONSTRAINT pk_notification_destinataire PRIMARY KEY (id),

                                           CONSTRAINT fk_notification_destinataire_notification
                                               FOREIGN KEY (notification_id)
                                                   REFERENCES notification(id),

                                           CONSTRAINT fk_notification_destinataire_utilisateur
                                               FOREIGN KEY (utilisateur_id)
                                                   REFERENCES utilisateur(id),

                                           CONSTRAINT uk_notification_destinataire
                                               UNIQUE (notification_id, utilisateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categorie_opportunite (
                                       id CHAR(36) NOT NULL,
                                       nom VARCHAR(150) NOT NULL,
                                       description TEXT,

                                       CONSTRAINT pk_categorie_opportunite PRIMARY KEY (id),

                                       CONSTRAINT uk_categorie_opportunite_nom
                                           UNIQUE (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE opportunite (
                             id CHAR(36) NOT NULL,
                             categorie_id CHAR(36) NOT NULL,
                             titre VARCHAR(200) NOT NULL,
                             description TEXT NOT NULL,
                             statut VARCHAR(30) NOT NULL,
                             type VARCHAR(40) NOT NULL,
                             date_publication DATETIME,
                             date_expiration DATE,

                             CONSTRAINT pk_opportunite PRIMARY KEY (id),

                             CONSTRAINT fk_opportunite_categorie
                                 FOREIGN KEY (categorie_id)
                                     REFERENCES categorie_opportunite(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE indicateur_skills_gap (
                                       id CHAR(36) NOT NULL,
                                       region_id CHAR(36) NOT NULL,
                                       competence_id CHAR(36) NOT NULL,
                                       periode_debut DATE NOT NULL,
                                       periode_fin DATE NOT NULL,
                                       date_calcul DATETIME NOT NULL,
                                       demande_estimee INT NOT NULL,
                                       offre_disponible INT NOT NULL,
                                       ecart INT NOT NULL,

                                       CONSTRAINT pk_indicateur_skills_gap PRIMARY KEY (id),

                                       CONSTRAINT fk_skills_gap_region
                                           FOREIGN KEY (region_id)
                                               REFERENCES region(id),

                                       CONSTRAINT fk_skills_gap_competence
                                           FOREIGN KEY (competence_id)
                                               REFERENCES competence(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;