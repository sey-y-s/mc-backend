package odk.malicompetences.backend.miseenrelation;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.besoin.BesoinRepository;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service @Transactional
public class ConsultationProfilService {
    private final ConsultationProfilRepository consultationRepository;
    private final CitoyenRepository citoyenRepository;
    private final OrganisationRepository organisationRepository;
    private final BesoinRepository besoinRepository;

    public ConsultationProfilService(ConsultationProfilRepository consultationRepository,
                                     CitoyenRepository citoyenRepository,
                                     OrganisationRepository organisationRepository,
                                     BesoinRepository besoinRepository) {
        this.consultationRepository = consultationRepository;
        this.citoyenRepository = citoyenRepository;
        this.organisationRepository = organisationRepository;
        this.besoinRepository = besoinRepository;
    }

    /** Journal interne : aucune route ne restitue ces consultations au talent consulté. */
    public void enregistrerConsultation(UUID utilisateurId, UUID identifiantPublicCitoyen) {
        enregistrerConsultation(utilisateurId, identifiantPublicCitoyen, null);
    }

    public void enregistrerConsultation(UUID utilisateurId, UUID identifiantPublicCitoyen, UUID besoinId) {
        Organisation organisation = organisationRepository.findByUtilisateur_Id(utilisateurId)
                .orElseThrow(() -> new AccessDeniedException("Seule une organisation peut consulter un talent."));
        if (organisation.getStatut() != StatutOrganisation.APPROUVEE)
            throw new AccessDeniedException("L'organisation doit être approuvée.");
        Citoyen citoyen = citoyenRepository.findByIdentifiantPublic(identifiantPublicCitoyen)
                .filter(Citoyen::isProfilPublic)
                .orElseThrow(() -> new ResourceNotFoundException("Profil talent introuvable."));
        ConsultationProfil consultation = new ConsultationProfil();
        consultation.setOrganisation(organisation);
        consultation.setCitoyen(citoyen);
        if (besoinId != null) {
            var besoin = besoinRepository.findById(besoinId)
                    .orElseThrow(() -> new ResourceNotFoundException("Besoin introuvable."));
            if (!besoin.getOrganisation().getId().equals(organisation.getId()))
                throw new AccessDeniedException("Ce besoin ne vous appartient pas.");
            consultation.setBesoin(besoin);
        }
        consultationRepository.save(consultation);
    }

}
