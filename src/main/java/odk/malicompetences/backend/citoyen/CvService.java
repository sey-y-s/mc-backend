package odk.malicompetences.backend.citoyen;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.experience.Experience;
import odk.malicompetences.backend.experience.ExperienceRepository;
import odk.malicompetences.backend.testnumerique.ResultatTest;
import odk.malicompetences.backend.testnumerique.ResultatTestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Génère le CV PDF du citoyen à partir de son passeport de compétences.
 * Charte MaliCompétences : vert profond et or, signature de la plateforme en pied de page.
 */
@Service
public class CvService {

    private static final Color VERT = new Color(0x0F, 0x51, 0x32);
    private static final Color OR = new Color(0xC9, 0xA2, 0x27);
    private static final Color OR_CLAIR = new Color(0xFB, 0xF3, 0xD9);
    private static final Color VERT_CLAIR = new Color(0xE6, 0xF1, 0xEB);
    private static final Color GRIS = new Color(0x5F, 0x6B, 0x66);
    private static final Color TRAIT = new Color(0xDD, 0xE3, 0xDF);

    private static final String SIGNATURE = "Généré par MaliCompétences · Votre parcours, vos talents.";

    private final CitoyenRepository citoyenRepository;
    private final CitoyenCompetenceRepository citoyenCompetenceRepository;
    private final ExperienceRepository experienceRepository;
        private final ResultatTestRepository resultatTestRepository;
    private final odk.malicompetences.backend.preuve.PreuveRepository preuveRepository;

    public CvService(
            CitoyenRepository citoyenRepository,
            CitoyenCompetenceRepository citoyenCompetenceRepository,
            ExperienceRepository experienceRepository,
                        ResultatTestRepository resultatTestRepository,
            odk.malicompetences.backend.preuve.PreuveRepository preuveRepository
    ) {
        this.citoyenRepository = citoyenRepository;
        this.citoyenCompetenceRepository = citoyenCompetenceRepository;
        this.experienceRepository = experienceRepository;
                this.resultatTestRepository = resultatTestRepository;
        this.preuveRepository = preuveRepository;
    }

