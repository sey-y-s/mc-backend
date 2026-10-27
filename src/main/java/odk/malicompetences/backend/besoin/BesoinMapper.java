package odk.malicompetences.backend.besoin;

import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.organisation.Organisation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID; // <--- C'est cet import crucial qui manquait et mettait tout en rouge
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
        besoin.setDateDebut(request.dateDebut());
        besoin.setDateFin(request.dateFin());
        besoin.setDateCreation(LocalDateTime.now());
        return besoin;
    }

    public BesoinCompetence toBesoinCompetenceEntity(BesoinCompetenceRequest request, Besoin besoin, Competence competence) {
        if (request == null) {
            return null;
        }
        BesoinCompetence bc = new BesoinCompetence();
        bc.setBesoin(besoin);
        bc.setCompetence(competence);
        bc.setNiveauMinimum(request.niveauMinimum());
        bc.setQuantite(request.quantite());
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
                besoin.getDateDebut(),
                besoin.getDateFin(),
                besoin.getDateCreation(),
                competenceResponses,
                besoin.getRegion() == null ? null : besoin.getRegion().getId(),
                besoin.getRegion() == null ? null : besoin.getRegion().getNom(),
                besoin.getCommune() == null ? null : besoin.getCommune().getId(),
                besoin.getCommune() == null ? null : besoin.getCommune().getNom()
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
                bc.getNiveauMinimum(),
                bc.getQuantite()
        );
    }
}