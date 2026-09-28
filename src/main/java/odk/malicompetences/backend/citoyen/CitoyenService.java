package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CitoyenService {

    private final CitoyenRepository citoyenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenMapper citoyenMapper;

    public CitoyenService(
            CitoyenRepository citoyenRepository,
            UtilisateurRepository utilisateurRepository,
            CitoyenMapper citoyenMapper
    ) {
        this.citoyenRepository = citoyenRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.citoyenMapper = citoyenMapper;
    }

    public CitoyenResponse creer(UUID utilisateurId, CitoyenRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur introuvable"
                ));

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
        citoyen.setSexe(Sexe.valueOf(request.sexe()));
        citoyen.setPhotoUrl(request.photoUrl());
        citoyen.setDisponibilite(
                request.disponibilite() != null
                        ? request.disponibilite()
                        : Disponibilite.DISPONIBLE
        );

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
}