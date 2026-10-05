package odk.malicompetences.backend.miseenrelation;

import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsultationProfilService {

    private final ConsultationProfilRepository consultationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CitoyenRepository citoyenRepository;

    public ConsultationProfilService(
            ConsultationProfilRepository consultationRepository,
            UtilisateurRepository utilisateurRepository,
            CitoyenRepository citoyenRepository
    ) {
        this.consultationRepository = consultationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.citoyenRepository = citoyenRepository;
    }

    public ConsultationProfilResponse enregistrerConsultation(ConsultationProfilRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(request.utilisateurId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Citoyen citoyen = citoyenRepository.findById(request.citoyenId())
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));

        ConsultationProfil consultation = new ConsultationProfil();
        // Si les getters/setters manquent sur l'entité ConsultationProfil fournie, assurez-vous d'ajouter Lombok ou les méthodes set/get.
        // consultation.setUtilisateur(utilisateur);
        // consultation.setCitoyen(citoyen);
        // consultation.setDateConsultation(LocalDateTime.now());

        ConsultationProfil saved = consultationRepository.save(consultation);

        return new ConsultationProfilResponse(
                saved.getId(),
                utilisateur.getId(),
                citoyen.getId(),
                citoyen.getPrenom() + " " + citoyen.getNom(),
                saved.getDateConsultation()
        );
    }

    @Transactional(readOnly = true)
    public List<ConsultationProfilResponse> listerConsultationsParCitoyen(UUID citoyenId) {
        return consultationRepository.findAll().stream()
                .filter(c -> c.getCitoyen().getId().equals(citoyenId))
                .map(c -> new ConsultationProfilResponse(
                        c.getId(),
                        c.getUtilisateur().getId(),
                        c.getCitoyen().getId(),
                        c.getCitoyen().getPrenom() + " " + c.getCitoyen().getNom(),
                        c.getDateConsultation()
                ))
                .collect(Collectors.toList());
    }
}