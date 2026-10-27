package odk.malicompetences.backend.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    /** Fichier local (hors git) qui conserve la clé de développement entre deux redémarrages. */
    private static final String FICHIER_CLE_DEV = ".jwt-dev-secret";

    private final SecretKey secretKey;
    private final long expiration;

    /** Durée de vie du jeton d'accès, en millisecondes. */
    public long getExpiration() {
        return expiration;
    }

    /** Jetons déconnectés (identifiant du jeton -> date d'expiration). Mémoire du serveur : vidée au redémarrage. */
    private final Map<String, Long> jetonsRevoques = new ConcurrentHashMap<>();

    public JwtService(@Value("${app.security.jwt.secret}") String secret,
                      @Value("${app.security.jwt.expiration}") long expiration,
                      Environment environment) {
        if (secret == null || secret.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of("prod"))) {
                throw new IllegalStateException("JWT_SECRET est obligatoire en production (32 caractères minimum).");
            }
            this.secretKey = cleDeDeveloppement();
        } else if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET doit contenir au moins 32 octets");
        } else {
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        this.expiration = expiration;
    }

    /** Clé aléatoire enregistrée dans un fichier local : les sessions survivent aux redémarrages en développement. */
    private static SecretKey cleDeDeveloppement() {
        Path fichier = Path.of(FICHIER_CLE_DEV);
        try {
            if (Files.exists(fichier)) {
                byte[] existante = Base64.getDecoder().decode(Files.readString(fichier, StandardCharsets.UTF_8).trim());
                if (existante.length >= 32) {
                    return Keys.hmacShaKeyFor(existante);
                }
            }
            byte[] octets = new byte[32];
            new SecureRandom().nextBytes(octets);
            Files.writeString(fichier, Base64.getEncoder().encodeToString(octets), StandardCharsets.UTF_8);
            log.warn("JWT_SECRET non défini : clé de développement enregistrée dans {}. Définissez JWT_SECRET en production.", FICHIER_CLE_DEV);
            return Keys.hmacShaKeyFor(octets);
        } catch (IOException | IllegalArgumentException exception) {
            log.warn("JWT_SECRET non défini et fichier de clé inutilisable : clé temporaire (sessions perdues au redémarrage).");
            return Jwts.SIG.HS256.key().build();
        }
    }

    private odk.malicompetences.backend.utilisateur.RoleEffectif roleEffectif;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setRoleEffectif(odk.malicompetences.backend.utilisateur.RoleEffectif roleEffectif) {
        this.roleEffectif = roleEffectif;
    }

    public String generateToken(Utilisateur utilisateur) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(utilisateur.getId().toString())
                .claim("role", (roleEffectif == null ? utilisateur.getRole() : roleEffectif.de(utilisateur)).name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public UUID extractUtilisateurId(String token) {
        return UUID.fromString(Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getSubject());
    }

    public boolean isValid(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return claims.getId() == null || !jetonsRevoques.containsKey(claims.getId());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    /** Déconnexion : le jeton n'est plus accepté, même s'il n'a pas encore expiré. */
    public void revoquer(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            if (claims.getId() != null && claims.getExpiration() != null) {
                long maintenant = System.currentTimeMillis();
                jetonsRevoques.values().removeIf(fin -> fin < maintenant);
                jetonsRevoques.put(claims.getId(), claims.getExpiration().getTime());
            }
        } catch (JwtException | IllegalArgumentException exception) {
            // jeton déjà invalide : rien à révoquer
        }
    }
}
