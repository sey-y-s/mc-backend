package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.centre.CentreAdministrateurRepository;
import odk.malicompetences.backend.common.exception.MetierException;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.otp.EnvoiCodeVerification;
import odk.malicompetences.backend.common.otp.GenerateurCodeOtp;
import odk.malicompetences.backend.common.otp.PolitiqueOtp;
import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.common.statut.CanalVerification;
import odk.malicompetences.backend.common.statut.StatutCompte;
import odk.malicompetences.backend.common.statut.UsageCodeVerification;
import odk.malicompetences.backend.common.validation.ContactCompte;
import odk.malicompetences.backend.common.validation.NumeroTelephone;
import odk.malicompetences.backend.utilisateur.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final CentreAdministrateurRepository centreAdministrateurRepository;
    private final CodeVerificationRepository codeVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EnvoiCodeVerification envoiCodeVerification;
    private final SessionRenouvellementRepository sessionRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_TENTATIVES_CODE = 5;

    @Value("${app.otp.validite-minutes:10}")
    private int otpValiditeMinutes;

    @Value("${app.otp.max-tentatives:5}")
    private int otpMaxTentatives;

    @Value("${app.security.refresh.expiration-ms:2592000000}")
    private long refreshExpirationMs;

    @Value("${app.otp.delai-renvoi-secondes:60}")
    private long otpCooldownSeconds;

    public AuthService(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            CentreAdministrateurRepository centreAdministrateurRepository,
            CodeVerificationRepository codeVerificationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EnvoiCodeVerification envoiCodeVerification,
            SessionRenouvellementRepository sessionRepository
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.centreAdministrateurRepository = centreAdministrateurRepository;
        this.codeVerificationRepository = codeVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.envoiCodeVerification = envoiCodeVerification;
        this.sessionRepository = sessionRepository;
    }

    public RegisterResponse inscrire(RegisterRequest request) {
        if (request.conditionsAcceptees() == null || !request.conditionsAcceptees()) {
            throw new IllegalArgumentException("L'acceptation des conditions est obligatoire");
        }

        Role role = request.role();
        if (role == null || (role != Role.CITOYEN && role != Role.ORGANISATION)) {
            throw new IllegalArgumentException("Seuls les rôles CITOYEN et ORGANISATION sont autorisés à l'inscription");
        }

        ContactCompte contact = ContactCompte.normaliser(request.telephone(), request.email());
        String telephone = contact.telephone();
        String email = contact.email();

        if (telephone != null && utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé");
        }
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cette adresse email est déjà utilisée");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);
        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.password()));
        utilisateur.setStatut(StatutCompte.EN_ATTENTE);
        utilisateur.setConditionsAccepteesLe(LocalDateTime.now());

        RoleEntity roleEntity = roleRepository.findByCode(role.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le rôle '" + role.name() + "' est absent du référentiel initial. Vérifiez la migration V1."));
        utilisateur.getRoles().add(roleEntity);

        Utilisateur compteCree = utilisateurRepository.save(utilisateur);

        String codeClair = GenerateurCodeOtp.generer();
        String codeHash = passwordEncoder.encode(codeClair);
        CanalVerification canal = (telephone != null) ? CanalVerification.TELEPHONE : CanalVerification.EMAIL;
        String destinataire = (canal == CanalVerification.TELEPHONE) ? telephone : email;

        CodeVerification codeVerif = new CodeVerification();
        codeVerif.setUtilisateurId(compteCree.getId());
        codeVerif.setCanal(canal);
        codeVerif.setDestinataire(destinataire);
        codeVerif.setUsageCode(UsageCodeVerification.INSCRIPTION);
        codeVerif.setCodeHash(codeHash);
        codeVerif.setExpireLe(LocalDateTime.now().plusMinutes(otpValiditeMinutes));
        codeVerif.setTentatives(0);
        codeVerificationRepository.save(codeVerif);

        envoiCodeVerification.envoyer(canal, destinataire, UsageCodeVerification.INSCRIPTION, codeClair);

        return new RegisterResponse(
                compteCree.getId(),
                "Compte créé avec succès. Un code de vérification vous a été envoyé.",
                compteCree.getStatut(),
                canal.name(),
                destinataire
        );
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public LoginResponse verifyOtp(VerifyOtpRequest request) {
        if (request.usage() != UsageCodeVerification.INSCRIPTION) {
            throw new IllegalArgumentException("OTP_USAGE_INVALIDE");
        }

        String destinataire = identifiantNormalise(request.destinataire());
        CodeVerification codeVerif = codeVerificationRepository
                .findTopByDestinataireAndUsageCodeOrderByCreeLeDesc(destinataire, UsageCodeVerification.INSCRIPTION)
                .orElseThrow(() -> new IllegalArgumentException("Aucun code de vérification trouvé pour ce destinataire"));

        Utilisateur utilisateur = codeVerif.getUtilisateurId() == null
                ? utilisateurRepository.findByTelephone(destinataire)
                    .or(() -> utilisateurRepository.findByEmail(destinataire))
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur associé introuvable"))
                : utilisateurRepository.findById(codeVerif.getUtilisateurId())
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur associé introuvable"));

        if (utilisateur.getStatut() != StatutCompte.EN_ATTENTE) {
            throw new IllegalStateException("Le compte doit être EN_ATTENTE pour confirmer l'inscription");
        }

        // L'état est évalué avec le compteur précédent : la cinquième tentative reste possible.
        PolitiqueOtp.Etat etat = PolitiqueOtp.etat(
                codeVerif.getExpireLe(),
                codeVerif.getUtiliseLe(),
                codeVerif.getTentatives(),
                otpMaxTentatives,
                LocalDateTime.now()
        );
        codeVerif.setTentatives(codeVerif.getTentatives() + 1);
        codeVerificationRepository.save(codeVerif);

        if (etat == PolitiqueOtp.Etat.DEJA_UTILISE) {
            throw new IllegalStateException("Ce code a déjà été utilisé");
        }
        if (etat == PolitiqueOtp.Etat.EXPIRE) {
            throw new IllegalStateException("Ce code a expiré");
        }
        if (etat == PolitiqueOtp.Etat.TROP_DE_TENTATIVES) {
            throw new IllegalStateException("Nombre maximal de tentatives dépassé");
        }

        if (!passwordEncoder.matches(request.code(), codeVerif.getCodeHash())) {
            throw new IllegalArgumentException("Code de vérification incorrect");
        }

        codeVerif.setUtiliseLe(LocalDateTime.now());
        codeVerificationRepository.save(codeVerif);
        utilisateur.setStatut(StatutCompte.ACTIF);
        if (codeVerif.getCanal() == CanalVerification.TELEPHONE) {
            utilisateur.setTelephoneVerifieLe(LocalDateTime.now());
        } else {
            utilisateur.setEmailVerifieLe(LocalDateTime.now());
        }
        utilisateurRepository.save(utilisateur);

        return genererLoginResponse(utilisateur);
    }

    public void resendOtp(ResendOtpRequest request) {
        String destinataire = identifiantNormalise(request.destinataire());
        Optional<Utilisateur> trouve = chercherParIdentifiant(destinataire);
        if (trouve.isEmpty()) return;

        Utilisateur utilisateur = trouve.get();
        boolean autorise = switch (request.usage()) {
            case INSCRIPTION -> utilisateur.getStatut() == StatutCompte.EN_ATTENTE;
            case RECUPERATION_MOT_DE_PASSE -> utilisateur.getStatut() == StatutCompte.ACTIF;
            case CHANGEMENT_CONTACT -> false;
        };
        if (!autorise) return;

        boolean cooldown = codeVerificationRepository
                .findTopByDestinataireAndUsageCodeOrderByCreeLeDesc(destinataire, request.usage())
                .filter(code -> code.getCreeLe() != null
                        && code.getCreeLe().plusSeconds(otpCooldownSeconds).isAfter(LocalDateTime.now()))
                .isPresent();
        if (cooldown) return;

        CanalVerification canal = destinataire.contains("@") ? CanalVerification.EMAIL : CanalVerification.TELEPHONE;
        String codeClair = GenerateurCodeOtp.generer();
        CodeVerification codeVerif = new CodeVerification();
        codeVerif.setUtilisateurId(utilisateur.getId());
        codeVerif.setCanal(canal);
        codeVerif.setDestinataire(destinataire);
        codeVerif.setUsageCode(request.usage());
        codeVerif.setCodeHash(passwordEncoder.encode(codeClair));
        codeVerif.setExpireLe(LocalDateTime.now().plusMinutes(otpValiditeMinutes));
        codeVerif.setTentatives(0);
        codeVerificationRepository.save(codeVerif);
        envoiCodeVerification.envoyer(canal, destinataire, request.usage(), codeClair);
    }

    public LoginResponse login(LoginRequest request) {
        String identifiant = identifiantNormalise(request.identifiant());

        String finalIdentifiant = identifiant;
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(identifiant)
                .or(() -> utilisateurRepository.findByEmail(finalIdentifiant))
                .orElseThrow(() -> new IllegalArgumentException("Identifiant ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.password(), utilisateur.getMotDePasseHash())) {
            throw new IllegalArgumentException("Identifiant ou mot de passe incorrect");
        }

        if (utilisateur.getStatut() != StatutCompte.ACTIF) {
            throw new MetierException("COMPTE_NON_VERIFIE", "Le compte n'est pas actif (statut : " + utilisateur.getStatut() + ")", HttpStatus.FORBIDDEN);
        }

        return genererLoginResponse(utilisateur);
    }

    public LoginResponse genererLoginResponse(Utilisateur utilisateur) {
        Set<String> roleCodes = utilisateur.getRoles().stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toSet());
        if (centreAdministrateurRepository.existsById(utilisateur.getId())) {
            roleCodes.add("CENTRE_ADMIN");
        }

        String token = jwtService.generateToken(utilisateur, roleCodes);
        String refreshToken = creerSessionRenouvellement(utilisateur.getId());
        return new LoginResponse(
                token,
                token,
                refreshToken,
                jwtService.getExpiration(),
                new LoginResponse.UtilisateurSession(
                        utilisateur.getId(),
                        utilisateur.getTelephone(),
                        utilisateur.getEmail(),
                        utilisateur.getStatut(),
                        roleCodes
                )
        );
    }


    public LoginResponse renouvelerSession(RefreshTokenRequest request) {
        String hash = hashJeton(request.refreshToken());
        SessionRenouvellement session = sessionRepository.findByJetonHash(hash)
                .orElseThrow(() -> new MetierException("REFRESH_TOKEN_INVALIDE", "La session a expiré ou a été révoquée.", HttpStatus.UNAUTHORIZED));
        LocalDateTime maintenant = LocalDateTime.now();
        if (session.getRevoqueLe() != null || !session.getExpireLe().isAfter(maintenant)) {
            throw new MetierException("REFRESH_TOKEN_INVALIDE", "La session a expiré ou a été révoquée.", HttpStatus.UNAUTHORIZED);
        }
        Utilisateur utilisateur = utilisateurRepository.findById(session.getUtilisateurId())
                .orElseThrow(() -> new MetierException("COMPTE_INTROUVABLE", "Compte introuvable.", HttpStatus.UNAUTHORIZED));
        if (utilisateur.getStatut() != StatutCompte.ACTIF) {
            session.setRevoqueLe(maintenant);
            sessionRepository.save(session);
            throw new MetierException("COMPTE_NON_ACTIF", "Le compte n'est pas actif.", HttpStatus.FORBIDDEN);
        }
        session.setRevoqueLe(maintenant);
        sessionRepository.save(session);
        return genererLoginResponse(utilisateur);
    }

    public void deconnecter(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        sessionRepository.findByJetonHash(hashJeton(refreshToken.trim())).ifPresent(session -> {
            if (session.getRevoqueLe() == null) {
                session.setRevoqueLe(LocalDateTime.now());
                sessionRepository.save(session);
            }
        });
    }

    private String creerSessionRenouvellement(UUID utilisateurId) {
        byte[] alea = new byte[48];
        RANDOM.nextBytes(alea);
        String jeton = Base64.getUrlEncoder().withoutPadding().encodeToString(alea);
        SessionRenouvellement session = new SessionRenouvellement();
        session.setUtilisateurId(utilisateurId);
        session.setJetonHash(hashJeton(jeton));
        session.setExpireLe(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        sessionRepository.save(session);
        return jeton;
    }

    private static String hashJeton(String jeton) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(jeton.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private String identifiantNormalise(String valeur) {
        String saisie = valeur == null ? "" : valeur.trim();
        if (saisie.contains("@")) return saisie.toLowerCase(Locale.ROOT);
        try {
            return NumeroTelephone.normaliser(saisie);
        } catch (Exception e) {
            return saisie;
        }
    }

    // --- Méthodes conservées pour forgot / reset password existants ---

    public void changerMotDePasse(
            UUID authenticatedUserId,
            ChangementMotDePasseRequest request
    ) {
        Utilisateur utilisateur = utilisateurRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        if (!passwordEncoder.matches(
                request.ancienMotDePasse(), utilisateur.getMotDePasseHash()
        )) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }
        if (passwordEncoder.matches(
                request.nouveauMotDePasse(), utilisateur.getMotDePasseHash()
        )) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent");
        }
        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
        sessionRepository.revoquerToutes(authenticatedUserId, LocalDateTime.now());
    }

    public void demanderReinitialisation(ForgotPasswordRequest request) {
        String identifiant = identifiantNormalise(request.identifiant());
        Optional<Utilisateur> trouve = chercherParIdentifiant(identifiant);
        if (trouve.isEmpty() || trouve.get().getStatut() != StatutCompte.ACTIF) return;

        Utilisateur utilisateur = trouve.get();
        String destinataire = utilisateur.getTelephone() != null
                ? utilisateur.getTelephone() : utilisateur.getEmail();
        CanalVerification canal = utilisateur.getTelephone() != null
                ? CanalVerification.TELEPHONE : CanalVerification.EMAIL;

        boolean cooldown = codeVerificationRepository.findTopByDestinataireAndUsageCodeOrderByCreeLeDesc(
                        destinataire, UsageCodeVerification.RECUPERATION_MOT_DE_PASSE)
                .filter(code -> code.getCreeLe() != null
                        && code.getCreeLe().plusSeconds(otpCooldownSeconds).isAfter(LocalDateTime.now()))
                .isPresent();
        if (cooldown) return;

        String codeClair = GenerateurCodeOtp.generer();
        CodeVerification code = new CodeVerification();
        code.setUtilisateurId(utilisateur.getId());
        code.setCanal(canal);
        code.setDestinataire(destinataire);
        code.setUsageCode(UsageCodeVerification.RECUPERATION_MOT_DE_PASSE);
        code.setCodeHash(passwordEncoder.encode(codeClair));
        code.setExpireLe(LocalDateTime.now().plusMinutes(otpValiditeMinutes));
        code.setTentatives(0);
        codeVerificationRepository.save(code);
        envoiCodeVerification.envoyer(canal, destinataire, UsageCodeVerification.RECUPERATION_MOT_DE_PASSE, codeClair);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void reinitialiserMotDePasse(ResetPasswordRequest request) {
        String destinataire = identifiantNormalise(request.identifiant());
        CodeVerification code = codeVerificationRepository
                .findTopByDestinataireAndUsageCodeOrderByCreeLeDesc(
                        destinataire, UsageCodeVerification.RECUPERATION_MOT_DE_PASSE)
                .orElseThrow(() -> new IllegalArgumentException("Code ou identifiant invalide"));

        // Le contrôle porte sur le compteur avant l'essai courant pour autoriser cinq comparaisons.
        PolitiqueOtp.Etat etat = PolitiqueOtp.etat(code.getExpireLe(), code.getUtiliseLe(),
                code.getTentatives(), otpMaxTentatives, LocalDateTime.now());
        code.setTentatives(code.getTentatives() + 1);
        codeVerificationRepository.save(code);
        if (etat != PolitiqueOtp.Etat.UTILISABLE) {
            throw new IllegalArgumentException("Code ou identifiant invalide, expiré ou déjà utilisé");
        }
        if (!passwordEncoder.matches(request.code(), code.getCodeHash())) {
            throw new IllegalArgumentException("Code ou identifiant invalide");
        }

        Utilisateur utilisateur = code.getUtilisateurId() == null
                ? chercherParIdentifiant(destinataire).orElseThrow(() -> new IllegalArgumentException("Code ou identifiant invalide"))
                : utilisateurRepository.findById(code.getUtilisateurId()).orElseThrow(() -> new IllegalArgumentException("Code ou identifiant invalide"));
        if (utilisateur.getStatut() != StatutCompte.ACTIF) {
            throw new IllegalArgumentException("Code ou identifiant invalide");
        }
        code.setUtiliseLe(LocalDateTime.now());
        codeVerificationRepository.save(code);
        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
        sessionRepository.revoquerToutes(utilisateur.getId(), LocalDateTime.now());
    }

    private Optional<Utilisateur> chercherParIdentifiant(String identifiant) {
        String normalise = identifiantNormalise(identifiant);
        return utilisateurRepository.findByTelephone(normalise)
                .or(() -> utilisateurRepository.findByEmail(normalise));
    }

}
