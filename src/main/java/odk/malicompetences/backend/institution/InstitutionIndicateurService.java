package odk.malicompetences.backend.institution;

import odk.malicompetences.backend.common.regle.MasquageStatistique;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.territoire.NiveauTerritoire;
import odk.malicompetences.backend.territoire.Territoire;
import odk.malicompetences.backend.territoire.TerritoireRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InstitutionIndicateurService {
    private static final String OFFRE_SQL = """
        WITH RECURSIVE descendants(id, root_id) AS (
          SELECT id, id FROM territoire WHERE niveau IN ('REGION','DISTRICT')
          UNION ALL
          SELECT enfant.id, parent.root_id
          FROM territoire enfant JOIN descendants parent ON enfant.parent_id = parent.id
        )
        SELECT COUNT(DISTINCT cc.citoyen_id)
        FROM citoyen_competence cc
        JOIN localisation_profil lp ON lp.citoyen_id = cc.citoyen_id
        JOIN commune cm ON cm.id = lp.commune_id
        JOIN descendants d ON d.id = cm.territoire_id
        WHERE d.root_id = :territoireId
          AND cc.competence_id = :competenceId
          AND cc.statut_validation = 'VALIDEE'
          AND (cc.valide_jusqu_au IS NULL OR cc.valide_jusqu_au >= CURRENT_DATE)
        """;

    private static final String DEMANDE_SQL = """
        WITH RECURSIVE descendants(id, root_id) AS (
          SELECT id, id FROM territoire WHERE niveau IN ('REGION','DISTRICT')
          UNION ALL
          SELECT enfant.id, parent.root_id
          FROM territoire enfant JOIN descendants parent ON enfant.parent_id = parent.id
        )
        SELECT COALESCE(SUM(bc.nombre_requis), 0)
        FROM besoin_competence bc
        JOIN besoin b ON b.id = bc.besoin_id
        WHERE bc.competence_id = :competenceId
          AND b.statut = 'OUVERT'
          AND (b.date_limite IS NULL OR b.date_limite >= CURRENT_DATE)
          AND EXISTS (
            SELECT 1 FROM besoin_zone bz
            LEFT JOIN commune cm ON cm.id = bz.commune_id
            LEFT JOIN descendants dc ON dc.id = cm.territoire_id
            LEFT JOIN descendants dt ON dt.id = bz.territoire_id
            WHERE bz.besoin_id = b.id
              AND (dc.root_id = :territoireId OR dt.root_id = :territoireId)
          )
        """;

    private final NamedParameterJdbcTemplate jdbc;
    private final CompetenceRepository competenceRepository;
    private final TerritoireRepository territoireRepository;
    private final InstitutionFeatureGate gate;

    public InstitutionIndicateurService(NamedParameterJdbcTemplate jdbc,
            CompetenceRepository competenceRepository,
            TerritoireRepository territoireRepository,
            InstitutionFeatureGate gate) {
        this.jdbc = jdbc;
        this.competenceRepository = competenceRepository;
        this.territoireRepository = territoireRepository;
        this.gate = gate;
    }

    public List<InstitutionIndicateurResponse> lire(UUID competenceId) {
        gate.requireEnabled();
        if (competenceId == null) throw new IllegalArgumentException("competenceId est obligatoire.");
        Competence competence = competenceRepository.findById(competenceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compétence introuvable."));

        List<Territoire> zones = new ArrayList<>();
        zones.addAll(territoireRepository.findAllByNiveau(NiveauTerritoire.REGION));
        zones.addAll(territoireRepository.findAllByNiveau(NiveauTerritoire.DISTRICT));
        zones = zones.stream().filter(Territoire::isActif)
                .sorted(Comparator.comparing(Territoire::getCode)).toList();

        List<MesureBrute> brutes = new ArrayList<>();
        Map<String, Integer> effectifs = new TreeMap<>();
        LocalDateTime calculeLe = LocalDateTime.now();
        for (Territoire zone : zones) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("territoireId", zone.getId().toString())
                    .addValue("competenceId", competenceId.toString());
            Integer offre = jdbc.queryForObject(OFFRE_SQL, params, Integer.class);
            Integer demande = jdbc.queryForObject(DEMANDE_SQL, params, Integer.class);
            int effectif = offre == null ? 0 : offre;
            int demandeValue = demande == null ? 0 : demande;
            brutes.add(new MesureBrute(zone, effectif, demandeValue));
            effectifs.put(zone.getCode(), effectif);
        }

        Set<String> masques = MasquageStatistique.codesMasques(effectifs, MasquageStatistique.SEUIL_PAR_DEFAUT);
        return brutes.stream().map(b -> {
            boolean masque = masques.contains(b.zone().getCode());
            return new InstitutionIndicateurResponse(
                    b.zone().getCode(), b.zone().getNom(), competence.getId(), competence.getNom(),
                    masque ? null : b.offre(), masque ? null : b.demande(),
                    masque ? null : b.offre() - b.demande(),
                    masque ? "MASQUE" : "VISIBLE",
                    masque ? "effectif de personnes distinctes inférieur au seuil ou masquage complémentaire" : null,
                    calculeLe,
                    "Offre : citoyens distincts avec compétence validée et non expirée ; demande : besoins ouverts non expirés couvrant ce territoire.",
                    "Couverture limitée aux profils dont la commune est renseignée et aux besoins géolocalisés ; les besoins nationaux sont exclus des cellules régionales pour éviter une répartition arbitraire."
            );
        }).collect(Collectors.toUnmodifiableList());
    }

    public List<CompetenceOption> competences() {
        gate.requireEnabled();
        return competenceRepository.findAll().stream().filter(Competence::isActif)
                .map(c -> new CompetenceOption(c.getId(), c.getNom()))
                .sorted(Comparator.comparing(CompetenceOption::nom, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    private record MesureBrute(Territoire zone, int offre, int demande) {}
    public record CompetenceOption(UUID id, String nom) {}
}
