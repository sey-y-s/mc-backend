package odk.malicompetences.backend.common.regle;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Portee geographique d'un besoin et regle de comptage de la DEMANDE (proposition a valider).
 *
 * <ul>
 *   <li>Aucune zone = besoin NATIONAL. Il compte dans la demande nationale UNIQUEMENT ; il n'est jamais
 *       reparti dans les regions ou communes (sinon la somme des regions depasserait le national).</li>
 *   <li>Un besoin zone compte UNE SEULE FOIS pour un territoire ou une commune donnes, meme si
 *       plusieurs de ses zones le couvrent (commune + sa region par exemple) : la requete SQL doit
 *       compter chaque (besoin, competence) une fois, pas une fois par zone.</li>
 *   <li>La demande de deux zones disjointes peut s'additionner ; celle d'une zone et de son parent, jamais.</li>
 *   <li>Demande d'une competence = somme de besoin_competence.nombre_requis des besoins OUVERT dont
 *       date_limite est vide ou n'est pas depassee.</li>
 * </ul>
 */
public final class PorteeBesoin {

    private PorteeBesoin() {}

    public static boolean estNational(Collection<UUID> communesCiblees, Collection<UUID> territoiresCibles) {
        return communesCiblees.isEmpty() && territoiresCibles.isEmpty();
    }

    /**
     * Un besoin zone couvre une commune si la commune est ciblee, ou si l'un de ses territoires
     * ancetres (cercle, region, district) l'est. Un besoin national ne couvre aucune commune precise.
     *
     * @param ancetresDeLaCommune identifiants de tous les territoires parents de la commune (commune.territoire et ses parents)
     */
    public static boolean couvre(
            Collection<UUID> communesCiblees,
            Collection<UUID> territoiresCibles,
            UUID commune,
            Set<UUID> ancetresDeLaCommune
    ) {
        if (estNational(communesCiblees, territoiresCibles)) return false;
        if (communesCiblees.contains(commune)) return true;
        return territoiresCibles.stream().anyMatch(ancetresDeLaCommune::contains);
    }
}