    @Transactional(readOnly = true)
    public GeneratedCv generate(UUID citoyenId) {
        Citoyen citoyen = citoyenRepository.findById(citoyenId)
                .orElseThrow(() -> new IllegalArgumentException("Profil citoyen introuvable."));

                // Un CV n'a de valeur que s'il s'appuie sur du vérifié : une compétence validée, et de quoi la situer
        // (une expérience ou une preuve). Le mobile affiche cette condition avant d'appeler.
        var competencesDuCitoyen = citoyenCompetenceRepository.findByCitoyenId(citoyenId);
        var experiencesDuCitoyen = experienceRepository.findByCitoyenId(citoyenId);
        boolean valide = competencesDuCitoyen.stream().anyMatch(c -> c.getEtat() == EtatCompetence.VALIDEE);
        boolean contexte = !experiencesDuCitoyen.isEmpty()
                || !preuveRepository.findByCitoyenCompetenceCitoyenId(citoyenId).isEmpty();
        if (!valide || !contexte) {
            throw new IllegalStateException("Pour générer votre CV, il faut au moins une compétence validée "
                    + "et une expérience ou une preuve ajoutée.");
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 36, 60);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            writer.setPageEvent(new PiedDePage());
            document.open();

            document.add(entete(citoyen));
            document.add(espace(10));
            document.add(coordonnees(citoyen));

            document.add(titreSection("Compétences"));
            ajouterCompetences(document, citoyenCompetenceRepository.findByCitoyenId(citoyenId));

            document.add(titreSection("Expériences professionnelles"));
            ajouterExperiences(document, experienceRepository.findByCitoyenId(citoyenId));

            List<ResultatTest> reussis = resultatTestRepository.findByCitoyenIdOrderByDatePassageDesc(citoyenId).stream()
                    .filter(r -> Boolean.TRUE.equals(r.getReussi()))
                    .toList();
            if (!reussis.isEmpty()) {
                document.add(titreSection("Tests numériques réussis"));
                ajouterTests(document, reussis);
            }

            document.close();
            return new GeneratedCv("cv-" + citoyenId + ".pdf", output.toByteArray());
        } catch (DocumentException | java.io.IOException exception) {
            throw new IllegalStateException("Impossible de générer le CV PDF.", exception);
        }
    }

    // ------------------------------------------------------------------ blocs

    private PdfPTable entete(Citoyen citoyen) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(VERT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(18);
        cell.addElement(new Paragraph("MALICOMPÉTENCES · PASSEPORT DE COMPÉTENCES", police(8, true, OR)));
        Paragraph nom = new Paragraph(safe(citoyen.getPrenom()) + " " + safe(citoyen.getNom()), police(24, true, Color.WHITE));
        nom.setSpacingBefore(6);
        cell.addElement(nom);
        Paragraph code = new Paragraph("N° " + safe(citoyen.getCodePasseport()), police(10, false, OR_CLAIR));
        code.setSpacingBefore(2);
        cell.addElement(code);
        table.addCell(cell);
        PdfPCell filet = new PdfPCell(new Phrase(" ", police(2, false, OR)));
        filet.setBackgroundColor(OR);
        filet.setBorder(Rectangle.NO_BORDER);
        filet.setFixedHeight(4);
        table.addCell(filet);
        return table;
    }

    private PdfPTable coordonnees(Citoyen citoyen) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1, 1});
        table.setWidthPercentage(100);
        var utilisateur = citoyen.getUtilisateur();
        String commune = citoyen.getCommune() == null ? "Non renseignée" : safe(citoyen.getCommune().getNom())
                + (citoyen.getCommune().getRegion() == null ? "" : ", " + safe(citoyen.getCommune().getRegion().getNom()));
        table.addCell(info("Téléphone", utilisateur == null ? null : utilisateur.getTelephone()));
        table.addCell(info("E-mail", utilisateur == null ? null : utilisateur.getEmail()));
        table.addCell(info("Commune", commune));
        table.addCell(info("Disponibilité", libelleDisponibilite(citoyen.getDisponibilite())));
        return table;
    }

    private PdfPCell info(String libelle, String valeur) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(4);
        cell.setPaddingBottom(4);
        cell.addElement(new Paragraph(libelle.toUpperCase(Locale.ROOT), police(7, true, GRIS)));
        cell.addElement(new Paragraph(safe(valeur), police(10, false, Color.BLACK)));
        return cell;
    }

    private PdfPTable titreSection(String titre) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(16);
        table.setSpacingAfter(6);
        PdfPCell cell = new PdfPCell(new Phrase(titre.toUpperCase(Locale.ROOT), police(11, true, VERT)));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColorBottom(OR);
        cell.setBorderWidthBottom(1.5f);
        cell.setPaddingLeft(0);
        cell.setPaddingBottom(4);
        table.addCell(cell);
        return table;
    }

    private void ajouterCompetences(Document document, List<CitoyenCompetence> competences) throws DocumentException {
        if (competences.isEmpty()) {
            document.add(new Paragraph("Aucune compétence renseignée.", police(10, false, GRIS)));
            return;
        }
        // Les compétences validées d'abord : ce sont celles qui rassurent un recruteur.
        List<CitoyenCompetence> triees = competences.stream()
                .sorted(Comparator.comparingInt((CitoyenCompetence c) -> rang(c.getEtat())))
                .toList();
        PdfPTable table = new PdfPTable(new float[]{5, 2.2f, 2.4f});
        table.setWidthPercentage(100);
        for (String entete : new String[]{"Compétence", "Niveau", "Statut"}) {
            PdfPCell cell = new PdfPCell(new Phrase(entete, police(8, true, Color.WHITE)));
            cell.setBackgroundColor(VERT);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(6);
            table.addCell(cell);
        }
        for (CitoyenCompetence c : triees) {
            boolean validee = c.getEtat() == EtatCompetence.VALIDEE;
            var niveau = validee && c.getNiveauValide() != null ? c.getNiveauValide() : c.getNiveau();
            table.addCell(ligne(c.getCompetence() == null ? "Compétence" : safe(c.getCompetence().getNom()), police(10, validee, Color.BLACK), null));
            table.addCell(ligne(niveau == null ? "—" : libelleNiveau(niveau.name()), police(9, false, Color.BLACK), null));
            table.addCell(ligne(libelleEtat(c.getEtat()), police(9, true, validee ? VERT : GRIS), validee ? VERT_CLAIR : null));
        }
        document.add(table);
        document.add(new Paragraph("« Validée » : confirmée par un évaluateur, un centre agréé ou un administrateur de la plateforme.",
                police(7, false, GRIS)));
    }

    private PdfPCell ligne(String texte, Font font, Color fond) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColorBottom(TRAIT);
        cell.setPadding(6);
        if (fond != null) cell.setBackgroundColor(fond);
        return cell;
    }

    private void ajouterExperiences(Document document, List<Experience> experiences) throws DocumentException {
        if (experiences.isEmpty()) {
            document.add(new Paragraph("Aucune expérience renseignée.", police(10, false, GRIS)));
            return;
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/yyyy");
        List<Experience> triees = experiences.stream()
                .sorted(Comparator.comparing(Experience::getDateDebut, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())))
                .toList();
        for (Experience experience : triees) {
            String periode = experience.getDateDebut() == null ? "" : experience.getDateDebut().format(format);
            if (experience.getDateFin() != null) {
                periode += " – " + experience.getDateFin().format(format);
            } else if (Boolean.TRUE.equals(experience.getEnCours())) {
                periode += " – En cours";
            }
            Paragraph titre = new Paragraph(safe(experience.getTitre()), police(11, true, Color.BLACK));
            titre.setSpacingBefore(6);
            document.add(titre);
            boolean entreprise = experience.getEntreprise() != null && !experience.getEntreprise().isBlank();
            String sousTitre = (entreprise ? experience.getEntreprise() : "")
                    + (periode.isBlank() ? "" : (entreprise ? " · " : "") + periode);
            if (!sousTitre.isBlank()) {
                document.add(new Paragraph(sousTitre, police(9, false, GRIS)));
            }
            if (experience.getDescription() != null && !experience.getDescription().isBlank()) {
                document.add(new Paragraph(experience.getDescription(), police(10, false, Color.BLACK)));
            }
        }
    }

    private void ajouterTests(Document document, List<ResultatTest> resultats) throws DocumentException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        PdfPTable table = new PdfPTable(new float[]{5, 1.6f, 2});
        table.setWidthPercentage(100);
        for (ResultatTest r : resultats) {
            String titre = r.getTest() == null ? "Test numérique" : safe(r.getTest().getTitre());
            table.addCell(ligne(titre, police(10, false, Color.BLACK), null));
            table.addCell(ligne(r.getScore() == null ? "—" : r.getScore().stripTrailingZeros().toPlainString() + " %", police(10, true, VERT), null));
            table.addCell(ligne(r.getDatePassage() == null ? "" : r.getDatePassage().format(format), police(9, false, GRIS), null));
        }
        document.add(table);
    }

    private Paragraph espace(float hauteur) {
        Paragraph p = new Paragraph(" ", police(hauteur, false, Color.WHITE));
        p.setLeading(hauteur);
        return p;
    }

    // ------------------------------------------------------------------ pied de page signé

    private static final class PiedDePage extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float gauche = document.left();
            float droite = document.right();
            cb.setColorStroke(OR);
            cb.setLineWidth(1f);
            cb.moveTo(gauche, 42);
            cb.lineTo(droite, 42);
            cb.stroke();
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, GRIS);
            Font gras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, VERT);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(SIGNATURE, gras), gauche, 30, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Édité le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " · page " + writer.getPageNumber(), font), droite, 30, 0);
        }
    }

    // ------------------------------------------------------------------ libellés

    private static Font police(float taille, boolean gras, Color couleur) {
        return FontFactory.getFont(gras ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, taille, Font.NORMAL, couleur);
    }

    private static int rang(EtatCompetence etat) {
        if (etat == EtatCompetence.VALIDEE) return 0;
        if (etat == EtatCompetence.ATTESTEE) return 1;
        return 2;
    }

    private static String libelleEtat(EtatCompetence etat) {
        if (etat == EtatCompetence.VALIDEE) return "Validée";
        if (etat == EtatCompetence.ATTESTEE) return "Preuve reçue";
        return "Déclarée";
    }

    private static String libelleNiveau(String niveau) {
        return switch (niveau) {
            case "DEBUTANT" -> "Débutant";
            case "INTERMEDIAIRE" -> "Intermédiaire";
            case "EXPERT" -> "Expert";
            default -> niveau;
        };
    }

    private static String libelleDisponibilite(Disponibilite disponibilite) {
        if (disponibilite == null) return "Non renseignée";
        return switch (disponibilite) {
            case DISPONIBLE -> "Disponible";
            case OCCUPE -> "Occupé";
            case EN_APPRENTISSAGE -> "En apprentissage";
            case NON_DISPONIBLE -> "Non disponible";
        };
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "Non renseigné" : value;
    }

    public record GeneratedCv(String fileName, byte[] content) {}
}
