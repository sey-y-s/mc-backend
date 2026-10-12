CREATE TABLE preferences_utilisateur (
    utilisateur_id CHAR(36) NOT NULL,
    low_data_mode BOOLEAN NOT NULL DEFAULT FALSE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_preferences_utilisateur PRIMARY KEY (utilisateur_id),
    CONSTRAINT fk_preferences_utilisateur_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
);
