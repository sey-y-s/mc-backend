package odk.malicompetences.backend.cv;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.experience.Experience;
import odk.malicompetences.backend.experience.ExperienceRepository;
import odk.malicompetences.backend.portfolio.PortfolioMediaStorageService;
import odk.malicompetences.backend.testnumerique.ResultatTest;
import odk.malicompetences.backend.testnumerique.ResultatTestRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CvService {

    private static final Color NAVY = new Color(30, 54, 84);
    private static final Color BLUE = new Color(40, 103, 157);
    private static final Color MUTED = new Color(95, 108, 122);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository competenceRepository;
    private final ExperienceRepository experienceRepository;
    private final ResultatTestRepository resultatTestRepository;
    private final PortfolioMediaStorageService mediaStorage;

    public CvService(
            CitoyenRepository citoyenRepository,
            CitoyenCompetenceRepository competenceRepository,
            ExperienceRepository experienceRepository,
            ResultatTestRepository resultatTestRepository,
            PortfolioMediaStorageService mediaStorage
    ) {
        this.citoyenRepository = citoyenRepository;
        this.competenceRepository = competenceRepository;
        this.experienceRepository = experienceRepository;
        this.resultatTestRepository = resultatTestRepository;
        this.mediaStorage = mediaStorage;
    }

    @Transactional(readOnly = true)
    public GeneratedCv generate(UUID citoyenId) {
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable."));
        List<CitoyenCompetence> competences = competenceRepository.findByCitoyenId(citoyenId).stream()
                .filter(competence -> competence.getEtat() == EtatCompetence.VALIDEE)
                .toList();
        List<Experience> experiences = experienceRepository.findByCitoyenId(citoyenId).stream()
                .sorted(Comparator.comparing(Experience::getDateDebut).reversed())
                .toList();
        List<ResultatTest> resultats = resultatTestRepository.findByCitoyenIdOrderByDatePassageDesc(citoyenId);

        return new GeneratedCv(render(citoyen, competences, experiences, resultats),
                fileName(citoyen));
    }

    private byte[] render(
            Citoyen citoyen,
            List<CitoyenCompetence> competences,
            List<Experience> experiences,
            List<ResultatTest> resultats
    ) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 40, 42);
            PdfWriter.getInstance(document, output);
            document.open();

            addHeader(document, citoyen);
            addSection(document, "PROFIL");
            addParagraph(document, "Code passeport : " + citoyen.getCodePasseport(), bodyFont());
            addParagraph(document, "Disponibilité : " + readable(citoyen.getDisponibilite().name()), bodyFont());

            addSection(document, "COMPÉTENCES VALIDÉES");
            if (competences.isEmpty()) {
                addParagraph(document, "Aucune compétence validée pour le moment.", mutedFont());
            } else {
                for (CitoyenCompetence competence : competences) {
                    String level = readable(competence.getNiveau().name());
                    addBullet(document, competence.getCompetence().getNom() + " — " + level);
                }
            }

            addSection(document, "EXPÉRIENCES");
            if (experiences.isEmpty()) {
                addParagraph(document, "Aucune expérience renseignée.", mutedFont());
            } else {
                for (Experience experience : experiences) {
                    Paragraph title = new Paragraph(experience.getTitre(), subheadingFont());
                    if (experience.getEntreprise() != null && !experience.getEntreprise().isBlank()) {
                        title.add(new Chunk("  -  " + experience.getEntreprise(), mutedFont()));
                    }
                    document.add(title);
                    String dates = formatDate(experience.getDateDebut()) + " - "
                            + (Boolean.TRUE.equals(experience.getEnCours())
                            ? "Aujourd'hui"
                            : formatDate(experience.getDateFin()));
                    addParagraph(document, dates, mutedFont());
                    if (experience.getDescription() != null && !experience.getDescription().isBlank()) {
                        addParagraph(document, experience.getDescription(), bodyFont());
                    }
                    document.add(Chunk.NEWLINE);
                }
            }

            addSection(document, "TESTS NUMÉRIQUES RÉALISÉS");
            if (resultats.isEmpty()) {
                addParagraph(document, "Aucun test numérique réalisé.", mutedFont());
            } else {
                for (ResultatTest resultat : resultats) {
                    addBullet(document, resultat.getTest().getTitre()
                            + " - " + resultat.getScore().stripTrailingZeros().toPlainString() + " %"
                            + " - " + (Boolean.TRUE.equals(resultat.getReussi()) ? "Réussi" : "Non réussi")
                            + " - " + formatDate(resultat.getDatePassage()));
                }
            }

            document.close();
            return output.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new IllegalStateException("Impossible de générer le CV.", exception);
        }
    }

    private void addHeader(Document document, Citoyen citoyen) throws DocumentException, IOException {
        PdfPTable header = new PdfPTable(new float[]{1, 4});
        header.setWidthPercentage(100);
        header.setSpacingAfter(18);

        PdfPCell photoCell = new PdfPCell();
        photoCell.setBorder(Rectangle.NO_BORDER);
        photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image photo = profilePhoto(citoyen.getPhotoUrl());
        if (photo != null) {
            photo.scaleToFit(78, 78);
            photo.setAlignment(Image.ALIGN_CENTER);
            photoCell.addElement(photo);
        } else {
            photoCell.setFixedHeight(78);
            photoCell.setBackgroundColor(new Color(231, 239, 247));
            Paragraph initials = new Paragraph(initials(citoyen), new Font(Font.HELVETICA, 22, Font.BOLD, BLUE));
            initials.setAlignment(Element.ALIGN_CENTER);
            photoCell.addElement(initials);
        }
        header.addCell(photoCell);

        PdfPCell identity = new PdfPCell();
        identity.setBorder(Rectangle.NO_BORDER);
        identity.setVerticalAlignment(Element.ALIGN_MIDDLE);
        identity.addElement(new Paragraph(citoyen.getPrenom() + " " + citoyen.getNom(),
                new Font(Font.HELVETICA, 22, Font.BOLD, NAVY)));
        identity.addElement(new Paragraph(contactLine(citoyen), mutedFont()));
        if (citoyen.getCommune() != null) {
            identity.addElement(new Paragraph(
                    citoyen.getCommune().getNom() + ", " + citoyen.getCommune().getRegion().getNom(),
                    mutedFont()));
        }
        header.addCell(identity);
        document.add(header);
        LineSeparator line = new LineSeparator(1, 100, BLUE, Element.ALIGN_LEFT, 0);
        document.add(line);
        document.add(Chunk.NEWLINE);
    }

    private Image profilePhoto(String photoUrl) throws IOException, BadElementException {
        if (photoUrl == null || !photoUrl.startsWith(PortfolioMediaStorageService.MEDIA_URL_PREFIX)) {
            return null;
        }
        String fileName = photoUrl.substring(PortfolioMediaStorageService.MEDIA_URL_PREFIX.length());
        if (!(fileName.toLowerCase().endsWith(".jpg")
                || fileName.toLowerCase().endsWith(".jpeg")
                || fileName.toLowerCase().endsWith(".png"))) {
            return null;
        }
        Resource resource = mediaStorage.load(fileName);
        try (var input = resource.getInputStream()) {
            return Image.getInstance(input.readAllBytes());
        }
    }

    private String contactLine(Citoyen citoyen) {
        var utilisateur = citoyen.getUtilisateur();
        String email = utilisateur.getEmail();
        String telephone = utilisateur.getTelephone();
        if (email != null && !email.isBlank() && telephone != null && !telephone.isBlank()) {
            return email + "  -  " + telephone;
        }
        if (email != null && !email.isBlank()) return email;
        if (telephone != null && !telephone.isBlank()) return telephone;
        return "Contact non renseigné";
    }

    private void addSection(Document document, String title) throws DocumentException {
        Paragraph heading = new Paragraph(title, new Font(Font.HELVETICA, 11, Font.BOLD, BLUE));
        heading.setSpacingBefore(12);
        heading.setSpacingAfter(6);
        document.add(heading);
    }

    private void addBullet(Document document, String text) throws DocumentException {
        Paragraph bullet = new Paragraph("-  " + text, bodyFont());
        bullet.setIndentationLeft(8);
        bullet.setSpacingAfter(5);
        document.add(bullet);
    }

    private void addParagraph(Document document, String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingAfter(4);
        document.add(paragraph);
    }

    private Font bodyFont() {
        return new Font(Font.HELVETICA, 10, Font.NORMAL, NAVY);
    }

    private Font mutedFont() {
        return new Font(Font.HELVETICA, 9, Font.NORMAL, MUTED);
    }

    private Font subheadingFont() {
        return new Font(Font.HELVETICA, 11, Font.BOLD, NAVY);
    }

    private String initials(Citoyen citoyen) {
        return (citoyen.getPrenom().substring(0, 1) + citoyen.getNom().substring(0, 1))
                .toUpperCase();
    }

    private String readable(String value) {
        String lower = value.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "En cours" : date.format(DATE);
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE);
    }

    private String fileName(Citoyen citoyen) {
        String name = Normalizer.normalize(citoyen.getPrenom() + "-" + citoyen.getNom(),
                        Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return "CV-" + (name.isBlank() ? "citoyen" : name) + ".pdf";
    }

    public record GeneratedCv(byte[] content, String fileName) {
    }
}
