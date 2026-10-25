package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.statut.SourceLocalisation;
import odk.malicompetences.backend.common.statut.StatutActivite;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class CitoyenService {
    private final CitoyenRepository citoyenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenMapper mapper;
    private final CommuneRepository communeRepository;
    private final LocalisationProfilRepository localisationRepository;

    public CitoyenService(CitoyenRepository citoyenRepository, UtilisateurRepository utilisateurRepository,
                          CitoyenMapper mapper, CommuneRepository communeRepository,
                          LocalisationProfilRepository localisationRepository) {
        this.citoyenRepository = citoyenRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.mapper = mapper;
        this.communeRepository = communeRepository;
        this.localisationRepository = localisationRepository;
    }

    public CitoyenResponse creer(UUID utilisateurId, CitoyenRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (utilisateur.getRoles() == null || utilisateur.getRoles().stream().noneMatch(role -> Role.CITOYEN.name().equals(role.getCode()))) throw new IllegalArgumentException("Le compte associé doit avoir le rôle CITOYEN");
        if (citoyenRepository.findByUtilisateurId(utilisateurId).isPresent()) throw new IllegalStateException("Le profil citoyen existe déjà");
        Citoyen citoyen = new Citoyen();
        citoyen.setUtilisateur(utilisateur);
        appliquer(citoyen, request);
        Citoyen saved = citoyenRepository.save(citoyen);
        enregistrerLocalisation(saved, request.communeId(), SourceLocalisation.DECLARATION);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CitoyenResponse trouverParId(UUID id) {
        Citoyen citoyen = citoyenRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Citoyen introuvable"));
        verifierProprietaireOuAdmin(citoyen);
        return mapper.toResponse(citoyen);
    }

    @Transactional(readOnly = true)
    public CitoyenResponse trouverParUtilisateurId(UUID utilisateurId) {
        Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        return mapper.toResponse(citoyen);
    }

    public CitoyenResponse modifierMonProfil(UUID utilisateurId, CitoyenRequest request) {
        Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        appliquer(citoyen, request);
        if (request.communeId() != null) enregistrerLocalisation(citoyen, request.communeId(), SourceLocalisation.DECLARATION);
        return mapper.toResponse(citoyenRepository.save(citoyen));
    }

    public CitoyenResponse modifier(UUID id, CitoyenRequest request) {
        Citoyen citoyen = citoyenRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Citoyen introuvable"));
        appliquer(citoyen, request);
        if (request.communeId() != null) enregistrerLocalisation(citoyen, request.communeId(), SourceLocalisation.ADMINISTRATION);
        return mapper.toResponse(citoyenRepository.save(citoyen));
    }

    public CitoyenResponse modifierLocalisation(UUID utilisateurId, UUID communeId) {
        Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        enregistrerLocalisation(citoyen, communeId, SourceLocalisation.DECLARATION);
        return mapper.toResponse(citoyen);
    }

    public CitoyenResponse modifierConsentementGps(UUID utilisateurId, boolean consentement) {
        Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        citoyen.setGpsConsenti(consentement);
        citoyen.setGpsConsentiLe(consentement ? LocalDateTime.now() : null);
        return mapper.toResponse(citoyenRepository.save(citoyen));
    }

    private void appliquer(Citoyen citoyen, CitoyenRequest request) {
        citoyen.setNom(request.nom().trim());
        citoyen.setPrenom(request.prenom().trim());
        citoyen.setDateNaissance(request.dateNaissance());
        citoyen.setSexe(request.sexe());
        citoyen.setStatutActivite(request.statutActivite() == null ? StatutActivite.NON_RENSEIGNE : request.statutActivite());
        if (request.profilPublic() != null) citoyen.setProfilPublic(request.profilPublic());
    }

    private void enregistrerLocalisation(Citoyen citoyen, UUID communeId, SourceLocalisation source) {
        if (communeId == null) return;
        Commune commune = communeRepository.findById(communeId).orElseThrow(() -> new ResourceNotFoundException("Commune introuvable"));
        LocalisationProfil localisation = localisationRepository.findByCitoyenId(citoyen.getId()).orElseGet(LocalisationProfil::new);
        localisation.setCitoyen(citoyen);
        localisation.setCommune(commune);
        localisation.setSource(source);
        localisation.setConfirmeeLe(LocalDateTime.now());
        citoyen.setLocalisationProfil(localisationRepository.save(localisation));
    }

    private void verifierProprietaireOuAdmin(Citoyen citoyen) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!admin && (auth == null || !citoyen.getUtilisateur().getId().toString().equals(auth.getName()))) throw new AccessDeniedException("Accès refusé.");
    }
}
