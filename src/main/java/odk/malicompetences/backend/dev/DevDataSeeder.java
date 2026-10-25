package odk.malicompetences.backend.dev;

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
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    @Value("${DEV_SEED_PASSWORD:#{null}}")
    private String devSeedPassword;

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String password = (devSeedPassword != null && !devSeedPassword.isBlank())
                ? devSeedPassword.trim()
                : System.getenv("DEV_SEED_PASSWORD");

        if (password == null || password.isBlank()) {
            log.info("--> DEV_SEED_PASSWORD absent : aucun seed dev exécuté.");
            return;
        }

        creerCompteSiAbsent("+22370000001", "admin.dev@malicompetences.ml", password, "ADMIN");
        creerCompteSiAbsent("+22370000002", "citoyen1.dev@malicompetences.ml", password, "CITOYEN");
        creerCompteSiAbsent("+22370000003", "citoyen2.dev@malicompetences.ml", password, "CITOYEN");
        creerCompteSiAbsent("+22370000004", "organisation.dev@malicompetences.ml", password, "ORGANISATION");
    }

    private void creerCompteSiAbsent(
            String telephone,
            String email,
            String rawPassword,
            String roleCode
    ) {
        if (utilisateurRepository.existsByTelephone(telephone) || utilisateurRepository.existsByEmail(email)) {
            return;
        }

        ContactCompte contact = ContactCompte.normaliser(telephone, email);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(contact.telephone());
        utilisateur.setEmail(contact.email());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(rawPassword));
        utilisateur.setStatut(StatutCompte.ACTIF);
        utilisateur.setTelephoneVerifieLe(LocalDateTime.now());
        utilisateur.setEmailVerifieLe(LocalDateTime.now());
        utilisateur.setConditionsAccepteesLe(LocalDateTime.now());

        RoleEntity roleEntity = roleRepository.findByCode(roleCode)
                .orElseGet(() -> roleRepository.save(new RoleEntity(roleCode, roleCode)));
        utilisateur.getRoles().add(roleEntity);

        utilisateurRepository.save(utilisateur);
        log.info("--> Compte dev créé : {} ({})", email, roleCode);
    }
}

