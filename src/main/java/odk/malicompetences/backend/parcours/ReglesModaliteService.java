package odk.malicompetences.backend.parcours;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.CompetenceRepository;
import odk.malicompetences.backend.competence.ParcoursCompetence;
import odk.malicompetences.backend.preuve.TypePreuve;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Règles de validation par compétence. Tant que l'administrateur n'a rien configuré pour une compétence,
 * des règles par défaut s'appliquent (selon son parcours formel / informel) : rien à saisir pour démarrer.
 */
@Service
@Transactional(readOnly = true)
public class ReglesModaliteService {

    /** Seuil de réussite du test numérique quand aucun seuil n'est configuré. */
    public static final int SEUIL_TEST_PAR_DEFAUT = 60;

    /** Pièces qu'un citoyen peut déposer lui-même (les autres sont produites par la plateforme). */
    private static final Set<TypePreuve> PIECES_DEPOSABLES = Set.of(
            TypePreuve.DIPLOME, TypePreuve.ATTESTATION, TypePreuve.ATTESTATION_STAGE, TypePreuve.CERTIFICAT_CENTRE,
            TypePreuve.CERTIFICAT, TypePreuve.ATTESTATION_EMPLOYEUR, TypePreuve.AUTRE);

    private final CompetenceModaliteRepository repository;
    private final CompetenceRepository competenceRepository;

    public ReglesModaliteService(CompetenceModaliteRepository repository, CompetenceRepository competenceRepository) {
        this.repository = repository;
        this.competenceRepository = competenceRepository;
    }

    /** Règles effectives (triées par ordre de préférence) ; les règles par défaut si rien n'est configuré. */
    public List<RegleModalite> regles(Competence competence, ParcoursCompetence parcours) {
        List<CompetenceModalite> configurees = repository.findByCompetenceIdOrderByOrdreAsc(competence.getId());
        if (!configurees.isEmpty()) {
            return configurees.stream().map(this::depuis).toList();
        }
        return parDefaut(parcours);
    }

    /** Vue de l'administrateur : règles configurées, ou règles par défaut (parDefaut = true). */
    public List<RegleModalite> reglesPourAdmin(UUID competenceId) {
        Competence competence = competence(competenceId);
        var possibles = competence.getParcoursAutorises();
        return regles(competence, possibles.size() == 1 ? possibles.get(0) : null);
    }

    @Transactional
    public List<RegleModalite> remplacer(UUID competenceId, List<RegleModaliteRequest> demandes) {
        Competence competence = competence(competenceId);
        if (demandes == null || demandes.isEmpty()) {
            return reinitialiser(competenceId);
        }
        Set<ModaliteValidation> vues = new HashSet<>();
        List<CompetenceModalite> nouvelles = new ArrayList<>();
        boolean uneActive = false;
        int index = 0;
        for (RegleModaliteRequest demande : demandes) {
            if (demande == null || demande.modalite() == null) {
                throw new IllegalArgumentException("La modalité est obligatoire.");
            }
            if (!vues.add(demande.modalite())) {
                throw new IllegalArgumentException("La modalité « " + demande.modalite().libelle() + " » est présente plusieurs fois.");
            }
            if (demande.seuilReussite() != null && (demande.seuilReussite() < 0 || demande.seuilReussite() > 100)) {
                throw new IllegalArgumentException("Le seuil de réussite doit être compris entre 0 et 100.");
            }
            if (demande.profilsHabilites() != null && demande.profilsHabilites().length() > 300) {
                throw new IllegalArgumentException("Les profils habilités ne doivent pas dépasser 300 caractères.");
            }
            if (demande.criteres() != null && demande.criteres().length() > 500) {
                throw new IllegalArgumentException("Les critères ne doivent pas dépasser 500 caractères.");
            }
            List<TypePreuve> pieces = demande.piecesAdmissibles() == null ? List.of() : demande.piecesAdmissibles();
            for (TypePreuve piece : pieces) {
                if (!PIECES_DEPOSABLES.contains(piece)) {
                    throw new IllegalArgumentException("Cette pièce ne peut pas être déposée par un citoyen : " + piece);
                }
            }
            boolean actif = demande.actif() == null || demande.actif();
            uneActive = uneActive || actif;

            CompetenceModalite regle = new CompetenceModalite();
            regle.setCompetence(competence);
            regle.setModalite(demande.modalite());
            regle.setOrdre(demande.ordre() != null ? demande.ordre() : index);
            regle.setActif(actif);
            regle.setSeuilReussite(demande.modalite() == ModaliteValidation.TEST_NUMERIQUE ? demande.seuilReussite() : null);
            regle.setPiecesAdmissibles(demande.modalite() == ModaliteValidation.DOCUMENTAIRE && !pieces.isEmpty()
                    ? pieces.stream().distinct().map(Enum::name).collect(Collectors.joining(","))
                    : null);
            regle.setProfilsHabilites(vide(demande.profilsHabilites()));
            regle.setCriteres(vide(demande.criteres()));
            nouvelles.add(regle);
            index++;
        }
        if (!uneActive) {
            throw new IllegalArgumentException("Au moins une modalité doit rester active pour cette compétence.");
        }
        repository.deleteByCompetenceId(competenceId);
        repository.flush();
        repository.saveAll(nouvelles);
        return reglesPourAdmin(competenceId);
    }

