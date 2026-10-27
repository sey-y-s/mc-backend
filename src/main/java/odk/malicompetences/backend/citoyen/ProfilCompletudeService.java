package odk.malicompetences.backend.citoyen;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Règle unique de visibilité : un profil n'est visible des autres (organisations, recherche de talents,
 * cartes) que s'il est complet. Ce sont les mêmes 5 étapes que le « Passeport complété » de l'application
 * mobile : sexe, photo, commune, première compétence, première preuve.
 * Pour changer les critères, modifier uniquement {@link #estVisible(Citoyen)} (et l'écran mobile correspondant).
 */
@Service
@Transactional(readOnly = true)
public class ProfilCompletudeService {

    private final CitoyenCompetenceRepository citoyenCompetenceRepository;

    public ProfilCompletudeService(CitoyenCompetenceRepository citoyenCompetenceRepository) {
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
    }

    /** Vrai si le profil est complet : il peut alors être vu des organisations et apparaître sur la plateforme. */
    public boolean estVisible(Citoyen citoyen) {
        // Les tests peu coûteux d'abord : la requête sur les compétences n'est faite que si nécessaire.
        if (citoyen.getSexe() == null
                || citoyen.getPhotoUrl() == null || citoyen.getPhotoUrl().isBlank()
                || citoyen.getCommune() == null) {
            return false;
        }
        // « Première preuve » : au moins une compétence au-delà du simple état déclaré (donc au moins une compétence).
        return citoyenCompetenceRepository.existsByCitoyenIdAndEtatNot(citoyen.getId(), EtatCompetence.DECLAREE);
    }

    /** À appeler avant toute action d'une organisation sur un citoyen : un profil incomplet « n'existe pas ». */
    public void exigerVisible(Citoyen citoyen) {
        if (!estVisible(citoyen)) {
            throw new ResourceNotFoundException("Profil talent introuvable.");
        }
    }
}
