package odk.malicompetences.backend.institution;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Skills-gap de l'administration : offre, demande et écart CALCULÉS À LA DEMANDE (plus de saisie manuelle
 * ni de snapshots en V1). Vue interne, non publique : les effectifs ne sont pas masqués ici ; l'espace
 * Institution applique, lui, le seuil de confidentialité.
 */
@RestController
@RequestMapping("/api/v1/indicateurs/skills-gap")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class SkillsGapController {

    private final IndicateurCalculService calcul;

    public SkillsGapController(IndicateurCalculService calcul) {
        this.calcul = calcul;
    }

    public record LigneSkillsGap(UUID competenceId, String competenceNom, UUID regionId, String regionCode,
                                 String regionNom, int offre, int demande, int ecart) {
    }

    public record SkillsGapCalcule(LocalDateTime calculeLe, String criteres, String limites, List<LigneSkillsGap> lignes) {
    }

    @GetMapping
    public SkillsGapCalcule calculer(@RequestParam(required = false) UUID competenceId) {
        var resultat = calcul.calculer(competenceId);
        List<LigneSkillsGap> lignes = resultat.lignes().stream()
                .map(l -> new LigneSkillsGap(
                        l.competence().getId(), l.competence().getNom(),
                        l.territoire() == null ? null : l.territoire().getId(),
                        l.territoire() == null ? null : l.territoire().getCodeStable(),
                        l.territoire() == null ? "Mali (national)" : l.territoire().getNom(),
                        l.offre(), l.demande(), l.ecart()))
                .toList();
        return new SkillsGapCalcule(resultat.calculeLe(), IndicateurCalculService.CRITERES,
                IndicateurCalculService.LIMITES, lignes);
    }
}
