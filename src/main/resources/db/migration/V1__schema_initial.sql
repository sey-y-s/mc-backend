-- MaliCompetences V1.1 - schema initial.
-- Compatible MySQL 8.0.16+ / MariaDB 10.5+ (CHECK reellement appliques).
-- A executer UNIQUEMENT sur une base vide, creee separement (ex. malicompetences).
-- Utilisable tel quel comme migration Flyway V1__schema_initial.sql (encodage UTF-8).
-- UUID en CHAR(36), generes par l'application (UUID() uniquement pour les donnees de base ci-dessous).
-- Fichiers : seule une cle de stockage PRIVE est conservee, jamais une URL publique.
-- Telephones : stockes au format international normalise (ex. +22370000000).

-- ============================================================
-- 1. COMPTES ET SECURITE
-- ============================================================

CREATE TABLE utilisateur (
  id CHAR(36) PRIMARY KEY,
  telephone VARCHAR(30) NULL UNIQUE,
  email VARCHAR(190) NULL UNIQUE,
  mot_de_passe_hash VARCHAR(255) NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
  telephone_verifie_le DATETIME(6) NULL,
  email_verifie_le DATETIME(6) NULL,
  conditions_acceptees_le DATETIME(6) NULL,
  cree_par_utilisateur_id CHAR(36) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  modifie_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  FOREIGN KEY (cree_par_utilisateur_id) REFERENCES utilisateur(id),
  CONSTRAINT ck_utilisateur_statut CHECK (statut IN ('EN_ATTENTE','ACTIF','SUSPENDU','FERME')),
  CONSTRAINT ck_utilisateur_contact CHECK (telephone IS NOT NULL OR email IS NOT NULL)
) ENGINE=InnoDB;

