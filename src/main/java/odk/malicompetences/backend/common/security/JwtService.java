package odk.malicompetences.backend.common.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(@Value("${app.security.jwt.secret}") String secret,
                      @Value("${app.security.jwt.expiration}") long expiration) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET doit contenir au moins 32 octets");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public long getExpiration() {
        return expiration;
    }

    public String generateToken(Utilisateur utilisateur) {
        Set<String> roles = new HashSet<>();
        if (utilisateur.getRoles() != null) {
            utilisateur.getRoles().forEach(r -> roles.add(r.getCode()));
        }
        return generateToken(utilisateur, roles);
    }

    public String generateToken(Utilisateur utilisateur, Collection<String> roles) {
        Date now = new Date();
        String primaryRole = (roles != null && !roles.isEmpty()) ? roles.iterator().next() : "";
        return Jwts.builder()
                .subject(utilisateur.getId().toString())
                .claim("roles", roles)
                .claim("role", primaryRole)
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
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
