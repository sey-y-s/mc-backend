package odk.malicompetences.backend.validation;

import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.util.UUID;
@Entity @Table(name="methode_validation") @Getter @Setter @NoArgsConstructor
public class MethodeValidation {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false, unique=true, length=40) private String code;
 @Column(nullable=false, length=120) private String libelle;
 @Column(columnDefinition="TEXT") private String description;
 @Column(nullable=false) private boolean active=true;
}
