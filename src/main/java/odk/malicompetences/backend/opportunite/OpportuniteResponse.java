package odk.malicompetences.backend.opportunite;
import java.time.LocalDate; import java.time.LocalDateTime; import java.util.UUID;
public record OpportuniteResponse(UUID id,UUID categorieId,String categorieNom,String titre,String description,String organisme,String lienExterne,String contact,UUID territoireId,StatutOpportunite statut,LocalDateTime publieeLe,LocalDate dateExpiration,UUID creeParUtilisateurId) {}
