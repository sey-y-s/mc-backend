package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Component;

@Component
public class TerritoireMapper {

    public Territoire toEntity(TerritoireRequest request) {
        if (request == null) return null;
        Territoire territoire = new Territoire();
        territoire.setCode(request.code());
        territoire.setNom(request.nom());
        territoire.setNiveau(request.niveau());
        territoire.setActif(request.actif() == null || request.actif());
        return territoire;
    }

    public void updateEntity(TerritoireRequest request, Territoire territoire) {
        if (request != null && territoire != null) {
            if (request.code() != null) territoire.setCode(request.code());
            if (request.nom() != null) territoire.setNom(request.nom());
            if (request.niveau() != null) territoire.setNiveau(request.niveau());
            if (request.actif() != null) territoire.setActif(request.actif());
        }
    }

    public TerritoireResponse toResponse(Territoire territoire) {
        if (territoire == null) return null;
        return new TerritoireResponse(
                territoire.getId(),
                territoire.getCode(),
                territoire.getNom(),
                territoire.getNiveau(),
                territoire.isActif(),
                territoire.getParent() != null ? territoire.getParent().getId() : null,
                territoire.getParent() != null ? territoire.getParent().getNom() : null
        );
    }
}
