package odk.malicompetences.backend.territoire;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "territoire")
@Getter
@Setter
@NoArgsConstructor
public class Territoire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Territoire parent;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private NiveauTerritoire niveau = NiveauTerritoire.REGION;

    @Column(nullable = false)
    private boolean actif = true;

}
