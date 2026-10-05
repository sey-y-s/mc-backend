CREATE TABLE test_numerique (
    id CHAR(36) NOT NULL,
    competence_id CHAR(36) NOT NULL,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_test_numerique PRIMARY KEY (id),
    CONSTRAINT fk_test_numerique_competence FOREIGN KEY (competence_id) REFERENCES competence(id)
);

CREATE TABLE question (
    id CHAR(36) NOT NULL,
    test_id CHAR(36) NOT NULL,
    texte TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    ordre INT NOT NULL,
    CONSTRAINT pk_question PRIMARY KEY (id),
    CONSTRAINT fk_question_test FOREIGN KEY (test_id) REFERENCES test_numerique(id)
);

CREATE TABLE proposition_reponse (
    id CHAR(36) NOT NULL,
    question_id CHAR(36) NOT NULL,
    texte TEXT NOT NULL,
    correcte BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_proposition_reponse PRIMARY KEY (id),
    CONSTRAINT fk_proposition_reponse_question FOREIGN KEY (question_id) REFERENCES question(id)
);

CREATE TABLE resultat_test (
    id CHAR(36) NOT NULL,
    test_id CHAR(36) NOT NULL,
    citoyen_id CHAR(36) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    reussi BOOLEAN NOT NULL,
    date_passage DATETIME NOT NULL,
    preuve_id CHAR(36),
    CONSTRAINT pk_resultat_test PRIMARY KEY (id),
    CONSTRAINT fk_resultat_test_test FOREIGN KEY (test_id) REFERENCES test_numerique(id),
    CONSTRAINT fk_resultat_test_citoyen FOREIGN KEY (citoyen_id) REFERENCES citoyen(id),
    CONSTRAINT fk_resultat_test_preuve FOREIGN KEY (preuve_id) REFERENCES preuve(id),
    CONSTRAINT uk_resultat_test_preuve UNIQUE (preuve_id)
);