CREATE TABLE role (
  id CHAR(36) PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  libelle VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE utilisateur_role (
  utilisateur_id CHAR(36) NOT NULL,
  role_id CHAR(36) NOT NULL,
  attribue_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (utilisateur_id, role_id),
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB;

CREATE TABLE preference_utilisateur (
  utilisateur_id CHAR(36) PRIMARY KEY,
  mode_economie_donnees BOOLEAN NOT NULL DEFAULT FALSE,
  notifications_actives BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB;

-- OTP par telephone (SMS) ou email. Le code est genere par le backend ; tant qu'aucun
-- service d'envoi n'est branche, il est simplement affiche dans la console du serveur.
CREATE TABLE code_verification (
  id CHAR(36) PRIMARY KEY,
  utilisateur_id CHAR(36) NULL,
  canal VARCHAR(15) NOT NULL,
  destinataire VARCHAR(190) NOT NULL,
  usage_code VARCHAR(40) NOT NULL,
  code_hash VARCHAR(255) NOT NULL,
  expire_le DATETIME(6) NOT NULL,
  utilise_le DATETIME(6) NULL,
  tentatives INT NOT NULL DEFAULT 0,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_code_verif_dest_usage (destinataire, usage_code, expire_le),
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  CONSTRAINT ck_code_verif_canal CHECK (canal IN ('TELEPHONE','EMAIL')),
  CONSTRAINT ck_code_verif_usage CHECK (usage_code IN ('INSCRIPTION','RECUPERATION_MOT_DE_PASSE','CHANGEMENT_CONTACT'))
) ENGINE=InnoDB;

CREATE TABLE session_renouvellement (
  id CHAR(36) PRIMARY KEY,
  utilisateur_id CHAR(36) NOT NULL,
  jeton_hash VARCHAR(255) NOT NULL UNIQUE,
  expire_le DATETIME(6) NOT NULL,
  revoque_le DATETIME(6) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  INDEX idx_session_utilisateur_expiration (utilisateur_id, expire_le)
) ENGINE=InnoDB;

CREATE TABLE journal_audit (
  id CHAR(36) PRIMARY KEY,
  acteur_id CHAR(36) NULL,
  action VARCHAR(100) NOT NULL,
  type_ressource VARCHAR(80) NOT NULL,
  ressource_id CHAR(36) NULL,
  resultat VARCHAR(24) NULL,
  details_json JSON NULL,
  adresse_ip VARCHAR(45) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_audit_ressource (type_ressource, ressource_id, cree_le),
  INDEX idx_audit_acteur (acteur_id, cree_le),
  FOREIGN KEY (acteur_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB;

-- ============================================================
-- 2. GEOGRAPHIE ET REFERENTIELS
-- ============================================================

-- Hierarchie : REGION et DISTRICT (sans parent) ; CERCLE rattache a une region.
-- Une commune se rattache au niveau le plus fin disponible : CERCLE ou DISTRICT (Bamako) ;
-- provisoirement REGION tant que les cercles ne sont pas charges. Le backend verifie qu'un
-- cercle a une region pour parent et refuse toute autre incoherence hierarchique.
CREATE TABLE territoire (
  id CHAR(36) PRIMARY KEY,
  parent_id CHAR(36) NULL,
  code VARCHAR(30) NOT NULL UNIQUE,
  nom VARCHAR(120) NOT NULL,
  niveau VARCHAR(15) NOT NULL DEFAULT 'REGION',
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (parent_id) REFERENCES territoire(id),
  INDEX idx_territoire_parent (parent_id),
  CONSTRAINT ck_territoire_niveau CHECK (
    (niveau IN ('REGION','DISTRICT') AND parent_id IS NULL)
    OR (niveau = 'CERCLE' AND parent_id IS NOT NULL)
  )
) ENGINE=InnoDB;

CREATE TABLE commune (
  id CHAR(36) PRIMARY KEY,
  territoire_id CHAR(36) NOT NULL,
  code VARCHAR(30) NOT NULL UNIQUE,
  nom VARCHAR(120) NOT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (territoire_id) REFERENCES territoire(id),
  INDEX idx_commune_territoire (territoire_id)
) ENGINE=InnoDB;

CREATE TABLE secteur_activite (
  id CHAR(36) PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  nom VARCHAR(160) NOT NULL,
  description TEXT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE source_referentiel (
  id CHAR(36) PRIMARY KEY,
  organisme_source VARCHAR(180) NOT NULL,
  nom_referentiel VARCHAR(180) NOT NULL,
  version VARCHAR(80) NOT NULL,
  date_import DATE NULL,
  url_source VARCHAR(500) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uq_source_version (organisme_source, nom_referentiel, version)
) ENGINE=InnoDB;

CREATE TABLE metier (
  id CHAR(36) PRIMARY KEY,
  secteur_id CHAR(36) NOT NULL,
  source_referentiel_id CHAR(36) NULL,
  code VARCHAR(50) NULL UNIQUE,
  nom VARCHAR(180) NOT NULL,
  description TEXT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (secteur_id) REFERENCES secteur_activite(id),
  FOREIGN KEY (source_referentiel_id) REFERENCES source_referentiel(id),
  INDEX idx_metier_secteur (secteur_id),
  INDEX idx_metier_nom (nom)
) ENGINE=InnoDB;

CREATE TABLE competence (
  id CHAR(36) PRIMARY KEY,
  source_referentiel_id CHAR(36) NULL,
  code VARCHAR(60) NULL UNIQUE,
  nom VARCHAR(180) NOT NULL,
  description TEXT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (source_referentiel_id) REFERENCES source_referentiel(id),
  INDEX idx_competence_nom (nom)
) ENGINE=InnoDB;

CREATE TABLE metier_competence (
  metier_id CHAR(36) NOT NULL,
  competence_id CHAR(36) NOT NULL,
  principale BOOLEAN NOT NULL DEFAULT FALSE,
  niveau_attendu VARCHAR(20) NULL,
  PRIMARY KEY (metier_id, competence_id),
  FOREIGN KEY (metier_id) REFERENCES metier(id),
  FOREIGN KEY (competence_id) REFERENCES competence(id),
  CONSTRAINT ck_metier_comp_niveau CHECK (niveau_attendu IS NULL OR niveau_attendu IN ('DEBUTANT','INTERMEDIAIRE','EXPERT'))
) ENGINE=InnoDB;

CREATE TABLE methode_validation (
  id CHAR(36) PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  libelle VARCHAR(120) NOT NULL,
  description TEXT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE competence_methode_validation (
  competence_id CHAR(36) NOT NULL,
  methode_id CHAR(36) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (competence_id, methode_id),
  FOREIGN KEY (competence_id) REFERENCES competence(id),
  FOREIGN KEY (methode_id) REFERENCES methode_validation(id)
) ENGINE=InnoDB;

-- ============================================================
-- 3. CITOYENS ET COMPETENCES DECLAREES
-- ============================================================

CREATE TABLE citoyen (
  id CHAR(36) PRIMARY KEY,
  utilisateur_id CHAR(36) NOT NULL UNIQUE,
  identifiant_public CHAR(36) NOT NULL UNIQUE,
  nom VARCHAR(120) NOT NULL,
  prenom VARCHAR(120) NOT NULL,
  date_naissance DATE NULL,
  sexe VARCHAR(20) NULL,
  statut_activite VARCHAR(30) NOT NULL DEFAULT 'NON_RENSEIGNE',
  profil_public BOOLEAN NOT NULL DEFAULT FALSE,
  -- Consentement a la detection PONCTUELLE de la commune par GPS. Aucune coordonnee n'est stockee.
  gps_consenti BOOLEAN NOT NULL DEFAULT FALSE,
  gps_consenti_le DATETIME(6) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  modifie_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  CONSTRAINT ck_citoyen_sexe CHECK (sexe IS NULL OR sexe IN ('FEMME','HOMME','AUTRE','NON_RENSEIGNE')),
  CONSTRAINT ck_citoyen_activite CHECK (statut_activite IN ('NON_RENSEIGNE','EN_ACTIVITE','SANS_ACTIVITE','EN_FORMATION','ETUDIANT','EN_RECONVERSION','RETRAITE','AUTRE'))
) ENGINE=InnoDB;

CREATE TABLE localisation_profil (
  id CHAR(36) PRIMARY KEY,
  citoyen_id CHAR(36) NOT NULL UNIQUE,
  commune_id CHAR(36) NOT NULL,
  source VARCHAR(30) NOT NULL DEFAULT 'DECLARATION',
  confirmee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  FOREIGN KEY (commune_id) REFERENCES commune(id),
  CONSTRAINT ck_localisation_source CHECK (source IN ('DECLARATION','GPS_CONFIRMEE','ADMINISTRATION'))
) ENGINE=InnoDB;

CREATE TABLE citoyen_competence (
  id CHAR(36) PRIMARY KEY,
  citoyen_id CHAR(36) NOT NULL,
  competence_id CHAR(36) NOT NULL,
  niveau_declare VARCHAR(20) NULL,
  niveau_valide VARCHAR(20) NULL,
  statut_validation VARCHAR(20) NOT NULL DEFAULT 'NON_VERIFIEE',
  situation VARCHAR(20) NOT NULL DEFAULT 'PRATIQUEE',
  disponibilite BOOLEAN NOT NULL DEFAULT FALSE,
  derniere_methode_id CHAR(36) NULL,
  validee_le DATETIME(6) NULL,
  valide_jusqu_au DATE NULL,
  declaree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  modifiee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  FOREIGN KEY (competence_id) REFERENCES competence(id),
  FOREIGN KEY (derniere_methode_id) REFERENCES methode_validation(id),
  UNIQUE KEY uq_citoyen_competence (citoyen_id, competence_id),
  INDEX idx_cc_competence_statut (competence_id, statut_validation),
  CONSTRAINT ck_cc_statut CHECK (statut_validation IN ('NON_VERIFIEE','EN_COURS','VALIDEE','REJETEE','A_REVALIDER')),
  CONSTRAINT ck_cc_niveau_declare CHECK (niveau_declare IS NULL OR niveau_declare IN ('DEBUTANT','INTERMEDIAIRE','EXPERT')),
  CONSTRAINT ck_cc_niveau_valide CHECK (niveau_valide IS NULL OR niveau_valide IN ('DEBUTANT','INTERMEDIAIRE','EXPERT')),
  CONSTRAINT ck_cc_situation CHECK (situation IN ('PRATIQUEE','NON_PRATIQUEE'))
) ENGINE=InnoDB;

-- Une experience appartient au citoyen et peut demontrer plusieurs competences.
CREATE TABLE experience (
  id CHAR(36) PRIMARY KEY,
  citoyen_id CHAR(36) NOT NULL,
  intitule VARCHAR(180) NOT NULL,
  description TEXT NULL,
  date_debut DATE NULL,
  date_fin DATE NULL,
  en_cours BOOLEAN NOT NULL DEFAULT FALSE,
  employeur_ou_client VARCHAR(180) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  INDEX idx_experience_citoyen (citoyen_id),
  CONSTRAINT ck_experience_dates CHECK (date_debut IS NULL OR date_fin IS NULL OR date_fin >= date_debut)
) ENGINE=InnoDB;

CREATE TABLE experience_competence (
  experience_id CHAR(36) NOT NULL,
  citoyen_competence_id CHAR(36) NOT NULL,
  PRIMARY KEY (experience_id, citoyen_competence_id),
  FOREIGN KEY (experience_id) REFERENCES experience(id),
  FOREIGN KEY (citoyen_competence_id) REFERENCES citoyen_competence(id)
) ENGINE=InnoDB;

-- Piece justificative OU realisation (type PORTFOLIO/PHOTO/VIDEO). visible_profil = le citoyen
-- accepte qu'elle soit montree aux organisations autorisees (via le backend, jamais par URL publique).
CREATE TABLE preuve (
  id CHAR(36) PRIMARY KEY,
  citoyen_competence_id CHAR(36) NOT NULL,
  type_preuve VARCHAR(30) NOT NULL,
  titre VARCHAR(180) NULL,
  cle_stockage_prive VARCHAR(500) NULL,
  reference_document VARCHAR(180) NULL,
  description TEXT NULL,
  statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
  visible_profil BOOLEAN NOT NULL DEFAULT FALSE,
  deposee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (citoyen_competence_id) REFERENCES citoyen_competence(id),
  INDEX idx_preuve_competence_statut (citoyen_competence_id, statut),
  CONSTRAINT ck_preuve_type CHECK (type_preuve IN ('DIPLOME','ATTESTATION','PHOTO','VIDEO','PORTFOLIO','AUTRE')),
  CONSTRAINT ck_preuve_statut CHECK (statut IN ('EN_ATTENTE','VERIFIEE','REJETEE','ARCHIVEE'))
) ENGINE=InnoDB;

-- ============================================================
-- 4. CENTRES ET EVALUATEURS
-- ============================================================

CREATE TABLE centre (
  id CHAR(36) PRIMARY KEY,
  nom VARCHAR(180) NOT NULL,
  description TEXT NULL,
  telephone VARCHAR(30) NULL,
  email VARCHAR(190) NULL,
  adresse VARCHAR(255) NULL,
  commune_id CHAR(36) NOT NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (commune_id) REFERENCES commune(id),
  INDEX idx_centre_commune_statut (commune_id, statut),
  CONSTRAINT ck_centre_statut CHECK (statut IN ('EN_ATTENTE','ACTIF','SUSPENDU'))
) ENGINE=InnoDB;

-- Un compte gestionnaire ne gere qu'un seul centre (PK sur utilisateur_id).
CREATE TABLE centre_administrateur (
  utilisateur_id CHAR(36) PRIMARY KEY,
  centre_id CHAR(36) NOT NULL,
  attribue_par_utilisateur_id CHAR(36) NULL,
  attribue_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  FOREIGN KEY (centre_id) REFERENCES centre(id),
  FOREIGN KEY (attribue_par_utilisateur_id) REFERENCES utilisateur(id),
  INDEX idx_centre_admin_centre (centre_id)
) ENGINE=InnoDB;

CREATE TABLE domaine_centre (
  id CHAR(36) PRIMARY KEY,
  centre_id CHAR(36) NOT NULL,
  secteur_id CHAR(36) NULL,
  metier_id CHAR(36) NULL,
  type_domaine VARCHAR(20) NOT NULL,
  FOREIGN KEY (centre_id) REFERENCES centre(id),
  FOREIGN KEY (secteur_id) REFERENCES secteur_activite(id),
  FOREIGN KEY (metier_id) REFERENCES metier(id),
  UNIQUE KEY uq_domaine_centre_secteur (centre_id, secteur_id),
  UNIQUE KEY uq_domaine_centre_metier (centre_id, metier_id),
  CONSTRAINT ck_domaine_centre_exclusif CHECK (
    (type_domaine = 'SECTEUR' AND secteur_id IS NOT NULL AND metier_id IS NULL)
    OR (type_domaine = 'METIER' AND metier_id IS NOT NULL AND secteur_id IS NULL)
  )
) ENGINE=InnoDB;

-- Pas d'unicite (centre, competence) : permet de garder l'historique des periodes d'agrement.
-- Le backend s'assure qu'une seule ligne active couvre une date donnee.
CREATE TABLE centre_competence_validation (
  id CHAR(36) PRIMARY KEY,
  centre_id CHAR(36) NOT NULL,
  competence_id CHAR(36) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  valide_du DATE NULL,
  valide_au DATE NULL,
  FOREIGN KEY (centre_id) REFERENCES centre(id),
  FOREIGN KEY (competence_id) REFERENCES competence(id),
  INDEX idx_ccv_centre_competence (centre_id, competence_id, active),
  CONSTRAINT ck_ccv_periode CHECK (valide_du IS NULL OR valide_au IS NULL OR valide_au >= valide_du)
) ENGINE=InnoDB;

CREATE TABLE evaluateur (
  id CHAR(36) PRIMARY KEY,
  citoyen_id CHAR(36) NOT NULL UNIQUE,
  statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  CONSTRAINT ck_evaluateur_statut CHECK (statut IN ('EN_ATTENTE','ACTIF','SUSPENDU'))
) ENGINE=InnoDB;

CREATE TABLE habilitation_evaluateur (
  id CHAR(36) PRIMARY KEY,
  evaluateur_id CHAR(36) NOT NULL,
  competence_id CHAR(36) NOT NULL,
  methode_id CHAR(36) NOT NULL,
  centre_id CHAR(36) NULL,
  autorise_par_utilisateur_id CHAR(36) NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
  date_debut DATE NULL,
  date_fin DATE NULL,
  FOREIGN KEY (evaluateur_id) REFERENCES evaluateur(id),
  FOREIGN KEY (competence_id) REFERENCES competence(id),
  FOREIGN KEY (methode_id) REFERENCES methode_validation(id),
  FOREIGN KEY (centre_id) REFERENCES centre(id),
  FOREIGN KEY (autorise_par_utilisateur_id) REFERENCES utilisateur(id),
  INDEX idx_habilitation_active (evaluateur_id, competence_id, methode_id, statut),
  CONSTRAINT ck_habilitation_statut CHECK (statut IN ('EN_ATTENTE','ACTIVE','SUSPENDUE','REVOQUEE')),
  CONSTRAINT ck_habilitation_periode CHECK (date_debut IS NULL OR date_fin IS NULL OR date_fin >= date_debut)
) ENGINE=InnoDB;

-- ============================================================
-- 5. VALIDATION DES COMPETENCES
-- ============================================================

CREATE TABLE test_numerique (
  id CHAR(36) PRIMARY KEY,
  competence_id CHAR(36) NOT NULL,
  titre VARCHAR(180) NOT NULL,
  description TEXT NULL,
  version VARCHAR(40) NOT NULL,
  seuil_reussite DECIMAL(5,2) NOT NULL DEFAULT 60,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (competence_id) REFERENCES competence(id),
  UNIQUE KEY uq_test_competence_version (competence_id, version),
  CONSTRAINT ck_test_seuil CHECK (seuil_reussite >= 0 AND seuil_reussite <= 100)
) ENGINE=InnoDB;

CREATE TABLE question (
  id CHAR(36) PRIMARY KEY,
  test_id CHAR(36) NOT NULL,
  enonce TEXT NOT NULL,
  type_question VARCHAR(30) NOT NULL,
  options_json JSON NULL,
  reponse_attendue_json JSON NOT NULL,
  ponderation DECIMAL(7,2) NOT NULL DEFAULT 1,
  ordre_affichage INT NOT NULL,
  FOREIGN KEY (test_id) REFERENCES test_numerique(id),
  UNIQUE KEY uq_question_ordre (test_id, ordre_affichage),
  CONSTRAINT ck_question_type CHECK (type_question IN ('CHOIX_UNIQUE','CHOIX_MULTIPLE','VRAI_FAUX','TEXTE_LIBRE')),
  CONSTRAINT ck_question_ponderation CHECK (ponderation > 0)
) ENGINE=InnoDB;

-- La demande ne suppose PAS de document : une demande MAITRE_ARTISAN ou TEST_NUMERIQUE
-- peut exister sans piece. evaluateur_assigne_id / centre_id sont renseignes selon la methode.
CREATE TABLE demande_validation (
  id CHAR(36) PRIMARY KEY,
  citoyen_competence_id CHAR(36) NOT NULL,
  methode_id CHAR(36) NOT NULL,
  centre_id CHAR(36) NULL,
  evaluateur_assigne_id CHAR(36) NULL,
  message_citoyen TEXT NULL,
  statut VARCHAR(25) NOT NULL DEFAULT 'EN_ATTENTE',
  creee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  cloturee_le DATETIME(6) NULL,
  FOREIGN KEY (citoyen_competence_id) REFERENCES citoyen_competence(id),
  FOREIGN KEY (methode_id) REFERENCES methode_validation(id),
  FOREIGN KEY (centre_id) REFERENCES centre(id),
  FOREIGN KEY (evaluateur_assigne_id) REFERENCES evaluateur(id),
  INDEX idx_demande_validation_statut (statut, creee_le),
  INDEX idx_demande_validation_cc (citoyen_competence_id, statut),
  CONSTRAINT ck_demande_statut CHECK (statut IN ('EN_ATTENTE','EN_COURS','COMPLEMENT_REQUIS','VALIDEE','REJETEE','ANNULEE'))
) ENGINE=InnoDB;

-- La competence concernee se deduit de demande_validation -> citoyen_competence.
CREATE TABLE tentative_test (
  id CHAR(36) PRIMARY KEY,
  demande_validation_id CHAR(36) NOT NULL,
  test_id CHAR(36) NOT NULL,
  score DECIMAL(5,2) NULL,
  seuil_applique DECIMAL(5,2) NOT NULL,
  reussi BOOLEAN NULL,
  reponses_json JSON NULL,
  commencee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  terminee_le DATETIME(6) NULL,
  FOREIGN KEY (demande_validation_id) REFERENCES demande_validation(id),
  FOREIGN KEY (test_id) REFERENCES test_numerique(id),
  INDEX idx_tentative_demande (demande_validation_id, terminee_le),
  CONSTRAINT ck_tentative_score CHECK (score IS NULL OR (score >= 0 AND score <= 100))
) ENGINE=InnoDB;

-- Evaluation pratique : centre OU maitre artisan / evaluateur habilite (centre_id facultatif).
CREATE TABLE evaluation_pratique (
  id CHAR(36) PRIMARY KEY,
  demande_validation_id CHAR(36) NOT NULL,
  evaluateur_id CHAR(36) NOT NULL,
  centre_id CHAR(36) NULL,
  score DECIMAL(5,2) NULL,
  observations TEXT NULL,
  grille_json JSON NULL,
  realisee_le DATETIME(6) NULL,
  FOREIGN KEY (demande_validation_id) REFERENCES demande_validation(id),
  FOREIGN KEY (evaluateur_id) REFERENCES evaluateur(id),
  FOREIGN KEY (centre_id) REFERENCES centre(id),
  INDEX idx_evaluation_demande (demande_validation_id),
  INDEX idx_evaluation_centre_date (centre_id, realisee_le),
  CONSTRAINT ck_evaluation_score CHECK (score IS NULL OR (score >= 0 AND score <= 100))
) ENGINE=InnoDB;

-- Historique IMMUABLE : jamais de UPDATE ni de DELETE (une correction = nouvelle decision).
CREATE TABLE decision_validation (
  id CHAR(36) PRIMARY KEY,
  demande_validation_id CHAR(36) NOT NULL,
  decideur_utilisateur_id CHAR(36) NULL,
  decision VARCHAR(25) NOT NULL,
  niveau_valide VARCHAR(20) NULL,
  motif TEXT NULL,
  decidee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (demande_validation_id) REFERENCES demande_validation(id),
  FOREIGN KEY (decideur_utilisateur_id) REFERENCES utilisateur(id),
  INDEX idx_decision_demande_date (demande_validation_id, decidee_le),
  CONSTRAINT ck_decision_validation CHECK (decision IN ('VALIDEE','REJETEE','COMPLEMENT_REQUIS','ANNULEE')),
  CONSTRAINT ck_decision_niveau CHECK (niveau_valide IS NULL OR niveau_valide IN ('DEBUTANT','INTERMEDIAIRE','EXPERT'))
) ENGINE=InnoDB;

-- Contestation / demande de correction faite par un citoyen (decision de validation ou donnee de profil).
CREATE TABLE contestation (
  id CHAR(36) PRIMARY KEY,
  citoyen_id CHAR(36) NOT NULL,
  type_objet VARCHAR(30) NOT NULL,
  decision_validation_id CHAR(36) NULL,
  champ_concerne VARCHAR(80) NULL,
  motif TEXT NOT NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'OUVERTE',
  reponse TEXT NULL,
  traitee_par_utilisateur_id CHAR(36) NULL,
  creee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  traitee_le DATETIME(6) NULL,
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  FOREIGN KEY (decision_validation_id) REFERENCES decision_validation(id),
  FOREIGN KEY (traitee_par_utilisateur_id) REFERENCES utilisateur(id),
  INDEX idx_contestation_statut (statut, creee_le),
  INDEX idx_contestation_citoyen (citoyen_id),
  CONSTRAINT ck_contestation_type CHECK (type_objet IN ('DECISION_VALIDATION','DONNEE_PROFIL','AUTRE')),
  CONSTRAINT ck_contestation_statut CHECK (statut IN ('OUVERTE','EN_EXAMEN','ACCEPTEE','REJETEE'))
) ENGINE=InnoDB;

-- ============================================================
-- 6. ORGANISATIONS ET BESOINS
-- ============================================================

CREATE TABLE organisation (
  id CHAR(36) PRIMARY KEY,
  utilisateur_id CHAR(36) NOT NULL UNIQUE,
  raison_sociale VARCHAR(200) NOT NULL,
  nom_commercial VARCHAR(200) NULL,
  description TEXT NULL,
  rccm VARCHAR(100) NULL,
  nif VARCHAR(100) NULL,
  telephone VARCHAR(30) NULL,
  email VARCHAR(190) NULL,
  adresse VARCHAR(255) NULL,
  commune_id CHAR(36) NULL,
  statut_verification VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
  motif_statut TEXT NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  FOREIGN KEY (commune_id) REFERENCES commune(id),
  INDEX idx_organisation_statut (statut_verification),
  CONSTRAINT ck_organisation_statut CHECK (statut_verification IN ('EN_ATTENTE','APPROUVEE','REJETEE','SUSPENDUE'))
) ENGINE=InnoDB;

CREATE TABLE justificatif_organisation (
  id CHAR(36) PRIMARY KEY,
  organisation_id CHAR(36) NOT NULL,
  type_justificatif VARCHAR(40) NOT NULL,
  reference_document VARCHAR(180) NULL,
  cle_stockage_prive VARCHAR(500) NULL,
  date_emission DATE NULL,
  date_expiration DATE NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'RENSEIGNE',
  verifie_par_utilisateur_id CHAR(36) NULL,
  verifie_le DATETIME(6) NULL,
  motif_rejet TEXT NULL,
  FOREIGN KEY (organisation_id) REFERENCES organisation(id),
  FOREIGN KEY (verifie_par_utilisateur_id) REFERENCES utilisateur(id),
  INDEX idx_justificatif_org_statut (organisation_id, statut),
  CONSTRAINT ck_justificatif_statut CHECK (statut IN ('RENSEIGNE','VERIFIE','REJETE','EXPIRE'))
) ENGINE=InnoDB;

-- Le nombre recherché est porté par besoin_competence.nombre_requis (pas par le besoin).
CREATE TABLE besoin (
  id CHAR(36) PRIMARY KEY,
  organisation_id CHAR(36) NOT NULL,
  titre VARCHAR(180) NOT NULL,
  description TEXT NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
  date_limite DATE NULL,
  publie_le DATETIME(6) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  modifie_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  FOREIGN KEY (organisation_id) REFERENCES organisation(id),
  INDEX idx_besoin_statut_date (statut, date_limite),
  INDEX idx_besoin_organisation (organisation_id, statut),
  CONSTRAINT ck_besoin_statut CHECK (statut IN ('BROUILLON','OUVERT','CLOTURE','ANNULE'))
) ENGINE=InnoDB;

CREATE TABLE besoin_competence (
   id CHAR(36) NOT NULL,
   besoin_id CHAR(36) NOT NULL,
   competence_id CHAR(36) NOT NULL,
   nombre_requis INT NOT NULL DEFAULT 1,
   niveau_requis VARCHAR(20) NULL,
   PRIMARY KEY (id),
   CONSTRAINT uk_besoin_competence UNIQUE (besoin_id, competence_id),
   CONSTRAINT fk_besoin_competence_besoin FOREIGN KEY (besoin_id) REFERENCES besoin(id),
   CONSTRAINT fk_besoin_competence_competence FOREIGN KEY (competence_id) REFERENCES competence(id),
   CONSTRAINT ck_besoin_comp_nombre CHECK (nombre_requis > 0),
   CONSTRAINT ck_besoin_comp_niveau CHECK (niveau_requis IS NULL OR niveau_requis IN ('DEBUTANT', 'INTERMEDIAIRE', 'EXPERT'))
) ENGINE=InnoDB;

-- commune OU territoire (region, district, cercle). Aucune ligne = portee nationale.
CREATE TABLE besoin_zone (
  id CHAR(36) PRIMARY KEY,
  besoin_id CHAR(36) NOT NULL,
  commune_id CHAR(36) NULL,
  territoire_id CHAR(36) NULL,
  FOREIGN KEY (besoin_id) REFERENCES besoin(id),
  FOREIGN KEY (commune_id) REFERENCES commune(id),
  FOREIGN KEY (territoire_id) REFERENCES territoire(id),
  UNIQUE KEY uq_besoin_zone_commune (besoin_id, commune_id),
  UNIQUE KEY uq_besoin_zone_territoire (besoin_id, territoire_id),
  INDEX idx_besoin_zone_commune (commune_id),
  INDEX idx_besoin_zone_territoire (territoire_id),
  CONSTRAINT ck_besoin_zone_exclusif CHECK (
    (commune_id IS NOT NULL AND territoire_id IS NULL)
    OR (commune_id IS NULL AND territoire_id IS NOT NULL)
  )
) ENGINE=InnoDB;

-- ============================================================
-- 7. OPPORTUNITES (formations, bourses, programmes, concours...)
-- ============================================================

CREATE TABLE categorie_opportunite (
  id CHAR(36) PRIMARY KEY,
  nom VARCHAR(150) NOT NULL UNIQUE,
  description TEXT NULL
) ENGINE=InnoDB;

-- Publiee uniquement par un compte autorise (admin, centre, institution). Une opportunite
-- dont date_expiration est depassee n'est jamais affichee (regle sur la date, meme si le statut
-- EXPIREE n'a pas encore ete positionne par une tache planifiee).
CREATE TABLE opportunite (
  id CHAR(36) PRIMARY KEY,
  categorie_id CHAR(36) NOT NULL,
  titre VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  organisme VARCHAR(180) NOT NULL,
  lien_externe VARCHAR(500) NULL,
  contact VARCHAR(190) NULL,
  territoire_id CHAR(36) NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'BROUILLON',
  publiee_le DATETIME(6) NULL,
  date_expiration DATE NULL,
  cree_par_utilisateur_id CHAR(36) NOT NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  modifie_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  FOREIGN KEY (categorie_id) REFERENCES categorie_opportunite(id),
  FOREIGN KEY (territoire_id) REFERENCES territoire(id),
  FOREIGN KEY (cree_par_utilisateur_id) REFERENCES utilisateur(id),
  INDEX idx_opportunite_statut_exp (statut, date_expiration),
  CONSTRAINT ck_opportunite_statut CHECK (statut IN ('BROUILLON','PUBLIEE','EXPIREE','ARCHIVEE'))
) ENGINE=InnoDB;

-- ============================================================
-- 8. NOTIFICATIONS
-- ============================================================

CREATE TABLE notification (
  id CHAR(36) PRIMARY KEY,
  utilisateur_id CHAR(36) NOT NULL,
  type_evenement VARCHAR(60) NOT NULL,
  titre VARCHAR(180) NOT NULL,
  contenu VARCHAR(1000) NOT NULL,
  type_ressource VARCHAR(80) NULL,
  ressource_id CHAR(36) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  lue_le DATETIME(6) NULL,
  cle_deduplication VARCHAR(190) NULL,
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  UNIQUE KEY uq_notification_dedup (utilisateur_id, cle_deduplication),
  INDEX idx_notification_inbox (utilisateur_id, lue_le, cree_le)
) ENGINE=InnoDB;

CREATE TABLE appareil_notification (
  id CHAR(36) PRIMARY KEY,
  utilisateur_id CHAR(36) NOT NULL,
  jeton_hash VARCHAR(255) NOT NULL UNIQUE,
  plateforme VARCHAR(20) NOT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  derniere_activite DATETIME(6) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  CONSTRAINT ck_appareil_plateforme CHECK (plateforme IN ('ANDROID','IOS','WEB'))
) ENGINE=InnoDB;

-- ============================================================
-- 9. SUIVI ET MISE EN RELATION
-- ============================================================

CREATE TABLE suivi_besoin_talent (
  id CHAR(36) PRIMARY KEY,
  besoin_id CHAR(36) NOT NULL,
  citoyen_id CHAR(36) NOT NULL,
  statut VARCHAR(25) NOT NULL DEFAULT 'A_L_ETUDE',
  note_interne TEXT NULL,
  modifie_par_utilisateur_id CHAR(36) NULL,
  cree_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  modifie_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  FOREIGN KEY (besoin_id) REFERENCES besoin(id),
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  FOREIGN KEY (modifie_par_utilisateur_id) REFERENCES utilisateur(id),
  UNIQUE KEY uq_suivi_besoin_citoyen (besoin_id, citoyen_id),
  INDEX idx_suivi_statut (besoin_id, statut),
  CONSTRAINT ck_suivi_statut CHECK (statut IN ('A_L_ETUDE','CONTACT_EN_COURS','MISE_EN_RELATION','ARCHIVE'))
) ENGINE=InnoDB;

-- La clé d'attente est renseignée par le service : suivi_id pour une demande liée à un besoin,
-- citoyen_id pour un contact direct. Elle est remise à NULL dès que la demande quitte EN_ATTENTE.
-- Les index uniques protègent aussi contre les créations concurrentes sans colonne générée.
CREATE TABLE demande_mise_en_relation (
  id CHAR(36) PRIMARY KEY,
  demandeur_utilisateur_id CHAR(36) NOT NULL,
  destinataire_citoyen_id CHAR(36) NOT NULL,
  suivi_besoin_talent_id CHAR(36) NULL,
  en_attente_cle CHAR(36) NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
  message TEXT NULL,
  creee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  repondue_le DATETIME(6) NULL,
  FOREIGN KEY (demandeur_utilisateur_id) REFERENCES utilisateur(id),
  FOREIGN KEY (destinataire_citoyen_id) REFERENCES citoyen(id),
  FOREIGN KEY (suivi_besoin_talent_id) REFERENCES suivi_besoin_talent(id),
  UNIQUE KEY uq_mise_relation_suivi_attente (suivi_besoin_talent_id, en_attente_cle),
  UNIQUE KEY uq_mise_relation_contact_direct_attente (demandeur_utilisateur_id, destinataire_citoyen_id, en_attente_cle),
  INDEX idx_mise_relation_suivi (suivi_besoin_talent_id, creee_le),
  INDEX idx_mise_relation_destinataire_statut (destinataire_citoyen_id, statut),
  CONSTRAINT ck_mise_relation_statut CHECK (statut IN ('EN_ATTENTE','ACCEPTEE','REFUSEE','ANNULEE'))
) ENGINE=InnoDB;

CREATE TABLE consultation_profil (
  id CHAR(36) PRIMARY KEY,
  organisation_id CHAR(36) NOT NULL,
  citoyen_id CHAR(36) NOT NULL,
  besoin_id CHAR(36) NULL,
  consultee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  FOREIGN KEY (organisation_id) REFERENCES organisation(id),
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  FOREIGN KEY (besoin_id) REFERENCES besoin(id),
  INDEX idx_consultation_citoyen_date (citoyen_id, consultee_le),
  INDEX idx_consultation_organisation (organisation_id, consultee_le)
) ENGINE=InnoDB;

-- ============================================================
-- 10. LOCALISATION
-- La commune de reference est localisation_profil. Le GPS ne sert qu'a SUGGERER un changement,
-- ponctuellement : les coordonnees recues ne sont ni stockees ni journalisees.
-- Les statistiques (offre, demande, ecart) sont calculees a la demande : pas de table d'indicateurs en V1.
-- ============================================================

-- Proposition de changement de commune (suggestion ponctuelle ou signalement du citoyen).
-- en_attente_cle est une colonne ordinaire nullable alimentée par le service : citoyen_id tant que
-- la demande est EN_ATTENTE, NULL après décision. L'unicité est ainsi portable sur MariaDB.
CREATE TABLE demande_actualisation_localisation (
  id CHAR(36) PRIMARY KEY,
  citoyen_id CHAR(36) NOT NULL,
  commune_suggeree_id CHAR(36) NULL,
  statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
  creee_le DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  repondue_le DATETIME(6) NULL,
  en_attente_cle CHAR(36) NULL,
  FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
  FOREIGN KEY (commune_suggeree_id) REFERENCES commune(id),
  UNIQUE KEY uq_actualisation_en_attente (en_attente_cle),
  INDEX idx_actualisation_citoyen_statut (citoyen_id, statut),
  CONSTRAINT ck_actualisation_statut CHECK (statut IN ('EN_ATTENTE','ACCEPTEE','REFUSEE','EXPIREE'))
) ENGINE=InnoDB;

-- ============================================================
-- DONNEES DE BASE (roles, methodes, categories d'opportunites)
-- Les referentiels geographiques et metiers/competences sont charges par des migrations de donnees separees.
-- ============================================================

-- CENTRE_ADMIN n'est volontairement PAS ici : ce role se deduit de centre_administrateur.
INSERT INTO role (id, code, libelle) VALUES
(UUID(), 'CITOYEN', 'Citoyen'),
(UUID(), 'ORGANISATION', 'Organisation'),
(UUID(), 'ADMIN', 'Administrateur'),
(UUID(), 'SUPER_ADMIN', 'Super administrateur'),
(UUID(), 'INSTITUTION', 'Institution'),
(UUID(), 'AGENT_TERRAIN', 'Agent de terrain (inscription assistée)');

INSERT INTO methode_validation (id, code, libelle, description) VALUES
(UUID(), 'DOCUMENT', 'Document officiel', 'Vérification d’un diplôme ou d’une attestation'),
(UUID(), 'TEST_NUMERIQUE', 'Test numérique', 'Évaluation par questionnaire numérique'),
(UUID(), 'EVALUATION_CENTRE', 'Évaluation pratique en centre', 'Évaluation pratique par un centre habilité'),
(UUID(), 'PORTFOLIO', 'Portfolio de réalisations', 'Examen de photos, vidéos ou réalisations'),
(UUID(), 'MAITRE_ARTISAN', 'Évaluation par un maître artisan', 'Évaluation par une personne habilitée');

INSERT INTO categorie_opportunite (id, nom, description) VALUES
(UUID(), 'Formations et apprentissage', 'Formations gratuites et opportunités d’apprentissage.'),
(UUID(), 'Bourses', 'Bourses d’études et de formation.'),
(UUID(), 'Programmes', 'Programmes d’accompagnement et d’insertion.'),
(UUID(), 'Concours et appels', 'Concours et appels à candidatures.'),
(UUID(), 'Accompagnement', 'Dispositifs d’accompagnement professionnel.');

-- Notes d'implementation :
-- 1) Les autorisations sur les ressources sont controlees dans le backend (voir dictionnaire, section 9).
-- 2) decision_validation est en ajout seul. Une decision favorable met a jour citoyen_competence
--    (statut_validation, niveau_valide, derniere_methode_id, validee_le) dans la MEME transaction.
-- 3) Les statistiques publiques appliquent un seuil minimum d'effectif avant affichage.
-- 4) Le role CENTRE_ADMIN est deduit de centre_administrateur (jamais de utilisateur_role).
