package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.otp.PolitiqueOtp;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Émission et contrôle des codes de vérification du téléphone et de l'email. */
@Service
@Transactional
public class VerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DELAI_RENVOI_SECONDES = 60;

    private final CodeVerificationRepository repository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnvoiCodeOtp envoi;
    private final int validiteMinutes;
    private final int maxTentatives;

    public VerificationService(CodeVerificationRepository repository,
                               UtilisateurRepository utilisateurRepository,
                               PasswordEncoder passwordEncoder,
                               EnvoiCodeOtp envoi,
                               @Value("${app.otp.validite-minutes:10}") int validiteMinutes,
                               @Value("${app.otp.max-tentatives:5}") int maxTentatives) {
        this.repository = repository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.envoi = envoi;
        this.validiteMinutes = validiteMinutes;
        this.maxTentatives = maxTentatives;
    }

    /** Contacts à vérifier pour ce compte : toujours le téléphone, et l'email s'il a été renseigné. */
    public List<CanalVerification> canauxDisponibles(Utilisateur utilisateur) {
        List<CanalVerification> canaux = new ArrayList<>();
        canaux.add(CanalVerification.TELEPHONE);
        if (utilisateur.getEmail() != null && !utilisateur.getEmail().isBlank()) {
            canaux.add(CanalVerification.EMAIL);
        }
        return canaux;
    }

    /** Envoie un code sur chaque contact du compte (appelé à l'inscription). */
    public List<CanalVerification> envoyerTous(Utilisateur utilisateur) {
        List<CanalVerification> canaux = canauxDisponibles(utilisateur);
        canaux.forEach(canal -> emettre(utilisateur, canal));
        return canaux;
    }

    /** Renvoi d'un code : refusé si le précédent date de moins d'une minute ou si le contact est déjà vérifié. */
    public void renvoyer(ResendOtpRequest request) {
        Optional<Utilisateur> trouve = AuthIdentifiant.chercher(utilisateurRepository, request.identifiant());
        // Réponse identique que le compte existe ou non (pas de fuite d'information).
        if (trouve.isEmpty() || !trouve.get().isActif()) {
            return;
        }
        Utilisateur utilisateur = trouve.get();
        if (!canauxDisponibles(utilisateur).contains(request.canal()) || estVerifie(utilisateur, request.canal())) {
            return;
        }
        repository.findFirstByUtilisateurIdAndCanalOrderByCreeLeDesc(utilisateur.getId(), request.canal())
                .filter(dernier -> dernier.getCreeLe().plusSeconds(DELAI_RENVOI_SECONDES).isAfter(LocalDateTime.now()))
                .ifPresent(dernier -> {
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                            "Patientez une minute avant de demander un nouveau code.");
                });
        emettre(utilisateur, request.canal());
    }

    /** Contrôle le code ; en cas de succès le contact est marqué vérifié et le compte est ouvert. */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public Utilisateur verifier(VerifyOtpRequest request) {
        Utilisateur utilisateur = AuthIdentifiant.chercher(utilisateurRepository, request.identifiant())
                .filter(Utilisateur::isActif)
                .orElseThrow(VerificationService::codeInvalide);
        CodeVerification code = repository
                .findFirstByUtilisateurIdAndCanalAndUtiliseLeIsNullOrderByCreeLeDesc(utilisateur.getId(), request.canal())
                .orElseThrow(VerificationService::codeInvalide);

        LocalDateTime maintenant = LocalDateTime.now();
        PolitiqueOtp.Etat etat = PolitiqueOtp.etat(code.getExpireLe(), code.getUtiliseLe(), code.getTentatives(),
                maxTentatives, maintenant);
        // Toute tentative est comptée avant la comparaison, pour empêcher l'essai en rafale.
        code.setTentatives(code.getTentatives() + 1);
        repository.save(code);
        if (etat != PolitiqueOtp.Etat.UTILISABLE
                || !passwordEncoder.matches(request.code().trim(), code.getCodeHash())) {
            throw codeInvalide();
        }

        code.setUtiliseLe(maintenant);
        repository.save(code);
        marquerVerifie(utilisateur, request.canal(), maintenant);
        return utilisateurRepository.save(utilisateur);
    }

    /** Un code de réinitialisation du mot de passe prouve aussi la possession du contact utilisé. */
    public void marquerVerifieParIdentifiant(Utilisateur utilisateur, String identifiantSaisi) {
        String saisie = identifiantSaisi == null ? "" : identifiantSaisi.trim();
        CanalVerification canal = saisie.contains("@") ? CanalVerification.EMAIL : CanalVerification.TELEPHONE;
        marquerVerifie(utilisateur, canal, LocalDateTime.now());
        utilisateurRepository.save(utilisateur);
    }

    private void emettre(Utilisateur utilisateur, CanalVerification canal) {
        LocalDateTime maintenant = LocalDateTime.now();
        repository.invaliderTous(utilisateur.getId(), canal, maintenant);
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        CodeVerification entite = new CodeVerification();
        entite.setUtilisateurId(utilisateur.getId());
        entite.setCanal(canal);
        entite.setCodeHash(passwordEncoder.encode(code));
        entite.setExpireLe(maintenant.plusMinutes(validiteMinutes));
        repository.save(entite);
        envoi.envoyer(utilisateur, canal, code);
    }

    private static boolean estVerifie(Utilisateur utilisateur, CanalVerification canal) {
        return canal == CanalVerification.EMAIL
                ? utilisateur.getEmailVerifieLe() != null
                : utilisateur.getTelephoneVerifieLe() != null;
    }

    private static void marquerVerifie(Utilisateur utilisateur, CanalVerification canal, LocalDateTime date) {
        if (canal == CanalVerification.EMAIL) {
            if (utilisateur.getEmailVerifieLe() == null) utilisateur.setEmailVerifieLe(date);
        } else if (utilisateur.getTelephoneVerifieLe() == null) {
            utilisateur.setTelephoneVerifieLe(date);
        }
    }

    private static IllegalArgumentException codeInvalide() {
        return new IllegalArgumentException("Code incorrect ou expiré");
    }
}
