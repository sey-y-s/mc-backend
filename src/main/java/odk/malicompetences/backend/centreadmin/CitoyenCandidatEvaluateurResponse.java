package odk.malicompetences.backend.centreadmin;
import odk.malicompetences.backend.citoyen.Citoyen;
import java.util.UUID;
public record CitoyenCandidatEvaluateurResponse(UUID id, String nom, String prenom) {
 public static CitoyenCandidatEvaluateurResponse from(Citoyen c) { return new CitoyenCandidatEvaluateurResponse(c.getId(), c.getNom(), c.getPrenom()); }
}
