package odk.malicompetences.backend.organisation;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service @Transactional
public class JustificatifOrganisationService {
    private final JustificatifOrganisationRepository repository;
    private final OrganisationRepository organisationRepository;
    private final UtilisateurRepository utilisateurRepository;
    public JustificatifOrganisationService(JustificatifOrganisationRepository repository,
            OrganisationRepository organisationRepository, UtilisateurRepository utilisateurRepository) {
        this.repository = repository; this.organisationRepository = organisationRepository; this.utilisateurRepository = utilisateurRepository;
    }

    public JustificatifOrganisationResponse deposer(JustificatifOrganisationRequest request) {
        UUID utilisateurId = utilisateurConnecteId();
        Organisation organisation = organisationRepository.findByUtilisateur_Id(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil organisation introuvable."));
        if (request.dateEmission() != null && request.dateExpiration() != null && request.dateExpiration().isBefore(request.dateEmission()))
            throw new IllegalArgumentException("La date d'expiration doit être postérieure à la date d'émission.");
        JustificatifOrganisation j = new JustificatifOrganisation();
        j.setOrganisation(organisation); j.setTypeJustificatif(request.typeJustificatif().trim());
        j.setReferenceDocument(normaliser(request.referenceDocument())); j.setCleStockagePrive(normaliser(request.cleStockagePrive()));
        j.setDateEmission(request.dateEmission()); j.setDateExpiration(request.dateExpiration());
        j.setStatut(request.dateExpiration() != null && request.dateExpiration().isBefore(LocalDate.now())
                ? StatutJustificatifOrganisation.EXPIRE : StatutJustificatifOrganisation.RENSEIGNE);
        return response(repository.save(j));
    }

    @Transactional(readOnly = true)
    public List<JustificatifOrganisationResponse> mesJustificatifs() {
        Organisation organisation = organisationRepository.findByUtilisateur_Id(utilisateurConnecteId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil organisation introuvable."));
        return repository.findByOrganisation_IdOrderByIdDesc(organisation.getId()).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<JustificatifOrganisationResponse> pourOrganisation(UUID organisationId) {
        if (!organisationRepository.existsById(organisationId)) throw new ResourceNotFoundException("Organisation introuvable.");
        return repository.findByOrganisation_IdOrderByIdDesc(organisationId).stream().map(this::response).toList();
    }

    public JustificatifOrganisationResponse examiner(UUID id, StatutJustificatifOrganisation statut, String motif) {
        if (statut != StatutJustificatifOrganisation.VERIFIE && statut != StatutJustificatifOrganisation.REJETE)
            throw new IllegalArgumentException("La décision doit être VERIFIE ou REJETE.");
        if (statut == StatutJustificatifOrganisation.REJETE && (motif == null || motif.isBlank()))
            throw new IllegalArgumentException("Un motif est obligatoire en cas de rejet.");
        JustificatifOrganisation j = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Justificatif introuvable."));
        if (j.getStatut() == StatutJustificatifOrganisation.EXPIRE)
            throw new IllegalStateException("Un justificatif expiré ne peut pas être vérifié.");
        Utilisateur decideur = utilisateurRepository.findById(utilisateurConnecteId())
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur introuvable."));
        j.setStatut(statut); j.setVerifiePar(decideur); j.setVerifieLe(LocalDateTime.now());
        j.setMotifRejet(statut == StatutJustificatifOrganisation.REJETE ? motif.trim() : null);
        return response(repository.save(j));
    }

    private JustificatifOrganisationResponse response(JustificatifOrganisation j) {
        return new JustificatifOrganisationResponse(j.getId(), j.getOrganisation().getId(), j.getTypeJustificatif(),
                j.getReferenceDocument(), j.getDateEmission(), j.getDateExpiration(), j.getStatut(), j.getVerifieLe(), j.getMotifRejet());
    }
    private String normaliser(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private UUID utilisateurConnecteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try { return UUID.fromString(auth == null ? "" : auth.getName()); }
        catch (IllegalArgumentException e) { throw new AccessDeniedException("Authentification requise."); }
    }
}
