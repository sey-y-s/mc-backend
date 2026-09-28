//package odk.malicompetences.backend.common.security;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//import java.util.UUID;
//
//@Service
//public class JwtService {
//
//    private final SecretKey secretKey;
//    private final long expiration;
//
//    public JwtService(
//            @Value("${app.security.jwt.secret}") String secret,
//            @Value("${app.security.jwt.expiration}") long expiration
//    ) {
//        this.secretKey = Keys.hmacShaKeyFor(
//                secret.getBytes(StandardCharsets.UTF_8)
//        );
//        this.expiration = expiration;
//    }
//
//    public String generateToken(UUID utilisateurId) {
//
//        Date maintenant = new Date();
//
//        return Jwts.builder()
//                .subject(utilisateurId.toString())
//                .issuedAt(maintenant)
//                .expiration(
//                        new Date(
//                                maintenant.getTime() + expiration
//                        )
//                )
//                .signWith(secretKey)
//                .compact();
//    }
//
//    public UUID extractUtilisateurId(String token) {
//
//        String subject = Jwts.parser()
//                .verifyWith(secretKey)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
//
//        return UUID.fromString(subject);
//    }
//
//    public boolean isValid(String token) {
//        try {
//            Jwts.parser()
//                    .verifyWith(secretKey)
//                    .build()
//                    .parseSignedClaims(token);
//
//            return true;
//        } catch (JwtException | IllegalArgumentException exception) {
//            return false;
//        }
//    }
//}