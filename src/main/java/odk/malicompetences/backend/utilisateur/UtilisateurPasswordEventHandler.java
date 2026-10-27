package odk.malicompetences.backend.utilisateur;

/**
 * Ancien gestionnaire d'événements Spring Data REST, retiré : Spring Data REST n'est pas utilisé par ce projet
 * (aucune dépendance dans le pom.xml, et il exposerait tous les repositories sans contrôle).
 * Le hachage BCrypt des mots de passe est fait par AuthService et UtilisateurService.
 * Cette classe ne contient plus de code ; le fichier peut être supprimé.
 */
final class UtilisateurPasswordEventHandler {

    private UtilisateurPasswordEventHandler() {
    }
}
