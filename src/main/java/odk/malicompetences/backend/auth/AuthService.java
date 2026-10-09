package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.citoyen.Disponibilite;
import odk.malicompetences.backend.common.security.JwtService;
import odk.malicompetences.backend.common.validation.NumeroTelephone;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenRepository citoyenRepository;
    private final OrganisationRepository organisationRepository;
    private final CommuneRepository communeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       CitoyenRepository citoyenRepository,
                       OrganisationRepository organisationRepository,
                       CommuneRepository communeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.citoyenRepository = citoyenRepository;
        this.organisationRepository = organisationRepository;
        this.communeRepository = communeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String saisie = request.identifiant().trim();
        String identifiant = NumeroTelephone.ressembleANumero(saisie) ? NumeroTelephone.normaliser(saisie) : saisie.toLowerCase(Locale.ROOT);


        Utilisateur utilisateur = utilisateurRepository.findByTelephone(identifiant)
                .orElseGet(() -> utilisateurRepository.findByEmail(identifiant.toLowerCase(Locale.ROOT))
                        .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects")));

        if (!utilisateur.isActif()
                || !passwordEncoder.matches(request.password(), utilisateur.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants incorrects");
        }

        return new LoginResponse(jwtService.generateToken(utilisateur));
    }

    public void inscrire(RegisterRequest request) {
        String telephone = NumeroTelephone.normaliser(request.telephone());
        String email = request.email() == null || request.email().isBlank()
                ? null : request.email().trim().toLowerCase(Locale.ROOT);

        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException("Ce numéro de téléphone est déjà utilisé");
        }
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }

        Role role = request.role() == null ? Role.CITOYEN : request.role();
        if (role != Role.CITOYEN && role != Role.ORGANISATION) {
            throw new IllegalArgumentException("Ce rôle ne peut pas être créé par inscription publique");
        }
        verifierDonneesProfil(request, role);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);
        utilisateur.setPasswordHash(passwordEncoder.encode(request.password()));
        utilisateur.setRole(role);
        utilisateur.setActif(true);

        if (role == Role.CITOYEN) {
            Citoyen citoyen = new Citoyen();
            citoyen.setUtilisateur(utilisateur);
            citoyen.setCodePasseport(genererCodePasseport());
            citoyen.setNom(request.nom().trim());
            citoyen.setPrenom(request.prenom().trim());
            citoyen.setSexe(request.sexe());
            citoyen.setPhotoUrl(request.photoUrl());
            citoyen.setDisponibilite(Disponibilite.DISPONIBLE);
            if (request.communeId() != null) {
                citoyen.setCommune(communeRepository.findById(request.communeId())
                        .orElseThrow(() -> new IllegalArgumentException("Commune introuvable")));
            }
            utilisateur.setCitoyen(citoyen);
        } else {
            Organisation organisation = new Organisation();
            organisation.setUtilisateur(utilisateur);
            organisation.setNom(request.nomOrganisation().trim());
            organisation.setDescription(request.descriptionOrganisation());
            organisation.setStatut(StatutOrganisation.EN_ATTENTE);

            utilisateur.setOrganisation(organisation);
        }

        utilisateurRepository.save(utilisateur);
    }

    private void verifierDonneesProfil(RegisterRequest request, Role role) {
        if (role == Role.CITOYEN) {
            if (request.nom() == null || request.nom().isBlank()
                    || request.prenom() == null || request.prenom().isBlank()) {
                throw new IllegalArgumentException("Le nom et le prénom sont obligatoires pour un citoyen");
            }
            if (request.nom().length() > 100 || request.prenom().length() > 100) {
                throw new IllegalArgumentException("Le nom et le prénom ne doivent pas dépasser 100 caractères");
            }
            if (request.photoUrl() != null && request.photoUrl().length() > 500) {
                throw new IllegalArgumentException("L'URL de la photo ne doit pas dépasser 500 caractères");
            }
        } else {
            if (request.nomOrganisation() == null || request.nomOrganisation().isBlank()) {
                throw new IllegalArgumentException("Le nom de l'organisation est obligatoire");
            }
            if (request.nomOrganisation().length() > 150) {
                throw new IllegalArgumentException("Le nom de l'organisation ne doit pas dépasser 150 caractères");
            }
        }
    }

    private String genererCodePasseport() {
        return "MC-" + java.time.Year.now().getValue() + "-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(Locale.ROOT);
    }
}
