package odk.malicompetences.backend.institution;

import odk.malicompetences.backend.common.regle.MasquageStatistique;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.territoire.NiveauTerritoire;
import odk.malicompetences.backend.territoire.Region;
import odk.malicompetences.backend.territoire.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Statistiques publiques par territoire, calculées à la demande (IndicateurCalculService) puis protégées :
 * une cellule de 1 à 4 personnes distinctes est masquée, ainsi qu'une cellule complémentaire pour
 * empêcher la déduction par différence. Aucun identifiant ni effectif brut n'est renvoyé.
 */
@Service
@Transactional(readOnly = true)
public class InstitutionIndicateurService {
    static final int SEUIL_MINIMAL = MasquageStatistique.SEUIL_PAR_DEFAUT;

    private final IndicateurCalculService calcul;
    private final RegionRepository regionRepository;
    private final CompetenceRepository competenceRepository;
    private final InstitutionFeatureGate gate;

    public InstitutionIndicateurService(
            IndicateurCalculService calcul,
            RegionRepository regionRepository,
            CompetenceRepository competenceRepository,
            InstitutionFeatureGate gate
    ) {
        this.calcul = calcul;
        this.regionRepository = regionRepository;
        this.competenceRepository = competenceRepository;
        this.gate = gate;
    }

    /** Les dates sont facultatives : le calcul porte sur la situation au jour de la demande. */
    public List<InstitutionIndicateurResponse> lire(UUID competenceId, LocalDate debut, LocalDate fin) {
        gate.requireEnabled();
        if (competenceId == null) {
            throw new IllegalArgumentException("La compétence est obligatoire.");
        }
        Competence competence = competenceRepository.findById(competenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence introuvable."));
        LocalDate jour = LocalDate.now();
        IndicateurCalculService.Calcul resultat = calcul.calculer(competenceId);

        Map<UUID, IndicateurCalculService.Ligne> parTerritoire = new HashMap<>();
        for (var l : resultat.lignes()) {
            if (l.territoire() != null) parTerritoire.put(l.territoire().getId(), l);
        }
        List<Region> territoires = regionRepository.findAllByNiveauNot(NiveauTerritoire.CERCLE).stream()
                .filter(r -> r.getCodeStable() != null && !r.getCodeStable().isBlank())
                .sorted(Comparator.comparing(Region::getCodeStable))
                .toList();

        // Règle de confidentialité isolée dans MasquageStatistique (effectif = personnes distinctes, ici l'offre).
        Map<UUID, Integer> effectifs = new LinkedHashMap<>();
        for (Region r : territoires) effectifs.put(r.getId(), offre(parTerritoire.get(r.getId())));
        Set<UUID> masques = MasquageStatistique.cellulesMasquees(effectifs, SEUIL_MINIMAL);

        List<InstitutionIndicateurResponse> output = new ArrayList<>();
        for (Region r : territoires) {
            var l = parTerritoire.get(r.getId());
            boolean masque = masques.contains(r.getId());
            int offre = offre(l);
            int demande = l == null ? 0 : l.demande();
            output.add(new InstitutionIndicateurResponse(
                    r.getCodeStable(), r.getNom(), competence.getId(), competence.getNom(),
                    debut == null ? jour : debut, fin == null ? jour : fin,
                    masque ? null : offre,
                    masque ? null : demande,
                    masque ? null : offre - demande,
                    masque ? "MASQUE" : "VISIBLE",
                    masque ? "effectif trop faible ou suppression complémentaire" : null,
                    resultat.calculeLe()));
        }
        return List.copyOf(output);
    }

    private static int offre(IndicateurCalculService.Ligne l) {
        return l == null ? 0 : l.offre();
    }
}
