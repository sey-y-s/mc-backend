package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Renouvellement de session avec rotation : chaque jeton ne sert qu'une fois et est remplacé par un
 * nouveau. Si un jeton déjà utilisé est présenté de nouveau (copie volée), toute la chaîne est coupée.
 */
@Service
public class RefreshTokenService {

    /** Deux requêtes simultanées avec le même jeton (double appel du client) ne coupent pas la session. */
    private static final long TOLERANCE_SECONDES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JetonRenouvellementRepository repository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final long validiteJours;

    public RefreshTokenService(JetonRenouvellementRepository repository,
                               UtilisateurRepository utilisateurRepository,
                               JwtService jwtService,
                               @Value("${app.security.jwt.refresh-expiration-days:30}") long validiteJours) {
        this.repository = repository;
        this.utilisateurRepository = utilisateurRepository;
        this.jwtService = jwtService;
        this.validiteJours = validiteJours;
    }

    /** Ouvre une nouvelle chaîne de renouvellement (connexion ou inscription) et renvoie le jeton en clair. */
    @Transactional
    public String emettre(UUID utilisateurId) {
        repository.supprimerExpires(LocalDateTime.now().minusDays(1));
        return creer(utilisateurId, UUID.randomUUID());
    }

    /** Échange un jeton valide contre un nouvel accès et un nouveau jeton de renouvellement. */
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public LoginResponse renouveler(String jetonBrut) {
        JetonRenouvellement jeton = repository.findByEmpreinte(empreinte(jetonBrut))
                .orElseThrow(RefreshTokenService::refuse);

        LocalDateTime maintenant = LocalDateTime.now();
        if (jeton.isRevoque() || jeton.getExpireLe().isBefore(maintenant)) {
            throw refuse();
        }
        if (jeton.getUtiliseLe() != null) {
            // Rejeu : soit un double appel très rapproché (on refuse sans punir), soit un vol (on coupe tout).
            if (jeton.getUtiliseLe().isBefore(maintenant.minusSeconds(TOLERANCE_SECONDES))) {
                repository.revoquerFamille(jeton.getFamilleId());
            }
            throw refuse();
        }

        Utilisateur utilisateur = utilisateurRepository.findById(jeton.getUtilisateurId())
                .filter(Utilisateur::isActif)
                .orElseThrow(() -> {
                    repository.revoquerFamille(jeton.getFamilleId());
                    return refuse();
                });

        jeton.setUtiliseLe(maintenant);
        repository.save(jeton);
        String suivant = creer(utilisateur.getId(), jeton.getFamilleId());
        return new LoginResponse(jwtService.generateToken(utilisateur), suivant, jwtService.getExpiration());
    }

    /** Déconnexion : le jeton fourni et toute sa chaîne ne sont plus acceptés. */
    @Transactional
    public void revoquer(String jetonBrut) {
        if (jetonBrut == null || jetonBrut.isBlank()) {
            return;
        }
        repository.findByEmpreinte(empreinte(jetonBrut))
                .ifPresent(j -> repository.revoquerFamille(j.getFamilleId()));
    }

    /** Déconnecte tous les appareils du compte. */
    @Transactional
    public void revoquerTous(UUID utilisateurId) {
        repository.revoquerTous(utilisateurId);
    }

    private String creer(UUID utilisateurId, UUID familleId) {
        byte[] octets = new byte[32];
        RANDOM.nextBytes(octets);
        String brut = Base64.getUrlEncoder().withoutPadding().encodeToString(octets);

        JetonRenouvellement jeton = new JetonRenouvellement();
        jeton.setUtilisateurId(utilisateurId);
        jeton.setFamilleId(familleId);
        jeton.setEmpreinte(empreinte(brut));
        jeton.setCreeLe(LocalDateTime.now());
        jeton.setExpireLe(LocalDateTime.now().plusDays(validiteJours));
        repository.save(jeton);
        return brut;
    }

    static String empreinte(String brut) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(brut.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ResponseStatusException refuse() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expirée. Reconnectez-vous.");
    }
}
