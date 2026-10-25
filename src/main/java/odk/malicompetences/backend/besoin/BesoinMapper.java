package odk.malicompetences.backend.besoin;


import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.organisation.Organisation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BesoinMapper {

    public Besoin toEntity(BesoinRequest request, Organisation organisation) {
        if (request == null) {
            return null;
        }
        Besoin besoin = new Besoin();
        besoin.setOrganisation(organisation);
        besoin.setTitre(request.titre());
        besoin.setDescription(request.description());
        besoin.setStatut(request.statut());
        besoin.setDateLimite(request.dateLimite());
        if (request.statut() == StatutBesoin.OUVERT) besoin.setPublieLe(LocalDateTime.now());
        return besoin;
    }

    public BesoinCompetence toBesoinCompetenceEntity(BesoinCompetenceRequest request, Besoin besoin, Competence competence) {
        if (request == null) {
            return null;
        }
        BesoinCompetence bc = new BesoinCompetence();
        bc.setBesoin(besoin);
        bc.setCompetence(competence);
        bc.setNiveauRequis(request.niveauRequis());
        bc.setNombreRequis(request.nombreRequis() == null ? 1 : request.nombreRequis());
        return bc;
    }

    public BesoinResponse toResponse(Besoin besoin, List<BesoinCompetence> besoinCompetences) {
        if (besoin == null) {
            return null;
        }

        List<BesoinCompetenceResponse> competenceResponses = besoinCompetences != null
                ? besoinCompetences.stream().map(this::toBesoinCompetenceResponse).collect(Collectors.toList())
                : List.of();

        UUID orgId = besoin.getOrganisation() != null ? besoin.getOrganisation().getId() : null;
        String orgNom = besoin.getOrganisation() != null ? besoin.getOrganisation().getNom() : null;

        return new BesoinResponse(
                besoin.getId(),
                orgId,
                orgNom,
                besoin.getTitre(),
                besoin.getDescription(),
                besoin.getStatut(),
                besoin.getDateLimite(),
                besoin.getDateCreation(),
                competenceResponses
        );
    }

    public BesoinCompetenceResponse toBesoinCompetenceResponse(BesoinCompetence bc) {
        if (bc == null) {
            return null;
        }
        UUID compId = bc.getCompetence() != null ? bc.getCompetence().getId() : null;
        String compNom = bc.getCompetence() != null ? bc.getCompetence().getNom() : null;

        return new BesoinCompetenceResponse(
                bc.getId(),
                compId,
                compNom,
                bc.getNiveauRequis(),
                bc.getNombreRequis()
        );
    }
}