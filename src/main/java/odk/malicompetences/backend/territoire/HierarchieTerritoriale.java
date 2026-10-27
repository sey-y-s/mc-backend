package odk.malicompetences.backend.territoire;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Contrôles hiérarchiques du référentiel territorial (consignes V1.2) :
 * une région et le district n'ont pas de parent ; un cercle appartient à une région ;
 * une commune appartient à un cercle (de sa région) ou au district de Bamako.
 */
@Component
public class HierarchieTerritoriale {

    private final RegionRepository regionRepository;

    public HierarchieTerritoriale(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    public void verifierTerritoire(NiveauTerritoire niveau, Region parent) {
        if (niveau == NiveauTerritoire.CERCLE) {
            if (parent == null) {
                throw new IllegalArgumentException("Un cercle doit être rattaché à une région.");
            }
            if (parent.getNiveau() != NiveauTerritoire.REGION) {
                throw new IllegalArgumentException("Le parent d'un cercle doit être une région (pas un district ni un cercle).");
            }
        } else if (parent != null) {
            throw new IllegalArgumentException("Une région ou un district n'a pas de territoire parent.");
        }
    }

    /**
     * Détermine le rattachement d'une commune. Renvoie {région, cercle} cohérents :
     * <ul>
     *   <li>district : pas de cercle ;</li>
     *   <li>région : un cercle de cette région (choisi d'office s'il n'y en a qu'un) ;</li>
     *   <li>cercle transmis comme « région » : sa région parente est retrouvée.</li>
     * </ul>
     */
    public Region[] rattachementCommune(Region territoire, Region cercle) {
        if (territoire == null) {
            throw new IllegalArgumentException("Le territoire de la commune est obligatoire.");
        }
        if (territoire.getNiveau() == NiveauTerritoire.CERCLE) {
            cercle = territoire;
            territoire = territoire.getParent();
            if (territoire == null) {
                throw new IllegalStateException("Ce cercle n'est rattaché à aucune région.");
            }
        }
        if (territoire.getNiveau() == NiveauTerritoire.DISTRICT) {
            if (cercle != null) {
                throw new IllegalArgumentException("Une commune du district de Bamako n'appartient à aucun cercle.");
            }
            return new Region[]{territoire, null};
        }
        if (cercle == null) {
            List<Region> cercles = regionRepository.findAllByParent_IdAndNiveau(territoire.getId(), NiveauTerritoire.CERCLE);
            if (cercles.size() != 1) {
                throw new IllegalArgumentException("Choisissez le cercle auquel appartient la commune.");
            }
            cercle = cercles.get(0);
        }
        if (cercle.getNiveau() != NiveauTerritoire.CERCLE) {
            throw new IllegalArgumentException("Le territoire choisi comme cercle n'est pas un cercle.");
        }
        if (cercle.getParent() == null || !cercle.getParent().getId().equals(territoire.getId())) {
            throw new IllegalArgumentException("Ce cercle n'appartient pas à la région choisie.");
        }
        return new Region[]{territoire, cercle};
    }
}
