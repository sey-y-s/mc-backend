package odk.malicompetences.backend.territoire;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/territoires")
public class TerritoireReferenceController {
    private final RegionRepository regionRepository;
    private final RegionMapper mapper;

    public TerritoireReferenceController(RegionRepository regionRepository, RegionMapper mapper) {
        this.regionRepository = regionRepository;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION','ADMIN','SUPER_ADMIN')")
    public List<RegionResponse> lister(@RequestParam(defaultValue = "REGION") String niveau) {
        String filtre = niveau == null ? "REGION" : niveau.trim().toUpperCase(java.util.Locale.ROOT);
        List<Region> territoires;
        int attendu;
        if ("TOUS".equals(filtre)) {
            territoires = regionRepository.findAllByNiveauNot(NiveauTerritoire.CERCLE);
            attendu = 20;
        } else {
            NiveauTerritoire type;
            try {
                type = NiveauTerritoire.valueOf(filtre);
            } catch (IllegalArgumentException ex) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Niveau territorial invalide. Valeurs admises : REGION, DISTRICT, CERCLE, TOUS.");
            }
            territoires = regionRepository.findAllByNiveau(type);
            attendu = type == NiveauTerritoire.REGION ? 19 : type == NiveauTerritoire.DISTRICT ? 1 : territoires.size();
        }
        boolean codesInvalides = territoires.stream().anyMatch(r -> r.getCodeStable() == null || r.getCodeStable().isBlank())
                || territoires.stream().map(Region::getCodeStable).distinct().count() != territoires.size();
        boolean niveauxInvalides = "TOUS".equals(filtre)
                && (territoires.stream().filter(r -> r.getNiveau() == NiveauTerritoire.REGION).count() != 19
                || territoires.stream().filter(r -> r.getNiveau() == NiveauTerritoire.DISTRICT).count() != 1);
        if (territoires.size() != attendu || codesInvalides || niveauxInvalides) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "Référentiel territorial incomplet : 19 régions et 1 district, avec des codes stables uniques, sont requis.");
        }
        return territoires.stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/code/{codeStable}")
    @PreAuthorize("hasAnyRole('INSTITUTION','ADMIN','SUPER_ADMIN')")
    public RegionResponse parCode(@PathVariable String codeStable) {
        return regionRepository.findByCodeStable(codeStable)
                .map(mapper::toResponse)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Territoire introuvable."));
    }
}
