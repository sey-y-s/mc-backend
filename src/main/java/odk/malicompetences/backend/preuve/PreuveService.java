package odk.malicompetences.backend.preuve;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PreuveService {

    private final PreuveRepository preuveRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final PreuveMapper preuveMapper;

    public PreuveService(
            PreuveRepository preuveRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            PreuveMapper preuveMapper
    ) {
        this.preuveRepository = preuveRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.preuveMapper = preuveMapper;
    }

    public PreuveResponse creerPreuve(PreuveRequest request) {
        CitoyenCompetence citoyenCompetence = citoyenCompetenceRepository.findById(request.citoyenCompetenceId())
                .orElseThrow(() -> new RuntimeException("Compétence du citoyen non trouvée avec l'id : " + request.citoyenCompetenceId()));

        verifierProprietaire(citoyenCompetence.getCitoyen().getId(), "CITOYEN");
        Preuve preuve = preuveMapper.toEntity(request, citoyenCompetence);
        Preuve saved = preuveRepository.save(preuve);
        return preuveMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PreuveResponse trouverParId(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));
        return preuveMapper.toResponse(preuve);
    }

    @Transactional(readOnly = true)
    public List<PreuveResponse> listerParCitoyenCompetence(UUID citoyenCompetenceId) {
        return preuveRepository.findAll().stream()
                .filter(p -> p.getCitoyenCompetence().getId().equals(citoyenCompetenceId))
                .map(preuveMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PreuveResponse modifierPreuve(UUID id, PreuveRequest request) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");

        preuveMapper.updateEntity(request, preuve);
        Preuve updated = preuveRepository.save(preuve);
        return preuveMapper.toResponse(updated);
    }

    public void supprimerPreuve(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preuve non trouvée avec l'id : " + id));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");
        preuveRepository.delete(preuve);
    }

    public PreuveResponse getById(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Preuve introuvable"));

        return preuveMapper.toResponse(preuve);
    }

    public PreuveResponse modifier(UUID id, PreuveRequest request) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");

        preuve.setType(request.type());
        preuve.setTitre(request.titre());
        preuve.setDescription(request.description());
        preuve.setFichierUrl(request.fichierUrl());

        return preuveMapper.toResponse(
                preuveRepository.save(preuve)
        );
    }

    public void supprimer(UUID id) {
        Preuve preuve = preuveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preuve introuvable"));
        verifierProprietaire(preuve.getCitoyenCompetence().getCitoyen().getId(), "CITOYEN");

        preuveRepository.delete(preuve);
    }

    /** Vérifie directement que l'utilisateur connecté possède la ressource. */
    private void verifierProprietaire(UUID proprietaireId, String roleAttendu) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean administrateur = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (administrateur) return;
        boolean bonRole = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleAttendu));
        boolean bonUtilisateur = false;
        if (authentication != null && authentication.getName() != null && proprietaireId != null) {
            try { bonUtilisateur = proprietaireId.equals(UUID.fromString(authentication.getName())); }
            catch (IllegalArgumentException ignored) { bonUtilisateur = false; }
        }
        if (!bonRole || !bonUtilisateur) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé : cette ressource ne vous appartient pas.");
        }
    }

    private boolean estAdministrateur() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}