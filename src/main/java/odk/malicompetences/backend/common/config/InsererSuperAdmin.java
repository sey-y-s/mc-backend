//package odk.malicompetences.backend.common.config;
//
//import odk.malicompetences.backend.utilisateur.Role;
//import odk.malicompetences.backend.utilisateur.Utilisateur;
//import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//public class InsererSuperAdmin {
//
//    @Bean
//    CommandLineRunner initDatabase(UtilisateurRepository utilisateurRepository) {
//        return args -> {
//            // On vérifie si un admin existe déjà pour éviter les doublons
//            if (utilisateurRepository.findByRole(Role.SUPER_ADMIN).stream().findFirst().isEmpty()) {
//                PasswordEncoder passwordEncoder =  new BCryptPasswordEncoder();
//
//                Utilisateur utilisateur = new Utilisateur();
//                utilisateur.setRole(Role.SUPER_ADMIN);
//                utilisateur.setTelephone("123456789");
//                utilisateur.setEmail("superadmin@mail.com");
//                utilisateur.setPasswordHash(passwordEncoder.encode("12345678"));
//                utilisateurRepository.save(utilisateur);
//
//                System.out.println("--> Administrateur par défaut créé avec succès !");
//            } else {
//                System.out.println("--> Un administrateur existe déjà.");
//            }
//        };
//    }
//}