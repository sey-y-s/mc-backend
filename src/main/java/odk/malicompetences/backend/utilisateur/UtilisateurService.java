package odk.malicompetences.backend.utilisateur;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;

    public UtilisateurService(
            UtilisateurRepository utilisateurRepository,
            UtilisateurMapper utilisateurMapper
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
    }

    public UtilisateurResponse creerUtilisateur(UtilisateurRequest request) {
        if (utilisateurRepository.existsByTelephone(request.telephone())) {
            throw new RuntimeException("Un utilisateur avec ce numéro de téléphone existe déjà.");
        }
        if (request.email() != null && !request.email().isBlank() && utilisateurRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà.");
        }

        // TODO : Encoder le mot de passe avec PasswordEncoder (ex: BCrypt) si Spring Security est configuré.
        // Exemple : passwordEncoder.encode(request.password())
        String passwordHash = request.password();

        Utilisateur utilisateur = utilisateurMapper.toEntity(request, passwordHash);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse trouverParId(UUID id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Transactional(readOnly = true)
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll().stream()
                .map(utilisateurMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UtilisateurResponse modifierUtilisateur(UUID id, UtilisateurRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));

        if (!utilisateur.getTelephone().equals(request.telephone()) && utilisateurRepository.existsByTelephone(request.telephone())) {
            throw new RuntimeException("Ce numéro de téléphone est déjà utilisé par un autre compte.");
        }

        String passwordHash = (request.password() != null && !request.password().isBlank()) ? request.password() : null;

        utilisateurMapper.updateEntity(request, utilisateur, passwordHash);
        Utilisateur updated = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toResponse(updated);
    }

    public void supprimerUtilisateur(UUID id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'id : " + id);
        }
        utilisateurRepository.deleteById(id);
    }
}