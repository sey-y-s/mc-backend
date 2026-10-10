package odk.malicompetences.backend.utilisateur;

public record PreferencesUtilisateurRequest(
        boolean lowDataMode,
        boolean notificationsEnabled
) {
}
