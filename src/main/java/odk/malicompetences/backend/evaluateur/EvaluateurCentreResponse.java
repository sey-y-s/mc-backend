package odk.malicompetences.backend.evaluateur;
import java.time.LocalDateTime;
import java.util.UUID;
public record EvaluateurCentreResponse(UUID id,UUID citoyenId,String nomCitoyen,String prenomCitoyen,UUID centreId,String nomCentre,String specialite,boolean actif,LocalDateTime dateDesignation){
 static EvaluateurCentreResponse from(EvaluateurCentre e){return new EvaluateurCentreResponse(e.getId(),e.getCitoyen().getId(),e.getCitoyen().getNom(),e.getCitoyen().getPrenom(),e.getCentre().getId(),e.getCentre().getNom(),e.getSpecialite(),e.isActif(),e.getDateDesignation());}
}
