package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.portfolio.PortfolioMediaStorageService;
import odk.malicompetences.backend.portfolio.TypeMedia;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CitoyenService {

    private final CitoyenRepository citoyenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenMapper citoyenMapper;
    private final CommuneRepository communeRepository;
    private final PortfolioMediaStorageService mediaStorage;

    public CitoyenService(
            CitoyenRepository citoyenRepository,
            UtilisateurRepository utilisateurRepository,
            CitoyenMapper citoyenMapper,
            CommuneRepository communeRepository,
            PortfolioMediaStorageService mediaStorage
    ) {
        this.citoyenRepository = citoyenRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.citoyenMapper = citoyenMapper;
        this.communeRepository = communeRepository;
        this.mediaStorage = mediaStorage;
    }

    public CitoyenResponse creer(UUID utilisateurId, CitoyenRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur introuvable"
                ));

        if (utilisateur.getRole() != Role.CITOYEN) {
            throw new IllegalArgumentException("Le compte associé doit avoir le rôle CITOYEN");
        }
        if (citoyenRepository.existsById(utilisateurId)) {
            throw new IllegalStateException(
                    "Le profil citoyen existe déjà"
            );
        }

        Citoyen citoyen = new Citoyen();

        citoyen.setId(utilisateur.getId());
        citoyen.setUtilisateur(utilisateur);
        citoyen.setCodePasseport(genererCodePasseport());
        citoyen.setNom(request.nom());
        citoyen.setPrenom(request.prenom());
        citoyen.setSexe(request.sexe() == null || request.sexe().isBlank() ? null : Sexe.valueOf(request.sexe()));
        citoyen.setPhotoUrl(request.photoUrl());
        citoyen.setDisponibilite(
                request.disponibilite() != null
                        ? request.disponibilite()
                        : Disponibilite.DISPONIBLE
        );
        if (request.communeId() != null) {
            citoyen.setCommune(communeRepository.findById(request.communeId())
                    .orElseThrow(() -> new IllegalArgumentException("Commune introuvable")));
        }

        return citoyenMapper.toResponse(
                citoyenRepository.save(citoyen)
        );
    }

    @Transactional(readOnly = true)
    public CitoyenResponse trouverParId(UUID id) {
        Citoyen citoyen = citoyenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citoyen introuvable"
                ));

        return citoyenMapper.toResponse(citoyen);
    }

    public CitoyenResponse modifierMonProfil(UUID utilisateurId, CitoyenRequest request) {
        Citoyen citoyen = citoyenRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        citoyen.setNom(request.nom());
        citoyen.setPrenom(request.prenom());
        citoyen.setSexe(request.sexe() == null || request.sexe().isBlank() ? null : Sexe.valueOf(request.sexe()));
        citoyen.setPhotoUrl(request.photoUrl());
        citoyen.setDisponibilite(request.disponibilite() == null ? citoyen.getDisponibilite() : request.disponibilite());
        citoyen.setCommune(request.communeId() == null ? null : communeRepository.findById(request.communeId())
                .orElseThrow(() -> new IllegalArgumentException("Commune introuvable")));
        return citoyenMapper.toResponse(citoyenRepository.save(citoyen));
    }

    public CitoyenResponse mettreAJourPhoto(UUID utilisateurId, MultipartFile fichier) {
        Citoyen citoyen = citoyenRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));

        String anciennePhoto = citoyen.getPhotoUrl();
        String nouvellePhoto = mediaStorage.store(fichier, TypeMedia.IMAGE);
        citoyen.setPhotoUrl(nouvellePhoto);
        CitoyenResponse response = citoyenMapper.toResponse(citoyenRepository.save(citoyen));
        if (anciennePhoto != null && !anciennePhoto.equals(nouvellePhoto)) {
            mediaStorage.delete(anciennePhoto);
        }
        return response;
    }

    private String genererCodePasseport() {
        return "MC-" +
                java.time.Year.now().getValue() +
                "-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 6)
                        .toUpperCase();
    }

    public CitoyenResponse modifier(UUID id, CitoyenRequest request) {
        Citoyen citoyen = citoyenRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Citoyen introuvable"));

        citoyen.setNom(request.nom());
        citoyen.setPrenom(request.prenom());
        citoyen.setSexe(request.sexe() == null || request.sexe().isBlank() ? null : Sexe.valueOf(request.sexe()));
        citoyen.setPhotoUrl(request.photoUrl());
        citoyen.setDisponibilite(request.disponibilite());
        citoyen.setCommune(request.communeId() == null ? null : communeRepository.findById(request.communeId())
                .orElseThrow(() -> new IllegalArgumentException("Commune introuvable")));

        Citoyen citoyenModifie = citoyenRepository.save(citoyen);

        return citoyenMapper.toResponse(citoyenModifie);
    }

}