    /** Supprime la configuration : les règles par défaut s'appliquent de nouveau. */
    @Transactional
    public List<RegleModalite> reinitialiser(UUID competenceId) {
        competence(competenceId);
        repository.deleteByCompetenceId(competenceId);
        repository.flush();
        return reglesPourAdmin(competenceId);
    }

    /** Seuil de réussite du test numérique de cette compétence (en %). */
    public int seuilReussiteTest(UUID competenceId) {
        return repository.findByCompetenceIdOrderByOrdreAsc(competenceId).stream()
                .filter(r -> r.isActif() && r.getModalite() == ModaliteValidation.TEST_NUMERIQUE)
                .map(CompetenceModalite::getSeuilReussite)
                .filter(seuil -> seuil != null)
                .findFirst()
                .orElse(SEUIL_TEST_PAR_DEFAUT);
    }

    /** Pièces justificatives admissibles configurées pour la validation documentaire (vide : règle par défaut). */
    public Optional<List<TypePreuve>> piecesDocumentairesConfigurees(UUID competenceId) {
        return repository.findByCompetenceIdOrderByOrdreAsc(competenceId).stream()
                .filter(r -> r.isActif() && r.getModalite() == ModaliteValidation.DOCUMENTAIRE)
                .map(this::pieces)
                .filter(liste -> !liste.isEmpty())
                .findFirst();
    }

    private Competence competence(UUID competenceId) {
        return competenceRepository.findById(competenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence introuvable"));
    }

    private RegleModalite depuis(CompetenceModalite m) {
        return new RegleModalite(m.getModalite(), m.getModalite().libelle(), m.getOrdre(), m.isActif(),
                m.getSeuilReussite(), pieces(m), m.getProfilsHabilites(), m.getCriteres(), false);
    }

    private List<TypePreuve> pieces(CompetenceModalite m) {
        if (m.getPiecesAdmissibles() == null || m.getPiecesAdmissibles().isBlank()) {
            return List.of();
        }
        return Arrays.stream(m.getPiecesAdmissibles().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(valeur -> {
                    try {
                        return TypePreuve.valueOf(valeur);
                    } catch (IllegalArgumentException exception) {
                        return null;
                    }
                })
                .filter(type -> type != null)
                .toList();
    }

    private static String vide(String texte) {
        return texte == null || texte.isBlank() ? null : texte.trim();
    }

    /**
     * Règles par défaut. L'ordre n'est qu'une préférence de proposition : aucune modalité n'est supérieure
     * à une autre. Formel : on part des documents ; informel : on part de la pratique.
     */
    private List<RegleModalite> parDefaut(ParcoursCompetence parcours) {
        List<ModaliteValidation> ordre;
        if (parcours == ParcoursCompetence.FORMEL) {
            ordre = List.of(ModaliteValidation.DOCUMENTAIRE, ModaliteValidation.TEST_NUMERIQUE,
                    ModaliteValidation.PRATIQUE_CENTRE);
        } else if (parcours == ParcoursCompetence.INFORMEL) {
            ordre = List.of(ModaliteValidation.PRATIQUE_CENTRE, ModaliteValidation.MAITRE_ARTISAN,
                    ModaliteValidation.PORTFOLIO_MEDIA, ModaliteValidation.TEST_NUMERIQUE,
                    ModaliteValidation.DOCUMENTAIRE);
        } else {
            ordre = List.of(ModaliteValidation.DOCUMENTAIRE, ModaliteValidation.TEST_NUMERIQUE,
                    ModaliteValidation.PRATIQUE_CENTRE, ModaliteValidation.PORTFOLIO_MEDIA,
                    ModaliteValidation.MAITRE_ARTISAN);
        }
        List<RegleModalite> regles = new ArrayList<>();
        int index = 0;
        for (ModaliteValidation modalite : ordre) {
            List<TypePreuve> pieces = modalite == ModaliteValidation.DOCUMENTAIRE
                    ? ParcoursCompetence.preuvesDeposables(parcours) : List.of();
            Integer seuil = modalite == ModaliteValidation.TEST_NUMERIQUE ? SEUIL_TEST_PAR_DEFAUT : null;
            regles.add(new RegleModalite(modalite, modalite.libelle(), index++, true, seuil, pieces, null, null, true));
        }
        return regles;
    }
}
