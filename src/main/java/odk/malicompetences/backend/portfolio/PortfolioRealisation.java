package odk.malicompetences.backend.portfolio;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import odk.malicompetences.backend.citoyen.Citoyen;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "portfolio_realisation")

@Getter
@Setter
@NoArgsConstructor
public class PortfolioRealisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_realisation")
    private LocalDate dateRealisation;

    @Column(name = "lien_url", length = 500)
    private String lienUrl;

    @OneToMany(
            mappedBy = "portfolioRealisation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MediaRealisation> medias = new ArrayList<>();
}