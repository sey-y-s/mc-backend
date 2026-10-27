package odk.malicompetences.backend.parcours;

import odk.malicompetences.backend.preuve.TypePreuve;

import java.util.List;

/** Règle effective d'une modalité pour une compétence (configurée, ou valeur par défaut si rien n'est configuré). */
public record RegleModalite(ModaliteValidation modalite,
                            String libelle,
                            int ordre,
                            boolean actif,
                            Integer seuilReussite,
                            List<TypePreuve> piecesAdmissibles,
                            String profilsHabilites,
                            String criteres,
                            boolean parDefaut) {
}
