package odk.malicompetences.backend.centreadmin;

import odk.malicompetences.backend.centre.Centre;
import java.util.UUID;
public record CentreAdminCentreResponse(UUID id, String nom, String description, String adresse, String telephone) {
 public static CentreAdminCentreResponse from(Centre c) { return new CentreAdminCentreResponse(c.getId(), c.getNom(), c.getDescription(), c.getAdresse(), c.getTelephone()); }
}
