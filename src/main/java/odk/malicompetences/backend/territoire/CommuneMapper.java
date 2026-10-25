package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommuneMapper {

    public Commune toEntity(CommuneRequest request, Territoire territoire) {
        if (request == null) return null;
        Commune commune = new Commune();
        commune.setCode(request.code());
        commune.setNom(request.nom());
        commune.setTerritoire(territoire);
        commune.setActif(request.actif() == null || request.actif());
        return commune;
    }

    public CommuneResponse toResponse(Commune commune) {
        if (commune == null) return null;
        return new CommuneResponse(
                commune.getId(),
                commune.getTerritoire() != null ? commune.getTerritoire().getId() : null,
                commune.getTerritoire() != null ? commune.getTerritoire().getNom() : null,
                commune.getCode(),
                commune.getNom(),
                commune.isActif(),
                construireHierarchie(commune.getTerritoire())
        );
    }

    private List<CommuneResponse.TerritoireSummary> construireHierarchie(Territoire territoire) {
        List<CommuneResponse.TerritoireSummary> hierarchie = new ArrayList<>();
        Territoire courant = territoire;
        while (courant != null) {
            hierarchie.add(new CommuneResponse.TerritoireSummary(
                    courant.getId(),
                    courant.getCode(),
                    courant.getNom(),
                    courant.getNiveau()
            ));
            courant = courant.getParent();
        }
        return hierarchie;
    }
}
