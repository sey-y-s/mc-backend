package odk.malicompetences.backend.common.config;

import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InsererSuperAdmin {

    private static final Logger log = LoggerFactory.getLogger(InsererSuperAdmin.class);

    @Bean
    CommandLineRunner initDatabase(UtilisateurRepository utilisateurRepository,
                                   @Value("${SUPER_ADMIN_PASSWORD:}") String motDePasseConfigure,
                                   @Value("${SUPER_ADMIN_EMAIL:superadmin@mail.com}") String emailConfigure,
                                   @Value("${SUPER_ADMIN_TELEPHONE:+22390909090}") String telephoneConfigure,
                                   org.springframework.core.env.Environment environnement) {
        return args -> {
            // On vérifie si un admin existe déjà pour éviter les doublons
            if (utilisateurRepository.findByRole(Role.SUPER_ADMIN).stream().findFirst().isEmpty()) {
                boolean motDePasseAbsent = motDePasseConfigure == null || motDePasseConfigure.isBlank();
                if (motDePasseAbsent && environnement.acceptsProfiles(org.springframework.core.env.Profiles.of("prod"))) {
                    log.error("SUPER_ADMIN_PASSWORD est obligatoire en production : aucun super administrateur créé.");
                    return;
                }
                PasswordEncoder passwordEncoder =  new BCryptPasswordEncoder();

                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setRole(Role.SUPER_ADMIN);
                utilisateur.setTelephone(telephoneConfigure);
                utilisateur.setEmail(emailConfigure);
                // Mot de passe lu dans la variable SUPER_ADMIN_PASSWORD (plus de mot de passe écrit dans le code).
                String motDePasse = motDePasseConfigure;
                if (motDePasse == null || motDePasse.isBlank()) {
                    motDePasse = java.util.UUID.randomUUID().toString().substring(0, 12);
                    log.warn("SUPER_ADMIN_PASSWORD absent : mot de passe provisoire du super administrateur = {} (à changer)", motDePasse);
                }
                utilisateur.setPasswordHash(passwordEncoder.encode(motDePasse));
                utilisateur.setTelephoneVerifieLe(java.time.LocalDateTime.now());
                utilisateurRepository.save(utilisateur);

                log.info("Administrateur par défaut créé.");
            } else {
                log.info("Un administrateur existe déjà.");
            }
        };
    }
}