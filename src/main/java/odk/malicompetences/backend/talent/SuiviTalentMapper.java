package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.organisation.Organisation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class SuiviTalentMapper {

    public SuiviTalent toEntity(SuiviTalentRequest req, Organisation organisation, Besoin besoin, Citoyen citoyen) {
        if (req == null) return null;

        SuiviTalent suivi = new SuiviTalent();
        suivi.setOrganisation(organisation);
        suivi.setBesoin(besoin);
        suivi.setCitoyen(citoyen);
        suivi.setStatut(req.statut());

        // Conversion de BigDecimal vers Double pour l'entité
        suivi.setScoreCorrespondance(BigDecimal.valueOf(req.scoreCorrespondance() != null ? req.scoreCorrespondance().doubleValue() : null));

        suivi.setDetailsCorrespondance(req.detailsCorrespondance());
        suivi.setNoteInterne(req.noteInterne());

        LocalDateTime now = LocalDateTime.now();
        suivi.setDateCreation(now);
        suivi.setDateMiseAJour(now);

        return suivi;
    }

    public void updateEntity(SuiviTalentRequest req, SuiviTalent suivi) {
        if (req != null && suivi != null) {
            suivi.setStatut(req.statut());
            if (req.scoreCorrespondance() != null) {
                suivi.setScoreCorrespondance(BigDecimal.valueOf(req.scoreCorrespondance().doubleValue()));
            }
            if (req.detailsCorrespondance() != null) suivi.setDetailsCorrespondance(req.detailsCorrespondance());
            if (req.noteInterne() != null) suivi.setNoteInterne(req.noteInterne());
            suivi.setDateMiseAJour(LocalDateTime.now());
        }
    }

    public SuiviTalentResponse toResponse(SuiviTalent s) {
        if (s == null) return null;

        UUID orgId = s.getOrganisation() != null ? s.getOrganisation().getId() : null;
        UUID besoinId = s.getBesoin() != null ? s.getBesoin().getId() : null;
        String besoinTitre = s.getBesoin() != null ? s.getBesoin().getTitre() : null;
        UUID citoyenId = s.getCitoyen() != null ? s.getCitoyen().getId() : null;
        String citoyenNomComplet = s.getCitoyen() != null ? s.getCitoyen().getPrenom() + " " + s.getCitoyen().getNom() : null;

        // Conversion sécurisée de Double vers BigDecimal pour le Response
        BigDecimal scoreBd = s.getScoreCorrespondance() != null ? s.getScoreCorrespondance() : null;

        return new SuiviTalentResponse(
                s.getId(),
                orgId,
                besoinId,
                besoinTitre,
                citoyenId,
                citoyenNomComplet,
                s.getStatut(),
                scoreBd, // <--- Utilisation du BigDecimal converti
                s.getDetailsCorrespondance(),
                s.getNoteInterne(),
                s.getDateCreation(),
                s.getDateMiseAJour()
        );
    }
}