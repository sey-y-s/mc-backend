package odk.malicompetences.backend.auth;

import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRegles;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.organisation.StatutOrganisation;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import org.springframework.stereotype.Component;

/** Profil d'un compte organisation : identité juridique, en attente de validation par l'administration. */
@Component
public class ProfilOrganisationInitialiseur implements ProfilInitialiseur {

    private final OrganisationRepository organisationRepository;
    private final CommuneRepository communeRepository;

    public ProfilOrganisationInitialiseur(OrganisationRepository organisationRepository,
                                          CommuneRepository communeRepository) {
        this.organisationRepository = organisationRepository;
        this.communeRepository = communeRepository;
    }

    @Override
    public Role role() {
        return Role.ORGANISATION;
    }

    @Override
    public void verifier(RegisterRequest request) {
        if (request.nomOrganisation() == null || request.nomOrganisation().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'organisation est obligatoire");
        }
        if (request.nomOrganisation().length() > 150) {
            throw new IllegalArgumentException("Le nom de l'organisation ne doit pas dépasser 150 caractères");
        }
        if (request.organisation() != null) {
            OrganisationRegles.verifier(request.organisation());
        }
    }

    @Override
    public void creer(Utilisateur compte, RegisterRequest request) {
        Organisation organisation = new Organisation();
        organisation.setUtilisateur(compte);
        organisation.setNom(request.nomOrganisation().trim());
        organisation.setDescription(request.descriptionOrganisation());
        organisation.setStatut(StatutOrganisation.EN_ATTENTE);
        if (request.organisation() != null) {
            OrganisationRegles.appliquer(request.organisation(), organisation, communeRepository);
        }
        organisationRepository.save(organisation);
    }
}
