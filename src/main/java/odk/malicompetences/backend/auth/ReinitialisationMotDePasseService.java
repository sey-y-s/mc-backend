package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/** « Mot de passe oublié » : émission du code (étape 1) puis contrôle du code et nouveau mot de passe (étape 2). */
@Service
@Transactional
public class ReinitialisationMotDePasseService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int VALIDITE_CODE_MINUTES = 15;
    private static final int MAX_TENTATIVES_CODE = 5;

    private final UtilisateurRepository utilisateurRepository;
    private final ResetPasswordTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeSender codeSender;
    private final VerificationService verificationService;

    public ReinitialisationMotDePasseService(UtilisateurRepository utilisateurRepository,
                                             ResetPasswordTokenRepository resetTokenRepository,
                                             PasswordEncoder passwordEncoder,
                                             CodeSender codeSender,
                                             VerificationService verificationService) {
        this.utilisateurRepository = utilisateurRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.codeSender = codeSender;
        this.verificationService = verificationService;
    }

    /** Étape 1 : ne révèle pas si le compte existe (aucune erreur dans les deux cas). */
    public void demander(ForgotPasswordRequest request) {
        Optional<Utilisateur> trouve = AuthIdentifiant.chercher(utilisateurRepository, request.identifiant());
        if (trouve.isEmpty() || !trouve.get().isActif()) {
            return;
        }
        Utilisateur utilisateur = trouve.get();
        resetTokenRepository.invaliderTous(utilisateur.getId());

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        ResetPasswordToken jeton = new ResetPasswordToken();
        jeton.setUtilisateurId(utilisateur.getId());
        jeton.setCodeHash(passwordEncoder.encode(code));
        jeton.setExpireLe(LocalDateTime.now().plusMinutes(VALIDITE_CODE_MINUTES));
        resetTokenRepository.save(jeton);

        codeSender.envoyer(utilisateur, code);
    }

    /** Étape 2 : vérifie le code (avec compteur d'essais) puis change le mot de passe. Retourne l'id du compte. */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public UUID reinitialiser(ResetPasswordRequest request) {
        Utilisateur utilisateur = AuthIdentifiant.chercher(utilisateurRepository, request.identifiant())
                .orElseThrow(ReinitialisationMotDePasseService::codeInvalide);
        ResetPasswordToken jeton = resetTokenRepository
                .findFirstByUtilisateurIdAndUtiliseFalseOrderByDateCreationDesc(utilisateur.getId())
                .orElseThrow(ReinitialisationMotDePasseService::codeInvalide);

        if (jeton.getExpireLe().isBefore(LocalDateTime.now()) || jeton.getTentatives() >= MAX_TENTATIVES_CODE) {
            throw codeInvalide();
        }
        if (!passwordEncoder.matches(request.code().trim(), jeton.getCodeHash())) {
            jeton.setTentatives(jeton.getTentatives() + 1);
            resetTokenRepository.save(jeton);
            throw codeInvalide();
        }

        utilisateur.setPasswordHash(passwordEncoder.encode(request.motDePasse()));
        utilisateurRepository.save(utilisateur);
        // Le code est parti sur ce contact : le recevoir prouve qu'il appartient bien à la personne.
        verificationService.marquerVerifieParIdentifiant(utilisateur, request.identifiant());
        jeton.setUtilise(true);
        resetTokenRepository.save(jeton);
        return utilisateur.getId();
    }

    private static IllegalArgumentException codeInvalide() {
        return new IllegalArgumentException("Code incorrect ou expiré");
    }
}
