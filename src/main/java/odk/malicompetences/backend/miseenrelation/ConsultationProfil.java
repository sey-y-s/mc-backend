package odk.malicompetences.backend.miseenrelation;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.utilisateur.Utilisateur;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "consultation_profil")
public class ConsultationProfil {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @Column(name = "date_consultation", nullable = false)
    private LocalDateTime dateConsultation;
}