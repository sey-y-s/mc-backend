package odk.malicompetences.backend.utilisateur;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component

@RepositoryEventHandler
public class UtilisateurPasswordEventHandler {

    private static final Pattern BCRYPT_HASH =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @HandleBeforeCreate
    @HandleBeforeSave
    public void encoderMotDePasse(Utilisateur utilisateur) {
        String password = utilisateur.getPasswordHash();
        if (password != null && !BCRYPT_HASH.matcher(password).matches()) {
            utilisateur.setPasswordHash(passwordEncoder.encode(password));
        }
    }
}
