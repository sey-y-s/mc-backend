package odk.malicompetences.backend.parcours;

import odk.malicompetences.backend.preuve.TypePreuve;

import java.util.List;

/** Règle envoyée par l'administrateur ou l'organisme responsable pour une modalité d'une compétence. */
public record RegleModaliteRequest(ModaliteValidation modalite,
                                   Integer ordre,
                                   Boolean actif,
                                   Integer seuilReussite,
                                   List<TypePreuve> piecesAdmissibles,
                                   String profilsHabilites,
                                   String criteres) {
}
