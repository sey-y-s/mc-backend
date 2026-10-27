-- MaliCompétences — schéma initial (V1).
-- Compatible MySQL 8.0.16+ / MariaDB 10.5+. À exécuter sur une base vide : Flyway s'en charge au démarrage.
-- UUID en CHAR(36) générés par l'application. Les téléphones sont stockés au format international (+22370000000).
-- Les tables sont créées dans l'ordre de leurs dépendances (une table n'en référence que des déjà créées).
-- Les données de référence sont dans V2 et suivantes ; les données de démonstration sont dans db/dev-data.


-- ============================================================
-- 1. COMPTES, SÉCURITÉ ET AUDIT
-- ============================================================

CREATE TABLE utilisateur (
  id char(36) NOT NULL,
  telephone varchar(30) NOT NULL,
  email varchar(255) DEFAULT NULL,
  password_hash varchar(255) NOT NULL,
  actif tinyint(1) NOT NULL DEFAULT 1,
  date_creation datetime NOT NULL,
  role varchar(30) NOT NULL,
  conditions_acceptees_le datetime DEFAULT NULL,
  cree_par_id char(36) DEFAULT NULL,
  consentement_recueilli_le datetime DEFAULT NULL,
  telephone_verifie_le datetime DEFAULT NULL,
  email_verifie_le datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_utilisateur_telephone (telephone),
  UNIQUE KEY uk_utilisateur_email (email),
  KEY fk_utilisateur_cree_par (cree_par_id),
  CONSTRAINT fk_utilisateur_cree_par FOREIGN KEY (cree_par_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE preferences_utilisateur (
  utilisateur_id char(36) NOT NULL,
  low_data_mode tinyint(1) NOT NULL DEFAULT 0,
  notifications_enabled tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (utilisateur_id),
  CONSTRAINT fk_preferences_utilisateur_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reset_password_token (
  id char(36) NOT NULL,
  utilisateur_id char(36) NOT NULL,
  code_hash varchar(100) NOT NULL,
  expire_le datetime NOT NULL,
  utilise tinyint(1) NOT NULL DEFAULT 0,
  tentatives int(11) NOT NULL DEFAULT 0,
  date_creation datetime NOT NULL,
  PRIMARY KEY (id),
  KEY idx_reset_token_utilisateur (utilisateur_id),
  CONSTRAINT fk_reset_token_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE code_verification (
  id char(36) NOT NULL,
  utilisateur_id char(36) NOT NULL,
  canal varchar(20) NOT NULL,
  code_hash varchar(100) NOT NULL,
  expire_le datetime NOT NULL,
  utilise_le datetime DEFAULT NULL,
  tentatives int(11) NOT NULL DEFAULT 0,
  cree_le datetime NOT NULL,
  PRIMARY KEY (id),
  KEY idx_code_verification_utilisateur (utilisateur_id,canal),
  CONSTRAINT fk_code_verification_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE jeton_renouvellement (
  id char(36) NOT NULL,
  utilisateur_id char(36) NOT NULL,
  famille_id char(36) NOT NULL,
  empreinte char(64) NOT NULL,
  cree_le datetime NOT NULL,
  expire_le datetime NOT NULL,
  utilise_le datetime DEFAULT NULL,
  revoque tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_jeton_renouvellement_empreinte (empreinte),
  KEY idx_jeton_renouvellement_utilisateur (utilisateur_id),
  KEY idx_jeton_renouvellement_famille (famille_id),
  CONSTRAINT fk_jeton_renouvellement_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_log (
  id char(36) NOT NULL,
  created_at datetime(6) NOT NULL,
  actor_id varchar(100) DEFAULT NULL,
  action varchar(80) NOT NULL,
  target_type varchar(80) NOT NULL,
  target_id varchar(100) DEFAULT NULL,
  result_code varchar(24) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_audit_created_at (created_at),
  KEY idx_audit_actor (actor_id),
  KEY idx_audit_target (target_type,target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 2. TERRITOIRES
-- ============================================================

CREATE TABLE region (
  id char(36) NOT NULL,
  nom varchar(100) NOT NULL,
  description text DEFAULT NULL,
  code_stable varchar(32) DEFAULT NULL,
  niveau varchar(16) NOT NULL DEFAULT 'REGION',
  latitude double DEFAULT NULL,
  longitude double DEFAULT NULL,
  parent_id char(36) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_region_nom (nom),
  UNIQUE KEY uk_region_code_stable (code_stable),
  KEY idx_region_niveau (niveau),
  KEY fk_region_parent (parent_id),
  CONSTRAINT fk_region_parent FOREIGN KEY (parent_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE commune (
  id char(36) NOT NULL,
  region_id char(36) NOT NULL,
  nom varchar(100) NOT NULL,
  description text DEFAULT NULL,
  cercle_id char(36) DEFAULT NULL,
  latitude double DEFAULT NULL,
  longitude double DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_commune_region_nom (region_id,nom),
  KEY fk_commune_cercle (cercle_id),
  CONSTRAINT fk_commune_cercle FOREIGN KEY (cercle_id) REFERENCES region (id),
  CONSTRAINT fk_commune_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 3. RÉFÉRENTIEL DES COMPÉTENCES
-- ============================================================

CREATE TABLE secteur_activite (
  id char(36) NOT NULL,
  nom varchar(150) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_secteur_activite_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE metier (
  id char(36) NOT NULL,
  secteur_activite_id char(36) NOT NULL,
  nom varchar(150) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_metier_secteur_nom (secteur_activite_id,nom),
  CONSTRAINT fk_metier_secteur FOREIGN KEY (secteur_activite_id) REFERENCES secteur_activite (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE competence (
  id char(36) NOT NULL,
  metier_id char(36) NOT NULL,
  nom varchar(200) NOT NULL,
  description text DEFAULT NULL,
  parcours_possibles varchar(30) NOT NULL DEFAULT 'FORMEL,INFORMEL',
  PRIMARY KEY (id),
  UNIQUE KEY uk_competence_metier_nom (metier_id,nom),
  CONSTRAINT fk_competence_metier FOREIGN KEY (metier_id) REFERENCES metier (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE competence_modalite (
  id char(36) NOT NULL,
  competence_id char(36) NOT NULL,
  modalite varchar(30) NOT NULL,
  ordre int(11) NOT NULL DEFAULT 0,
  seuil_reussite int(11) DEFAULT NULL,
  pieces_admissibles varchar(300) DEFAULT NULL,
  profils_habilites varchar(300) DEFAULT NULL,
  criteres varchar(500) DEFAULT NULL,
  actif tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_competence_modalite (competence_id,modalite),
  CONSTRAINT fk_competence_modalite_competence FOREIGN KEY (competence_id) REFERENCES competence (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 4. CITOYENS, EXPÉRIENCES ET LOCALISATION
-- ============================================================

CREATE TABLE citoyen (
  id char(36) NOT NULL,
  nom varchar(100) NOT NULL,
  prenom varchar(100) NOT NULL,
  sexe varchar(20) DEFAULT NULL,
  photo_url varchar(500) DEFAULT NULL,
  disponibilite varchar(30) NOT NULL,
  code_passeport char(14) NOT NULL,
  commune_id char(36) DEFAULT NULL,
  gps_consenti tinyint(1) NOT NULL DEFAULT 0,
  gps_consenti_le datetime DEFAULT NULL,
  localisation_source varchar(20) DEFAULT NULL,
  localisation_confirmee_le datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_citoyen_code_passeport (code_passeport),
  KEY idx_citoyen_commune (commune_id),
  CONSTRAINT fk_citoyen_commune FOREIGN KEY (commune_id) REFERENCES commune (id),
  CONSTRAINT fk_citoyen_utilisateur FOREIGN KEY (id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE citoyen_competence (
  id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  competence_id char(36) NOT NULL,
  niveau varchar(30) NOT NULL,
  etat varchar(30) NOT NULL,
  date_declaration datetime NOT NULL,
  parcours varchar(20) DEFAULT NULL,
  niveau_valide varchar(30) DEFAULT NULL,
  validee_le datetime DEFAULT NULL,
  valide_jusqu_au date DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_citoyen_competence (citoyen_id,competence_id),
  KEY fk_citoyen_competence_competence (competence_id),
  CONSTRAINT fk_citoyen_competence_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_citoyen_competence_competence FOREIGN KEY (competence_id) REFERENCES competence (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE experience (
  id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  titre varchar(200) NOT NULL,
  description text DEFAULT NULL,
  entreprise varchar(200) DEFAULT NULL,
  date_debut date NOT NULL,
  date_fin date DEFAULT NULL,
  en_cours tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY fk_experience_citoyen (citoyen_id),
  CONSTRAINT fk_experience_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE experience_competence (
  experience_id char(36) NOT NULL,
  citoyen_competence_id char(36) NOT NULL,
  PRIMARY KEY (experience_id,citoyen_competence_id),
  KEY fk_experience_competence_citoyen_competence (citoyen_competence_id),
  CONSTRAINT fk_experience_competence_citoyen_competence FOREIGN KEY (citoyen_competence_id) REFERENCES citoyen_competence (id),
  CONSTRAINT fk_experience_competence_experience FOREIGN KEY (experience_id) REFERENCES experience (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE portfolio_realisation (
  id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  titre varchar(200) NOT NULL,
  description text DEFAULT NULL,
  date_realisation date DEFAULT NULL,
  lien_url varchar(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_portfolio_realisation_citoyen (citoyen_id),
  CONSTRAINT fk_portfolio_realisation_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE media_realisation (
  id char(36) NOT NULL,
  portfolio_realisation_id char(36) NOT NULL,
  type varchar(30) NOT NULL,
  url varchar(500) NOT NULL,
  nom_fichier varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_media_realisation_portfolio (portfolio_realisation_id),
  CONSTRAINT fk_media_realisation_portfolio FOREIGN KEY (portfolio_realisation_id) REFERENCES portfolio_realisation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE demande_actualisation_localisation (
  id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  commune_suggeree_id char(36) NOT NULL,
  statut varchar(20) NOT NULL DEFAULT 'EN_ATTENTE',
  date_creation datetime NOT NULL,
  date_reponse datetime DEFAULT NULL,
  en_attente_cle char(36) GENERATED ALWAYS AS (case when statut = 'EN_ATTENTE' then rtrim(citoyen_id) else NULL end) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uk_actualisation_en_attente (en_attente_cle),
  KEY fk_actualisation_commune (commune_suggeree_id),
  KEY idx_actualisation_citoyen_statut (citoyen_id,statut),
  CONSTRAINT fk_actualisation_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_actualisation_commune FOREIGN KEY (commune_suggeree_id) REFERENCES commune (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE consultation_profil (
  id char(36) NOT NULL,
  utilisateur_id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  date_consultation datetime NOT NULL,
  PRIMARY KEY (id),
  KEY fk_consultation_profil_utilisateur (utilisateur_id),
  KEY fk_consultation_profil_citoyen (citoyen_id),
  CONSTRAINT fk_consultation_profil_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_consultation_profil_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 5. ORGANISATIONS, BESOINS ET MISE EN RELATION
-- ============================================================

CREATE TABLE organisation (
  id char(36) NOT NULL,
  nom varchar(150) NOT NULL,
  description text DEFAULT NULL,
  statut varchar(30) NOT NULL,
  motif_statut text DEFAULT NULL,
  latitude double DEFAULT NULL,
  longitude double DEFAULT NULL,
  date_position datetime DEFAULT NULL,
  type_organisation varchar(40) DEFAULT NULL,
  sigle varchar(30) DEFAULT NULL,
  forme_juridique varchar(80) DEFAULT NULL,
  secteur_activite varchar(150) DEFAULT NULL,
  date_creation date DEFAULT NULL,
  effectif int(11) DEFAULT NULL,
  numero_rccm varchar(80) DEFAULT NULL,
  nif varchar(40) DEFAULT NULL,
  numero_inps varchar(40) DEFAULT NULL,
  numero_recepisse varchar(80) DEFAULT NULL,
  numero_accord_cadre varchar(80) DEFAULT NULL,
  reference_acte varchar(150) DEFAULT NULL,
  adresse varchar(255) DEFAULT NULL,
  commune_id char(36) DEFAULT NULL,
  site_web varchar(255) DEFAULT NULL,
  responsable_nom varchar(150) DEFAULT NULL,
  responsable_fonction varchar(100) DEFAULT NULL,
  responsable_telephone varchar(30) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_organisation_commune (commune_id),
  CONSTRAINT fk_organisation_commune FOREIGN KEY (commune_id) REFERENCES commune (id),
  CONSTRAINT fk_organisation_utilisateur FOREIGN KEY (id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE organisation_document (
  id char(36) NOT NULL,
  organisation_id char(36) NOT NULL,
  type_document varchar(40) NOT NULL,
  nom_fichier varchar(255) NOT NULL,
  chemin_fichier varchar(500) NOT NULL,
  type_contenu varchar(100) DEFAULT NULL,
  taille_octets bigint(20) DEFAULT NULL,
  statut varchar(20) NOT NULL,
  commentaire text DEFAULT NULL,
  date_depot datetime NOT NULL,
  PRIMARY KEY (id),
  KEY idx_organisation_document_organisation (organisation_id),
  CONSTRAINT fk_organisation_document_organisation FOREIGN KEY (organisation_id) REFERENCES organisation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE besoin (
  id char(36) NOT NULL,
  organisation_id char(36) NOT NULL,
  titre varchar(200) NOT NULL,
  description text DEFAULT NULL,
  statut varchar(30) NOT NULL,
  date_debut date DEFAULT NULL,
  date_fin date DEFAULT NULL,
  date_creation datetime NOT NULL,
  latitude double DEFAULT NULL,
  longitude double DEFAULT NULL,
  region_id char(36) DEFAULT NULL,
  commune_id char(36) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_besoin_organisation (organisation_id),
  KEY fk_besoin_region (region_id),
  KEY fk_besoin_commune (commune_id),
  CONSTRAINT fk_besoin_commune FOREIGN KEY (commune_id) REFERENCES commune (id),
  CONSTRAINT fk_besoin_organisation FOREIGN KEY (organisation_id) REFERENCES organisation (id),
  CONSTRAINT fk_besoin_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE besoin_competence (
  id char(36) NOT NULL,
  besoin_id char(36) NOT NULL,
  competence_id char(36) NOT NULL,
  niveau_minimum varchar(30) NOT NULL,
  quantite int(11) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_besoin_competence (besoin_id,competence_id),
  KEY fk_besoin_competence_competence (competence_id),
  CONSTRAINT fk_besoin_competence_besoin FOREIGN KEY (besoin_id) REFERENCES besoin (id),
  CONSTRAINT fk_besoin_competence_competence FOREIGN KEY (competence_id) REFERENCES competence (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE suivi_besoin_talent (
  id char(36) NOT NULL,
  organisation_id char(36) NOT NULL,
  besoin_id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  statut varchar(30) NOT NULL,
  score_correspondance decimal(5,2) DEFAULT NULL,
  details_correspondance JSON NULL,
  note_interne text DEFAULT NULL,
  date_creation datetime NOT NULL,
  date_mise_a_jour datetime NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_suivi_talent_besoin_citoyen (besoin_id,citoyen_id),
  KEY fk_suivi_talent_organisation (organisation_id),
  KEY fk_suivi_talent_citoyen (citoyen_id),
  CONSTRAINT fk_suivi_talent_besoin FOREIGN KEY (besoin_id) REFERENCES besoin (id),
  CONSTRAINT fk_suivi_talent_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_suivi_talent_organisation FOREIGN KEY (organisation_id) REFERENCES organisation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE demande_mise_en_relation (
  id char(36) NOT NULL,
  demandeur_id char(36) NOT NULL,
  destinataire_id char(36) NOT NULL,
  suivi_besoin_talent_id char(36) DEFAULT NULL,
  statut varchar(30) NOT NULL,
  message text DEFAULT NULL,
  date_demande datetime NOT NULL,
  date_reponse datetime DEFAULT NULL,
  en_attente_cle char(36) GENERATED ALWAYS AS (case when statut = 'EN_ATTENTE' then rtrim(suivi_besoin_talent_id) else NULL end) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mise_relation_en_attente (en_attente_cle),
  KEY fk_demande_relation_demandeur (demandeur_id),
  KEY fk_demande_relation_destinataire (destinataire_id),
  KEY fk_demande_relation_suivi_besoin_talent (suivi_besoin_talent_id),
  CONSTRAINT fk_demande_relation_demandeur FOREIGN KEY (demandeur_id) REFERENCES utilisateur (id),
  CONSTRAINT fk_demande_relation_destinataire FOREIGN KEY (destinataire_id) REFERENCES citoyen (id),
  CONSTRAINT fk_demande_relation_suivi_besoin_talent FOREIGN KEY (suivi_besoin_talent_id) REFERENCES suivi_besoin_talent (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 6. CENTRES, ÉVALUATEURS ET VALIDATION DES COMPÉTENCES
-- ============================================================

CREATE TABLE centre (
  id char(36) NOT NULL,
  nom varchar(150) NOT NULL,
  description text DEFAULT NULL,
  adresse varchar(255) DEFAULT NULL,
  telephone varchar(30) DEFAULT NULL,
  administrateur_id char(36) DEFAULT NULL,
  statut varchar(20) NOT NULL DEFAULT 'ACTIF',
  commune_id char(36) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_centre_administrateur (administrateur_id),
  KEY fk_centre_commune (commune_id),
  CONSTRAINT fk_centre_administrateur FOREIGN KEY (administrateur_id) REFERENCES utilisateur (id),
  CONSTRAINT fk_centre_commune FOREIGN KEY (commune_id) REFERENCES commune (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE centre_competence (
  id char(36) NOT NULL,
  centre_id char(36) NOT NULL,
  competence_id char(36) NOT NULL,
  actif tinyint(1) NOT NULL DEFAULT 1,
  date_debut date DEFAULT NULL,
  date_fin date DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_centre_competence (centre_id,competence_id),
  KEY fk_centre_competence_competence (competence_id),
  CONSTRAINT fk_centre_competence_centre FOREIGN KEY (centre_id) REFERENCES centre (id),
  CONSTRAINT fk_centre_competence_competence FOREIGN KEY (competence_id) REFERENCES competence (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE evaluateur_centre (
  id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  centre_id char(36) NOT NULL,
  designe_par_id char(36) NOT NULL,
  specialite varchar(200) DEFAULT NULL,
  actif tinyint(1) NOT NULL DEFAULT 1,
  date_designation datetime NOT NULL,
  date_fin_habilitation date DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_evaluateur_centre (citoyen_id,centre_id),
  KEY fk_evaluateur_centre_centre (centre_id),
  KEY fk_evaluateur_centre_designe_par (designe_par_id),
  CONSTRAINT fk_evaluateur_centre_centre FOREIGN KEY (centre_id) REFERENCES centre (id),
  CONSTRAINT fk_evaluateur_centre_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_evaluateur_centre_designe_par FOREIGN KEY (designe_par_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE evaluateur_habilitation (
  id char(36) NOT NULL,
  evaluateur_centre_id char(36) NOT NULL,
  competence_id char(36) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_evaluateur_habilitation (evaluateur_centre_id,competence_id),
  KEY fk_habilitation_competence (competence_id),
  CONSTRAINT fk_habilitation_competence FOREIGN KEY (competence_id) REFERENCES competence (id),
  CONSTRAINT fk_habilitation_evaluateur FOREIGN KEY (evaluateur_centre_id) REFERENCES evaluateur_centre (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE preuve (
  id char(36) NOT NULL,
  citoyen_competence_id char(36) NOT NULL,
  type varchar(40) NOT NULL,
  titre varchar(200) NOT NULL,
  description text DEFAULT NULL,
  fichier_url varchar(500) DEFAULT NULL,
  date_depot datetime NOT NULL,
  PRIMARY KEY (id),
  KEY fk_preuve_citoyen_competence (citoyen_competence_id),
  CONSTRAINT fk_preuve_citoyen_competence FOREIGN KEY (citoyen_competence_id) REFERENCES citoyen_competence (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE validation (
  id char(36) NOT NULL,
  citoyen_competence_id char(36) NOT NULL,
  preuve_id char(36) DEFAULT NULL,
  validee_par_id char(36) NOT NULL,
  centre_id char(36) DEFAULT NULL,
  type varchar(40) NOT NULL,
  statut varchar(30) NOT NULL,
  date_validation datetime DEFAULT NULL,
  commentaire text DEFAULT NULL,
  date_expiration date DEFAULT NULL,
  date_demande datetime NOT NULL,
  niveau_valide varchar(30) DEFAULT NULL,
  decision_corrigee_id char(36) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_validation_citoyen_competence (citoyen_competence_id),
  KEY fk_validation_preuve (preuve_id),
  KEY fk_validation_utilisateur (validee_par_id),
  KEY fk_validation_centre (centre_id),
  KEY fk_validation_corrigee (decision_corrigee_id),
  CONSTRAINT fk_validation_centre FOREIGN KEY (centre_id) REFERENCES centre (id),
  CONSTRAINT fk_validation_citoyen_competence FOREIGN KEY (citoyen_competence_id) REFERENCES citoyen_competence (id),
  CONSTRAINT fk_validation_corrigee FOREIGN KEY (decision_corrigee_id) REFERENCES validation (id),
  CONSTRAINT fk_validation_preuve FOREIGN KEY (preuve_id) REFERENCES preuve (id),
  CONSTRAINT fk_validation_utilisateur FOREIGN KEY (validee_par_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contestation (
  id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  type_objet varchar(30) NOT NULL,
  validation_id char(36) DEFAULT NULL,
  champ_concerne varchar(60) DEFAULT NULL,
  motif text NOT NULL,
  statut varchar(20) NOT NULL DEFAULT 'OUVERTE',
  reponse text DEFAULT NULL,
  traitee_par_id char(36) DEFAULT NULL,
  nouvelle_decision_id char(36) DEFAULT NULL,
  date_creation datetime NOT NULL,
  date_traitement datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_contestation_validation (validation_id),
  KEY fk_contestation_traitee_par (traitee_par_id),
  KEY fk_contestation_nouvelle_decision (nouvelle_decision_id),
  KEY idx_contestation_statut (statut,date_creation),
  KEY idx_contestation_citoyen (citoyen_id),
  CONSTRAINT fk_contestation_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_contestation_nouvelle_decision FOREIGN KEY (nouvelle_decision_id) REFERENCES validation (id),
  CONSTRAINT fk_contestation_traitee_par FOREIGN KEY (traitee_par_id) REFERENCES utilisateur (id),
  CONSTRAINT fk_contestation_validation FOREIGN KEY (validation_id) REFERENCES validation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 7. TESTS NUMÉRIQUES
-- ============================================================

CREATE TABLE test_numerique (
  id char(36) NOT NULL,
  competence_id char(36) NOT NULL,
  titre varchar(200) NOT NULL,
  description text DEFAULT NULL,
  actif tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  KEY fk_test_numerique_competence (competence_id),
  CONSTRAINT fk_test_numerique_competence FOREIGN KEY (competence_id) REFERENCES competence (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE question (
  id char(36) NOT NULL,
  test_id char(36) NOT NULL,
  texte text NOT NULL,
  type varchar(30) NOT NULL,
  ordre int(11) NOT NULL,
  PRIMARY KEY (id),
  KEY fk_question_test (test_id),
  CONSTRAINT fk_question_test FOREIGN KEY (test_id) REFERENCES test_numerique (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proposition_reponse (
  id char(36) NOT NULL,
  question_id char(36) NOT NULL,
  texte text NOT NULL,
  correcte tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY fk_proposition_reponse_question (question_id),
  CONSTRAINT fk_proposition_reponse_question FOREIGN KEY (question_id) REFERENCES question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE resultat_test (
  id char(36) NOT NULL,
  test_id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  score decimal(5,2) NOT NULL,
  reussi tinyint(1) NOT NULL,
  date_passage datetime NOT NULL,
  preuve_id char(36) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_resultat_test_preuve (preuve_id),
  KEY fk_resultat_test_test (test_id),
  KEY fk_resultat_test_citoyen (citoyen_id),
  CONSTRAINT fk_resultat_test_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_resultat_test_preuve FOREIGN KEY (preuve_id) REFERENCES preuve (id),
  CONSTRAINT fk_resultat_test_test FOREIGN KEY (test_id) REFERENCES test_numerique (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 8. OPPORTUNITÉS, NOTIFICATIONS ET INDICATEURS
-- ============================================================

CREATE TABLE categorie_opportunite (
  id char(36) NOT NULL,
  nom varchar(150) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categorie_opportunite_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE opportunite (
  id char(36) NOT NULL,
  categorie_id char(36) NOT NULL,
  titre varchar(200) NOT NULL,
  description text NOT NULL,
  statut varchar(30) NOT NULL,
  type varchar(40) NOT NULL,
  date_publication datetime DEFAULT NULL,
  date_expiration date DEFAULT NULL,
  region_id char(36) DEFAULT NULL,
  commune_id char(36) DEFAULT NULL,
  date_diffusion datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_opportunite_categorie (categorie_id),
  KEY fk_opportunite_region (region_id),
  KEY fk_opportunite_commune (commune_id),
  CONSTRAINT fk_opportunite_categorie FOREIGN KEY (categorie_id) REFERENCES categorie_opportunite (id),
  CONSTRAINT fk_opportunite_commune FOREIGN KEY (commune_id) REFERENCES commune (id),
  CONSTRAINT fk_opportunite_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE candidature_opportunite (
  id char(36) NOT NULL,
  opportunite_id char(36) NOT NULL,
  citoyen_id char(36) NOT NULL,
  message text DEFAULT NULL,
  statut varchar(20) NOT NULL,
  locale tinyint(1) NOT NULL DEFAULT 0,
  date_candidature datetime NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_candidature_opportunite_citoyen (opportunite_id,citoyen_id),
  KEY fk_candidature_citoyen (citoyen_id),
  CONSTRAINT fk_candidature_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen (id),
  CONSTRAINT fk_candidature_opportunite FOREIGN KEY (opportunite_id) REFERENCES opportunite (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification (
  id char(36) NOT NULL,
  titre varchar(200) NOT NULL,
  message text NOT NULL,
  type varchar(40) NOT NULL,
  date_creation datetime NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification_destinataire (
  id char(36) NOT NULL,
  notification_id char(36) NOT NULL,
  utilisateur_id char(36) NOT NULL,
  lu tinyint(1) NOT NULL DEFAULT 0,
  date_reception datetime NOT NULL,
  date_lecture datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_destinataire (notification_id,utilisateur_id),
  KEY fk_notification_destinataire_utilisateur (utilisateur_id),
  CONSTRAINT fk_notification_destinataire_notification FOREIGN KEY (notification_id) REFERENCES notification (id),
  CONSTRAINT fk_notification_destinataire_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE indicateur_skills_gap (
  id char(36) NOT NULL,
  region_id char(36) NOT NULL,
  competence_id char(36) NOT NULL,
  periode_debut date NOT NULL,
  periode_fin date NOT NULL,
  date_calcul datetime NOT NULL,
  demande_estimee int(11) NOT NULL,
  offre_disponible int(11) NOT NULL,
  ecart int(11) NOT NULL,
  effectif_contributeurs int(11) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_skills_gap_region (region_id),
  KEY idx_skills_gap_cohort (competence_id,periode_debut,periode_fin),
  CONSTRAINT fk_skills_gap_competence FOREIGN KEY (competence_id) REFERENCES competence (id),
  CONSTRAINT fk_skills_gap_region FOREIGN KEY (region_id) REFERENCES region (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
