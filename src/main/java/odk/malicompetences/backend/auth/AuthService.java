package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.Disponibilite;
import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.common.validation.NumeroTelephone;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenRepository citoyenRepository;
    private final OrganisationRepository organisationRepository;
    private final CommuneRepository communeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ResetPasswordTokenRepository resetTokenRepository;
    private final CodeSender codeSender;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int VALIDITE_CODE_MINUTES = 15;
    private static final int MAX_TENTATIVES_CODE = 5;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       CitoyenRepository citoyenRepository,
                       OrganisationRepository organisationRepository,
                       CommuneRepository communeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       ResetPasswordTokenRepository resetTokenRepository,
                       CodeSender codeSender) {
        this.utilisateurRepository = utilisateurRepository;
        this.citoyenRepository = citoyenRepository;
        this.organisationRepository = organisationRepository;
        this.communeRepository = communeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.resetTokenRepository = resetTokenRepository;
        this.codeSender = codeSender;
    }

    public LoginResponse login(LoginRequest request) {

        String saisie = request.identifiant().trim();
        String identifiant = NumeroTelephone.ressembleANumero(saisie) ? NumeroTelephone.normaliser(saisie) : saisie.toLowerCase(Locale.ROOT);


        Utilisateur utilisateur = utilisateurRepository.findByTelephone(identifiant)
                .orElseGet(() -> utilisateurRepository.findByEmail(identifiant.toLowerCase(Locale.ROOT))
                        .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects")));

        if (!utilisateur.isActif()
                || !passwordEncoder.matches(request.password(), utilisateur.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants incorrects");
        }

        return new LoginResponse(jwtService.generateToken(utilisateur));
    }

    public LoginResponse inscrire(RegisterRequest request) {
        String telephone = NumeroTelephone.normaliser(request.telephone());
        String email = request.email() == null || request.email().isBlank()
                ? null : request.email().trim().toLowerCase(Locale.ROOT);

        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé");
        }
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }

        Role role = request.role() == null ? Role.CITOYEN : request.role();
        if (role != Role.CITOYEN && role != Role.ORGANISATION) {
            throw new IllegalArgumentException("Ce rôle ne peut pas être créé par inscription publique");
        }
        verifierDonneesProfil(request, role);

<<<<<<< HEAD
        // 1. Sauvegarde de l'utilisateur
=======
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);
        utilisateur.setPasswordHash(passwordEncoder.encode(request.password()));
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        Utilisateur compteCree = utilisateurRepository.save(utilisateur);

<<<<<<< HEAD
        // 2. Création et liaison du profil associé
        if (role == Role.CITOYEN) {
            Citoyen citoyen = new Citoyen();
            citoyen.setUtilisateur(compteCree);
=======
        if (role == Role.CITOYEN) {
            Citoyen citoyen = new Citoyen();
            citoyen.setUtilisateur(compteCree);
            citoyen.setId(compteCree.getId());
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
            citoyen.setCodePasseport(genererCodePasseport());
            citoyen.setNom(request.nom().trim());
            citoyen.setPrenom(request.prenom().trim());
            citoyen.setSexe(request.sexe());
            citoyen.setPhotoUrl(request.photoUrl());
            citoyen.setDisponibilite(Disponibilite.DISPONIBLE);
            if (request.communeId() != null) {
                citoyen.setCommune(communeRepository.findById(request.communeId())
                        .orElseThrow(() -> new IllegalArgumentException("Commune introuvable")));
            }
            citoyenRepository.save(citoyen);
        } else {
            Organisation organisation = new Organisation();
            organisation.setUtilisateur(compteCree);
<<<<<<< HEAD
=======
            organisation.setId(compteCree.getId());
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
            organisation.setNom(request.nomOrganisation().trim());
            organisation.setDescription(request.descriptionOrganisation());
            organisation.setStatut(StatutOrganisation.EN_ATTENTE);
            organisationRepository.save(organisation);
        }
<<<<<<< HEAD

        return new LoginResponse(jwtService.generateToken(compteCree));
    }
=======
        return new LoginResponse(jwtService.generateToken(compteCree));
    }

