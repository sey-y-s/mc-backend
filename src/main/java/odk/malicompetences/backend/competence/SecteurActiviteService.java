package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SecteurActiviteService {
    private final SecteurActiviteRepository repository;
    private final SecteurActiviteMapper mapper;

    public SecteurActiviteService(SecteurActiviteRepository repository, SecteurActiviteMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<SecteurActiviteResponse> lister(Boolean actif) {
        List<SecteurActivite> secteurs = actif == null
                ? repository.findAll().stream().sorted(java.util.Comparator.comparing(SecteurActivite::getNom)).toList()
                : repository.findAllByActifOrderByNomAsc(actif);
        return secteurs.stream().map(mapper::toResponse).toList();
    }

    public SecteurActiviteResponse trouverParId(UUID id) {
        return repository.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Secteur d’activité introuvable : " + id));
    }

    public SecteurActiviteResponse trouverParCode(String code) {
        return repository.findByCode(code).map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Secteur d’activité introuvable pour le code : " + code));
    }
}
