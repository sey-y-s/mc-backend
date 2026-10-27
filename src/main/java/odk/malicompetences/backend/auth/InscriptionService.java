package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.common.validation.NumeroTelephone;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Création des comptes : inscription publique et inscription assistée par un agent de terrain.
 * Le profil métier est délégué à un {@link ProfilInitialiseur} choisi selon le rôle.
 */
@Service
@Transactional
public class InscriptionService {

    private static final List<Role> ROLES_PUBLICS = List.of(Role.CITOYEN, Role.ORGANISATION);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final Map<Role, ProfilInitialiseur> profils = new EnumMap<>(Role.class);

    public InscriptionService(UtilisateurRepository utilisateurRepository,
                              PasswordEncoder passwordEncoder,
                              VerificationService verificationService,
                              List<ProfilInitialiseur> initialiseurs) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        initialiseurs.forEach(i -> profils.put(i.role(), i));
    }

    /** Inscription publique : le compte est créé bloqué, un code part sur chaque contact. */
    public InscriptionResponse inscrire(RegisterRequest request) {
        String telephone = NumeroTelephone.normaliser(request.telephone());
        String email = normaliserEmail(request.email());
        verifierContactsLibres(telephone, email);

        Role role = request.role() == null ? Role.CITOYEN : request.role();
        if (!ROLES_PUBLICS.contains(role)) {
            throw new IllegalArgumentException("Ce rôle ne peut pas être créé par inscription publique");
        }
        profil(role).verifier(request);
        profil(role).verifierInscriptionPublique(request);
        if (!Boolean.TRUE.equals(request.conditionsAcceptees())) {
            throw new IllegalArgumentException("Vous devez accepter les conditions d'utilisation pour créer un compte.");
        }
        Utilisateur compte = creerCompte(request, telephone, email, role, request.password(), null);
        var canaux = verificationService.envoyerTous(compte);
        return new InscriptionResponse(compte.getId(), true, canaux);
    }

    /**
     * Inscription assistée pour une personne qui ne peut pas s'inscrire seule. Le compte est tracé (créé par l'agent)
     * et n'est ouvert qu'avec le consentement recueilli. Un mot de passe provisoire aléatoire est fixé : la personne
     * choisit le sien via « mot de passe oublié », ce qui vérifie aussi son contact.
     */
    public Map<String, Object> inscrireAssiste(RegisterRequest request, boolean consentementRecueilli, UUID agentId) {
        if (!consentementRecueilli) {
            throw new IllegalArgumentException("Le consentement de la personne doit être recueilli avant de créer son compte.");
        }
        String telephone = NumeroTelephone.normaliser(request.telephone());
        String email = normaliserEmail(request.email());
        verifierContactsLibres(telephone, email);
        profil(Role.CITOYEN).verifier(request);
        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent introuvable"));
        String provisoire = UUID.randomUUID().toString();
        Utilisateur compte = creerCompte(request, telephone, email, Role.CITOYEN, provisoire, agent);
        return Map.of(
                "utilisateurId", compte.getId(),
                "message", "Compte créé. La personne choisit son mot de passe avec « Mot de passe oublié » et son numéro.");
    }

    private Utilisateur creerCompte(RegisterRequest request, String telephone, String email, Role role,
                                    String motDePasse, Utilisateur agent) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);
        utilisateur.setPasswordHash(passwordEncoder.encode(motDePasse));
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        utilisateur.setConditionsAccepteesLe(LocalDateTime.now());
        if (agent != null) {
            utilisateur.setCreePar(agent);
            utilisateur.setConsentementRecueilliLe(LocalDateTime.now());
        }
        Utilisateur compte = utilisateurRepository.save(utilisateur);
        profil(role).creer(compte, request);
        return compte;
    }

    private void verifierContactsLibres(String telephone, String email) {
        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé");
        }
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }
    }

    private ProfilInitialiseur profil(Role role) {
        ProfilInitialiseur profil = profils.get(role);
        if (profil == null) throw new IllegalArgumentException("Ce rôle ne peut pas être créé par inscription");
        return profil;
    }

    private static String normaliserEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
