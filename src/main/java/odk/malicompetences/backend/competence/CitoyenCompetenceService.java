package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CitoyenCompetenceService {

    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final CitoyenRepository citoyenRepository;
    private final CompetenceRepository competenceRepository;
    private final CitoyenCompetenceMapper mapper;

    public CitoyenCompetenceService(
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            CitoyenRepository citoyenRepository,
            CompetenceRepository competenceRepository,
            CitoyenCompetenceMapper mapper
    ) {
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.citoyenRepository = citoyenRepository;
        this.competenceRepository = competenceRepository;
        this.mapper = mapper;
    }

    public CitoyenCompetenceResponse ajouter(
            UUID citoyenId,
            CitoyenCompetenceRequest request
    ) {
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citoyen introuvable"
                ));

        Competence competence = competenceRepository.findById(
                request.competenceId()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Compétence introuvable"
        ));

        if (citoyenCompetenceRepository
                .existsByCitoyenIdAndCompetenceId(
                        citoyenId,
                        request.competenceId()
                )) {
            throw new IllegalStateException(
                    "Cette compétence est déjà déclarée"
            );
        }

        // Le secteur doit être possible pour cette compétence ; s'il n'y en a qu'un, il est appliqué d'office.
        ParcoursCompetence parcours = request.parcours();
        java.util.List<ParcoursCompetence> possibles = competence.getParcoursAutorises();
        if (parcours == null && possibles.size() == 1) {
            parcours = possibles.get(0);
        }
        if (parcours != null && !possibles.contains(parcours)) {
            throw new IllegalArgumentException(messageParcoursImpossible(competence));
        }

        CitoyenCompetence citoyenCompetence =
                new CitoyenCompetence();

        citoyenCompetence.setCitoyen(citoyen);
        citoyenCompetence.setCompetence(competence);
        citoyenCompetence.setNiveau(request.niveau());
        citoyenCompetence.setParcours(parcours);
        citoyenCompetence.setEtat(EtatCompetence.DECLAREE);
        citoyenCompetence.setDateDeclaration(LocalDateTime.now());

        return mapper.toResponse(
                citoyenCompetenceRepository.save(citoyenCompetence)
        );
    }

    @Transactional(readOnly = true)
    public List<CitoyenCompetenceResponse> trouverParCitoyen(
            UUID citoyenId
    ) {
        return citoyenCompetenceRepository
                .findByCitoyenId(citoyenId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Le citoyen indique (ou corrige) le parcours d'une de ses compétences. */
    public CitoyenCompetenceResponse definirParcours(UUID citoyenCompetenceId, UUID citoyenId, ParcoursCompetence parcours) {
        if (parcours == null) {
            throw new IllegalArgumentException("Le parcours est obligatoire : FORMEL ou INFORMEL");
        }
        CitoyenCompetence cc = citoyenCompetenceRepository.findById(citoyenCompetenceId)
                .orElseThrow(() -> new odk.malicompetences.backend.common.exception.ResourceNotFoundException("Compétence du citoyen introuvable"));
        if (!cc.getCitoyen().getId().equals(citoyenId)) {
            throw new org.springframework.security.access.AccessDeniedException("Cette compétence ne vous appartient pas.");
        }
        if (!cc.getCompetence().getParcoursAutorises().contains(parcours)) {
            throw new IllegalArgumentException(messageParcoursImpossible(cc.getCompetence()));
        }
        // Les preuves produites par la plateforme (test numérique, évaluation) restent valables quel que soit le secteur.
        var preuvesPlateforme = java.util.List.of(
                odk.malicompetences.backend.preuve.TypePreuve.TEST_NUMERIQUE,
                odk.malicompetences.backend.preuve.TypePreuve.EVALUATION_PRATIQUE);
        boolean preuvesIncompatibles = cc.getPreuves().stream()
                .filter(p -> !preuvesPlateforme.contains(p.getType()))
                .anyMatch(p -> !parcours.getPreuvesAcceptees().contains(p.getType()));
        if (preuvesIncompatibles) {
            throw new IllegalStateException("Des preuves déjà jointes ne correspondent pas à ce parcours.");
        }
        cc.setParcours(parcours);
        return mapper.toResponse(citoyenCompetenceRepository.save(cc));
    }

    private static String messageParcoursImpossible(Competence competence) {
        return competence.getParcoursAutorises().size() == 1
                ? "« " + competence.getNom() + " » s'acquiert uniquement par le secteur "
                  + (competence.getParcoursAutorises().get(0) == ParcoursCompetence.FORMEL ? "formel (école ou formation)." : "informel (terrain).")
                : "Ce secteur n'est pas possible pour cette compétence.";
    }
}
