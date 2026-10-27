package odk.malicompetences.backend.competence;

import odk.malicompetences.backend.preuve.TypePreuve;

import java.util.List;

/**
 * Parcours par lequel le citoyen a acquis une compétence ; il détermine les preuves acceptées.
 * <ul>
 *   <li>FORMEL : école ou formation diplômante. Preuves : diplôme, certificat, attestation, attestation de stage.</li>
 *   <li>INFORMEL : apprentissage sur le terrain. Preuve : certificat délivré par un centre professionnel ;
 *       sans certificat, le citoyen demande un test de validation dans un centre (aucune preuve à joindre).</li>
 * </ul>
 */
public enum ParcoursCompetence {
    FORMEL(List.of(TypePreuve.DIPLOME, TypePreuve.CERTIFICAT, TypePreuve.ATTESTATION, TypePreuve.ATTESTATION_STAGE)),
    INFORMEL(List.of(TypePreuve.CERTIFICAT_CENTRE));

    private final List<TypePreuve> preuvesAcceptees;

    ParcoursCompetence(List<TypePreuve> preuvesAcceptees) {
        this.preuvesAcceptees = preuvesAcceptees;
    }

    public List<TypePreuve> getPreuvesAcceptees() {
        return preuvesAcceptees;
    }

    /** Types qu'un citoyen peut déposer lui-même pour une compétence de ce parcours (null = compétence ancienne). */
    public static List<TypePreuve> preuvesDeposables(ParcoursCompetence parcours) {
        if (parcours != null) return parcours.preuvesAcceptees;
        return List.of(TypePreuve.DIPLOME, TypePreuve.CERTIFICAT, TypePreuve.ATTESTATION, TypePreuve.ATTESTATION_STAGE,
                TypePreuve.CERTIFICAT_CENTRE, TypePreuve.ATTESTATION_EMPLOYEUR, TypePreuve.AUTRE);
    }
}
