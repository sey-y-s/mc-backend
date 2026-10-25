package odk.malicompetences.backend.utilisateur;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import odk.malicompetences.backend.common.validation.NumeroTelephone;
import odk.malicompetences.backend.common.validation.ContactCompte;

import java.util.Locale;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final PreferencesUtilisateurRepository preferencesRepository;

    public UtilisateurService(
            UtilisateurRepository utilisateurRepository,
            UtilisateurMapper utilisateurMapper,
            PasswordEncoder passwordEncoder,
            PreferencesUtilisateurRepository preferencesRepository
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
        this.passwordEncoder = passwordEncoder;
        this.preferencesRepository = preferencesRepository;
    }

    public UtilisateurResponse creerUtilisateur(UtilisateurRequest request) {
        if (request.role() == Role.CENTRE_ADMIN || (request.roles() != null && request.roles().contains(Role.CENTRE_ADMIN))) {
            throw new IllegalArgumentException("Le rôle CENTRE_ADMIN doit être attribué via l'affectation à un centre.");
        }
        // Même normalisation qu'à la connexion, sinon le compte créé ne pourrait pas se connecter.
        ContactCompte contact = ContactCompte.normaliser(request.telephone(), request.email());
        String telephone = contact.telephone();
        String email = contact.email();
        if (telephone != null && utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Un utilisateur avec ce numéro de téléphone existe déjà.");
        }
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Un utilisateur avec cet email existe déjà.");
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire à la création du compte.");
        }
        String passwordHash = passwordEncoder.encode(request.password());

        Utilisateur utilisateur = utilisateurMapper.toEntity(
                new UtilisateurRequest(telephone, email, request.password(), request.role(), request.actif(), request.roles()),
                passwordHash);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse trouverParId(UUID id) {
        return utilisateurMapper.toResponse(chargerUtilisateur(id));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll().stream()
                .map(utilisateurMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UtilisateurResponse modifierUtilisateur(UUID id, UtilisateurRequest request) {
        Utilisateur utilisateur = chargerUtilisateur(id);
        if (request.role() == Role.CENTRE_ADMIN || (request.roles() != null && request.roles().contains(Role.CENTRE_ADMIN))) {
            throw new IllegalArgumentException("Le rôle CENTRE_ADMIN doit être attribué via l'affectation à un centre.");
        }
        ContactCompte contact = ContactCompte.normaliser(request.telephone(), request.email());
        String telephone = contact.telephone();
        String email = contact.email();

        if (!java.util.Objects.equals(telephone, utilisateur.getTelephone()) && telephone != null && utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé par un autre compte.");
        }
        if (email != null && !email.equalsIgnoreCase(utilisateur.getEmail())
                && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé par un autre compte.");
        }

        String passwordHash = (request.password() != null && !request.password().isBlank())
                ? passwordEncoder.encode(request.password())
                : null;

        utilisateurMapper.updateEntity(
                new UtilisateurRequest(telephone, email, request.password(), request.role(), request.actif(), request.roles()),
                utilisateur, passwordHash);
        Utilisateur updated = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toResponse(updated);
    }

    public UtilisateurResponse modifierCoordonnees(
            UUID utilisateurId,
            UtilisateurContactRequest request
    ) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException(
                        "Utilisateur introuvable"
                ));
        ContactCompte contact = ContactCompte.normaliser(request.telephone(), request.email());
        String telephone = contact.telephone();
        String email = contact.email();
        if (!java.util.Objects.equals(telephone, utilisateur.getTelephone())
                && telephone != null && utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé");
        }
        if (email != null && !email.equalsIgnoreCase(utilisateur.getEmail())
                && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);
        utilisateur.setTelephoneVerifieLe(null);
        utilisateur.setEmailVerifieLe(null);
        utilisateur.setStatut(odk.malicompetences.backend.common.statut.StatutCompte.EN_ATTENTE);
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public PreferencesUtilisateurRequest lireMesPreferences(UUID utilisateurId) {
        return preferencesRepository.findById(utilisateurId)
                .map(preferences -> new PreferencesUtilisateurRequest(
                        preferences.isLowDataMode(),
                        preferences.isNotificationsEnabled()
                ))
                .orElseGet(() -> new PreferencesUtilisateurRequest(false, true));
    }

    public PreferencesUtilisateurRequest modifierMesPreferences(
            UUID utilisateurId,
            PreferencesUtilisateurRequest request
    ) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException(
                        "Utilisateur introuvable"
                ));
        PreferencesUtilisateur preferences = preferencesRepository.findById(utilisateurId)
                .orElseGet(() -> {
                    PreferencesUtilisateur created = new PreferencesUtilisateur();
                    created.setUtilisateur(utilisateur);
                    return created;
                });
        preferences.setLowDataMode(request.lowDataMode());
        preferences.setNotificationsEnabled(request.notificationsEnabled());
        PreferencesUtilisateur saved = preferencesRepository.save(preferences);
        return new PreferencesUtilisateurRequest(
                saved.isLowDataMode(), saved.isNotificationsEnabled()
        );
    }

    public void supprimerUtilisateur(UUID id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new odk.malicompetences.backend.common.exception.ResourceNotFoundException(
                    "Utilisateur non trouvé avec l'id : " + id);
        }
        try {
            utilisateurRepository.deleteById(id);
            utilisateurRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Impossible de supprimer cet utilisateur : il est lié à d'autres données. Désactivez-le plutôt.");
        }
    }

    private Utilisateur chargerUtilisateur(UUID id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException(
                        "Utilisateur non trouvé avec l'id : " + id));
    }

    private static String normaliserEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
