package odk.malicompetences.backend.common.config;

import odk.malicompetences.backend.common.statut.StatutCompte;
import odk.malicompetences.backend.common.validation.ContactCompte;
import odk.malicompetences.backend.utilisateur.RoleEntity;
import odk.malicompetences.backend.utilisateur.RoleRepository;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class InsererSuperAdmin {

    private static final Logger log = LoggerFactory.getLogger(InsererSuperAdmin.class);

    @Value("${ADMIN_BOOTSTRAP_TELEPHONE:#{null}}")
    private String bootstrapTelephone;

    @Value("${ADMIN_BOOTSTRAP_EMAIL:#{null}}")
    private String bootstrapEmail;

    @Value("${ADMIN_BOOTSTRAP_PASSWORD:#{null}}")
    private String bootstrapPassword;

    @Bean
    CommandLineRunner initDatabase(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            String tel = bootstrapTelephone != null && !bootstrapTelephone.isBlank() ? bootstrapTelephone.trim() : null;
            String mail = bootstrapEmail != null && !bootstrapEmail.isBlank() ? bootstrapEmail.trim() : null;
            String pwd = bootstrapPassword != null && !bootstrapPassword.isBlank() ? bootstrapPassword.trim() : null;

            if ((tel == null && mail == null) || pwd == null) {
                log.info("--> Aucun bootstrap super admin configuré (variables manquantes).");
                return;
            }

            if (utilisateurRepository.existsByRoleCode("SUPER_ADMIN")) {
                log.info("--> Un super administrateur existe déjà.");
                return;
            }

            ContactCompte contact = ContactCompte.normaliser(tel, mail);
            Utilisateur admin = new Utilisateur();
            admin.setTelephone(contact.telephone());
            admin.setEmail(contact.email());
            admin.setMotDePasseHash(passwordEncoder.encode(pwd));
            admin.setStatut(StatutCompte.ACTIF);
            if (contact.telephone() != null) {
                admin.setTelephoneVerifieLe(LocalDateTime.now());
            }
            if (contact.email() != null) {
                admin.setEmailVerifieLe(LocalDateTime.now());
            }
            admin.setConditionsAccepteesLe(LocalDateTime.now());

            RoleEntity roleSuperAdmin = roleRepository.findByCode("SUPER_ADMIN")
                    .orElseGet(() -> roleRepository.save(new RoleEntity("SUPER_ADMIN", "Super administrateur")));
            admin.getRoles().add(roleSuperAdmin);

            utilisateurRepository.save(admin);
            log.info("--> Super administrateur créé avec succès via les variables bootstrap.");
        };
    }
}
