package odk.malicompetences.backend.citoyen;


import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.experience.Experience;
import odk.malicompetences.backend.experience.ExperienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class CvService {

    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final ExperienceRepository experienceRepository;

    public CvService(
            CitoyenRepository citoyenRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            ExperienceRepository experienceRepository
    ) {
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.experienceRepository = experienceRepository;
    }

    @Transactional(readOnly = true)
    public GeneratedCv generate(UUID utilisateurId) {
        Citoyen citoyen = citoyenRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Profil citoyen introuvable."));
        UUID citoyenId = citoyen.getId();

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph(citoyen.getPrenom() + " " + citoyen.getNom(), titleFont));
            document.add(new Paragraph("Passeport de compétences : " + safe(citoyen.getIdentifiantPublic() == null ? "" : citoyen.getIdentifiantPublic().toString()), bodyFont));
            document.add(new Paragraph("Disponibilité : " + (citoyen.getCompetences() == null || citoyen.getCompetences().stream().noneMatch(CitoyenCompetence::isDisponibilite) ? "Non disponible" : "Disponible pour certaines compétences"), bodyFont));
            document.add(new Paragraph("Commune : " + (citoyen.getCommune() == null ? "Non renseignée" : safe(citoyen.getCommune().getNom())), bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Compétences", headingFont));
            var competences = citoyenCompetenceRepository.findByCitoyenId(citoyenId);
            if (competences.isEmpty()) {
                document.add(new Paragraph("Aucune compétence renseignée.", bodyFont));
            } else {
                for (CitoyenCompetence competence : competences) {
                    String nom = competence.getCompetence() == null ? "Compétence" : safe(competence.getCompetence().getNom());
                    String niveau = competence.getNiveauDeclare() == null ? "Niveau non renseigné" : competence.getNiveauDeclare().name();
                    document.add(new Paragraph("• " + nom + " — " + niveau, bodyFont));
                }
            }
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Expériences", headingFont));
            var experiences = experienceRepository.findByCitoyenId(citoyenId);
            if (experiences.isEmpty()) {
                document.add(new Paragraph("Aucune expérience renseignée.", bodyFont));
            } else {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/yyyy");
                for (Experience experience : experiences) {
                    String periode = experience.getDateDebut() == null ? "" : experience.getDateDebut().format(dateFormat);
                    if (experience.getDateFin() != null) {
                        periode += " – " + experience.getDateFin().format(dateFormat);
                    } else if (experience.isEnCours()) {
                        periode += " – En cours";
                    }
                    document.add(new Paragraph(safe(experience.getTitre()) +
                            (experience.getEntreprise() == null || experience.getEntreprise().isBlank() ? "" : " — " + experience.getEntreprise()) +
                            (periode.isBlank() ? "" : " (" + periode + ")"), headingFont));
                    if (experience.getDescription() != null && !experience.getDescription().isBlank()) {
                        document.add(new Paragraph(experience.getDescription(), bodyFont));
                    }
                }
            }

            document.close();
            return new GeneratedCv("cv-" + citoyenId + ".pdf", output.toByteArray());
        } catch (DocumentException | java.io.IOException exception) {
            throw new IllegalStateException("Impossible de générer le CV PDF.", exception);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Non renseigné" : value;
    }

    public record GeneratedCv(String fileName, byte[] content) {}
}
