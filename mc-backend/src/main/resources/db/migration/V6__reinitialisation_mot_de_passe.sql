CREATE TABLE reset_password_token (
    id             CHAR(36) NOT NULL,
    utilisateur_id CHAR(36) NOT NULL,
    code_hash      VARCHAR(100) NOT NULL,
    expire_le      DATETIME NOT NULL,
    utilise        BOOLEAN NOT NULL DEFAULT FALSE,
    tentatives     INT NOT NULL DEFAULT 0,
    date_creation  DATETIME NOT NULL,

    CONSTRAINT pk_reset_password_token PRIMARY KEY (id),
    CONSTRAINT fk_reset_token_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

CREATE INDEX idx_reset_token_utilisateur ON reset_password_token(utilisateur_id);
