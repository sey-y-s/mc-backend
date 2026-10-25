package odk.malicompetences.backend.common.validation;

import java.util.regex.Pattern;

/**
 * Contact d'un compte : au moins un du telephone ou de l'email, REELLEMENT renseigne
 * (une chaine vide ou faite d'espaces n'est pas un contact). Les valeurs sont normalisees avant stockage
 * (telephone au format +223XXXXXXXX, email en minuscules) afin que les contraintes d'unicite fonctionnent.
 */
public record ContactCompte(String telephone, String email) {

    /** utilisateur.email est VARCHAR(190). */
    public static final int EMAIL_MAX = 190;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static ContactCompte normaliser(String telephoneBrut, String emailBrut) {
        String tel = vide(telephoneBrut) ? null : NumeroTelephone.normaliser(telephoneBrut);
        String mail = vide(emailBrut) ? null : normaliserEmail(emailBrut);
        if (tel == null && mail == null) {
            throw new IllegalArgumentException("Un numero de telephone ou une adresse email est obligatoire");
        }
        return new ContactCompte(tel, mail);
    }

    private static String normaliserEmail(String brut) {
        String e = brut.trim().toLowerCase(java.util.Locale.ROOT);
        if (e.length() > EMAIL_MAX || !EMAIL.matcher(e).matches()) {
            throw new IllegalArgumentException("Adresse email invalide");
        }
        return e;
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
