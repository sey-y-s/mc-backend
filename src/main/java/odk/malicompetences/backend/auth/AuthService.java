//package odk.malicompetences.backend.auth;
//
//import odk.malicompetences.backend.common.security.JwtService;
//import odk.malicompetences.backend.utilisateur.Utilisateur;
//import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AuthService {
//
//    private final UtilisateurRepository utilisateurRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//
//    public AuthService(
//            UtilisateurRepository utilisateurRepository,
//            PasswordEncoder passwordEncoder,
//            JwtService jwtService
//    ) {
//        this.utilisateurRepository = utilisateurRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtService = jwtService;
//    }
//
//    public LoginResponse login(LoginRequest request) {
//
//        Utilisateur utilisateur =
//                utilisateurRepository.findByTelephone(
//                        request.identifiant()
//                ).orElseGet(() ->
//                        utilisateurRepository.findByEmail(
//                                request.identifiant()
//                        ).orElseThrow(() ->
//                                new IllegalArgumentException(
//                                        "Identifiants incorrects"
//                                )
//                        )
//                );
//
//        if (!utilisateur.getActif()
//                || !passwordEncoder.matches(
//                request.password(),
//                utilisateur.getPasswordHash()
//        )) {
//
//            throw new IllegalArgumentException(
//                    "Identifiants incorrects"
//            );
//        }
//
//        return new LoginResponse(
//                jwtService.generateToken(utilisateur.getId())
//        );
//    }
//}