package odk.malicompetences.backend.utilisateur;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import odk.malicompetences.backend.common.validation.NumeroTelephone;

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
        // Même normalisation qu'à la connexion, sinon le compte créé ne pourrait pas se connecter.
        String telephone = NumeroTelephone.normaliser(request.telephone());
        String email = normaliserEmail(request.email());
        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Un utilisateur avec ce numéro de téléphone existe déjà.");
        }
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Un utilisateur avec cet email existe déjà.");
        }

        refuserRoleCentreGenerique(request.role(), null);
        String passwordHash = passwordEncoder.encode(request.password());

        Utilisateur utilisateur = utilisateurMapper.toEntity(
                new UtilisateurRequest(telephone, email, request.password(), request.role(), request.actif()),
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
        String telephone = NumeroTelephone.normaliser(request.telephone());
        String email = normaliserEmail(request.email());

        if (!telephone.equals(utilisateur.getTelephone()) && utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé par un autre compte.");
        }
        if (email != null && !email.equalsIgnoreCase(utilisateur.getEmail())
                && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé par un autre compte.");
        }

        refuserRoleCentreGenerique(request.role(), utilisateur.getRole());
        String passwordHash = (request.password() != null && !request.password().isBlank())
                ? passwordEncoder.encode(request.password())
                : null;

        utilisateurMapper.updateEntity(
                new UtilisateurRequest(telephone, email, request.password(), request.role(), request.actif()),
                utilisateur, passwordHash);
        Utilisateur updated = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toResponse(updated);
    }

    /** CENTRE_ADMIN découle du rattachement à un centre : il ne s'attribue jamais comme rôle générique. */
    private static void refuserRoleCentreGenerique(Role demande, Role actuel) {
        if (demande == Role.CENTRE_ADMIN && actuel != Role.CENTRE_ADMIN) {
            throw new IllegalArgumentException("Le rôle d'administrateur de centre s'obtient en rattachant le compte à un centre.");
        }
    }

    public UtilisateurResponse modifierCoordonnees(
            UUID utilisateurId,
            UtilisateurContactRequest request
    ) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException(
                        "Utilisateur introuvable"
                ));
        String telephone = NumeroTelephone.normaliser(request.telephone());
        if (!telephone.equals(utilisateur.getTelephone())
                && utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé");
        }
        String email = request.email() == null || request.email().isBlank()
                ? null : request.email().trim().toLowerCase(Locale.ROOT);
        if (email != null && !email.equalsIgnoreCase(utilisateur.getEmail())
                && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);
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