>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
    public void changerMotDePasse(
            UUID authenticatedUserId,
            ChangementMotDePasseRequest request
    ) {
        if (!authenticatedUserId.equals(request.utilisateurId())) {
            throw new AccessDeniedException("Vous ne pouvez modifier que votre propre mot de passe");
        }
        Utilisateur utilisateur = utilisateurRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        if (!passwordEncoder.matches(
                request.ancienMotDePasse(), utilisateur.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }
        if (passwordEncoder.matches(
                request.nouveauMotDePasse(), utilisateur.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent");
        }
        utilisateur.setPasswordHash(passwordEncoder.encode(request.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }

    /**
<<<<<<< HEAD
     - Étape 1 du mot de passe oublié. Ne révèle jamais si le compte existe : la méthode se termine

     - normalement dans tous les cas (le contrôleur répond toujours 204).

=======
     * Étape 1 du mot de passe oublié. Ne révèle jamais si le compte existe : la méthode se termine
     * normalement dans tous les cas (le contrôleur répond toujours 204).
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
     */
    public void demanderReinitialisation(ForgotPasswordRequest request) {
        Optional<Utilisateur> trouve = chercherParIdentifiant(request.identifiant());
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

    /**
<<<<<<< HEAD
     - Étape 2 : vérifie le code puis change le mot de passe. noRollbackFor : le compteur de tentatives

     - doit être conservé même quand le code est faux (sinon la limite de 5 essais serait contournable).

=======
     * Étape 2 : vérifie le code puis change le mot de passe. noRollbackFor : le compteur de tentatives
     * doit être conservé même quand le code est faux (sinon la limite de 5 essais serait contournable).
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void reinitialiser(ResetPasswordRequest request) {
        Utilisateur utilisateur = chercherParIdentifiant(request.identifiant())
                .orElseThrow(AuthService::codeInvalide);
        ResetPasswordToken jeton = resetTokenRepository
                .findFirstByUtilisateurIdAndUtiliseFalseOrderByDateCreationDesc(utilisateur.getId())
                .orElseThrow(AuthService::codeInvalide);

        if (jeton.getExpireLe().isBefore(LocalDateTime.now())
                || jeton.getTentatives() >= MAX_TENTATIVES_CODE) {
            throw codeInvalide();
        }
        if (!passwordEncoder.matches(request.code().trim(), jeton.getCodeHash())) {
            jeton.setTentatives(jeton.getTentatives() + 1);
            resetTokenRepository.save(jeton);
            throw codeInvalide();
        }

        utilisateur.setPasswordHash(passwordEncoder.encode(request.motDePasse()));
        utilisateurRepository.save(utilisateur);
        jeton.setUtilise(true);
        resetTokenRepository.save(jeton);
    }

    private static IllegalArgumentException codeInvalide() {
        return new IllegalArgumentException("Code incorrect ou expiré");
    }

    /** Même logique d'identifiant que login : téléphone normalisé, sinon email en minuscules. */
    private Optional<Utilisateur> chercherParIdentifiant(String saisie) {
        String valeur = saisie.trim();
        String identifiant;
        try {
            identifiant = NumeroTelephone.ressembleANumero(valeur)
                    ? NumeroTelephone.normaliser(valeur)
                    : valeur.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        Optional<Utilisateur> parTelephone = utilisateurRepository.findByTelephone(identifiant);
        return parTelephone.isPresent() ? parTelephone : utilisateurRepository.findByEmail(identifiant);
    }

    private void verifierDonneesProfil(RegisterRequest request, Role role) {
        if (role == Role.CITOYEN) {
            if (request.nom() == null || request.nom().isBlank()
                    || request.prenom() == null || request.prenom().isBlank()) {
                throw new IllegalArgumentException("Le nom et le prénom sont obligatoires pour un citoyen");
            }
            if (request.nom().length() > 100 || request.prenom().length() > 100) {
                throw new IllegalArgumentException("Le nom et le prénom ne doivent pas dépasser 100 caractères");
            }
            if (request.photoUrl() != null && request.photoUrl().length() > 500) {
                throw new IllegalArgumentException("L'URL de la photo ne doit pas dépasser 500 caractères");
            }
        } else {
            if (request.nomOrganisation() == null || request.nomOrganisation().isBlank()) {
                throw new IllegalArgumentException("Le nom de l'organisation est obligatoire");
            }
            if (request.nomOrganisation().length() > 150) {
                throw new IllegalArgumentException("Le nom de l'organisation ne doit pas dépasser 150 caractères");
            }
        }
    }

    private String genererCodePasseport() {
        return "MC-" + java.time.Year.now().getValue() + "-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 24b70548ed6170cd3de9508110eb0605e8cfc796
