package odk.malicompetences.backend.utilisateur;

public enum Role {
    CITOYEN,
    ORGANISATION,
    EVALUATEUR,
    CENTRE_ADMIN,
    ADMIN,
    SUPER_ADMIN,
    INSTITUTION,
    /** Agent de terrain : inscription assistée tracée (parcours complets prévus après la V1). */
    AGENT_TERRAIN
}