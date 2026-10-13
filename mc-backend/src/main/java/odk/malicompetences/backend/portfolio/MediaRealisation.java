package odk.malicompetences.backend.portfolio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "media_realisation")

@Getter
@Setter
@NoArgsConstructor
public class MediaRealisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeMedia type;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "nom_fichier", length = 255)
    private String nomFichier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_realisation_id", nullable = false)
    private PortfolioRealisation portfolioRealisation;
}