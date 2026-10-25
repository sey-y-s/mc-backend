package odk.malicompetences.backend.common.validation;


// Les numéros maliens sont normalisés au format +223XXXXXXXX.
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NumeroTelephone {
    private static final Pattern MALI = Pattern.compile("^(?:\\+?223|00223)?(\\d{8})$"); /** +223 suivi de 8 chiffres. */

    private NumeroTelephone() {}

    public static String normaliser(String brut) {
        if (brut == null) throw new IllegalArgumentException("Le téléphone est obligatoire");
        Matcher m = MALI.matcher(brut.replaceAll("[\\s.\\-()]", ""));
        if (!m.matches()) {
            throw new IllegalArgumentException("Numéro invalide : 8 chiffres, indicatif +223 facultatif");
        }
        return "+223" + m.group(1);
    }

    public static boolean ressembleANumero(String saisie) {
        return saisie != null && saisie.trim().matches("^[+\\d][\\d\\s.\\-()]*$");
    }
}