package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Consultation du référentiel secteurs / métiers (lecture seule ; l'écriture reste dans l'administration). */
@Service
@Transactional(readOnly = true)
public class ReferentielLectureService {

    private final SecteurActiviteRepository secteurRepository;
    private final MetierRepository metierRepository;

    public ReferentielLectureService(SecteurActiviteRepository secteurRepository, MetierRepository metierRepository) {
        this.secteurRepository = secteurRepository;
        this.metierRepository = metierRepository;
    }

    public List<SecteurActiviteResume> secteurs() {
        return secteurRepository.findAll().stream()
                .sorted(Comparator.comparing(SecteurActivite::getNom, String.CASE_INSENSITIVE_ORDER))
                .map(SecteurActiviteResume::de)
                .toList();
    }

    public SecteurActiviteResume secteur(UUID id) {
        return secteurRepository.findById(id)
                .map(SecteurActiviteResume::de)
                .orElseThrow(() -> new ResourceNotFoundException("Secteur d'activité introuvable avec l'id : " + id));
    }

    public List<MetierResume> metiers(UUID secteurId, String q) {
        String recherche = q == null || q.isBlank() ? null : q.trim();
        return metierRepository.rechercher(secteurId, recherche).stream().map(MetierResume::de).toList();
    }

    public MetierResume metier(UUID id) {
        return metierRepository.findById(id)
                .map(MetierResume::de)
                .orElseThrow(() -> new ResourceNotFoundException("Métier introuvable avec l'id : " + id));
    }
}
