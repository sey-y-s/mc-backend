package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.Disponibilite;
import odk.malicompetences.backend.localisation.SourceLocalisation;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Locale;
import java.util.UUID;

/** Profil d'un compte citoyen : identité, passeport et commune de référence déclarée. */
@Component
public class ProfilCitoyenInitialiseur implements ProfilInitialiseur {

    private final CitoyenRepository citoyenRepository;
    private final CommuneRepository communeRepository;

    public ProfilCitoyenInitialiseur(CitoyenRepository citoyenRepository, CommuneRepository communeRepository) {
        this.citoyenRepository = citoyenRepository;
        this.communeRepository = communeRepository;
    }

    @Override
    public Role role() {
        return Role.CITOYEN;
    }

    @Override
    public void verifier(RegisterRequest request) {
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
    }

    @Override
    public void verifierInscriptionPublique(RegisterRequest request) {
        if (request.sexe() == null) {
            throw new IllegalArgumentException("Le sexe est obligatoire");
        }
        if (request.communeId() == null) {
            throw new IllegalArgumentException("La commune est obligatoire");
        }
    }

    @Override
    public void creer(Utilisateur compte, RegisterRequest request) {
        Citoyen citoyen = new Citoyen();
        citoyen.setUtilisateur(compte);
        citoyen.setCodePasseport(genererCodePasseport());
        citoyen.setNom(request.nom().trim());
        citoyen.setPrenom(request.prenom().trim());
        citoyen.setSexe(request.sexe());
        citoyen.setPhotoUrl(request.photoUrl());
        citoyen.setDisponibilite(Disponibilite.DISPONIBLE);
        if (request.communeId() != null) {
            citoyen.definirCommuneReference(communeRepository.findById(request.communeId())
                    .orElseThrow(() -> new IllegalArgumentException("Commune introuvable")), SourceLocalisation.DECLARATION);
        }
        citoyenRepository.save(citoyen);
    }

    private static String genererCodePasseport() {
        return "MC-" + Year.now().getValue() + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
    }
}
