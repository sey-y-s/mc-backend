package odk.malicompetences.backend.parcours;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.centre.HabilitationService;
import odk.malicompetences.backend.centre.StatutCentre;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetence;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.Competence;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.competence.ParcoursCompetence;
import odk.malicompetences.backend.evaluateur.EvaluateurCentre;
import odk.malicompetences.backend.evaluateur.EvaluateurCentreRepository;
import odk.malicompetences.backend.parcours.ParcoursValidationResponse.CentreProche;
import odk.malicompetences.backend.parcours.ParcoursValidationResponse.ModaliteProposee;
import odk.malicompetences.backend.portfolio.PortfolioRealisationRepository;
import odk.malicompetences.backend.preuve.TypePreuve;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.testnumerique.TestNumerique;
import odk.malicompetences.backend.testnumerique.TestNumeriqueRepository;
import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.validation.ValidationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Moteur d'orientation : pour une compétence déclarée, choisit le parcours de validation le plus adapté
 * à partir des règles de la compétence, de la situation territoriale du citoyen et des moyens disponibles
 * (tests, centres agréés, évaluateurs habilités). Si une modalité n'est pas réalisable, il propose
 * automatiquement une alternative autorisée ; si aucune ne l'est, le dossier est « en attente d'évaluation ».
 */
@Service
@Transactional(readOnly = true)
public class ParcoursValidationService {

    private final CitoyenCompetenceRepository citoyenCompetences;
    private final ReglesModaliteService regles;
    private final CentreRepository centres;
    private final HabilitationService habilitation;
    private final EvaluateurCentreRepository evaluateurs;
    private final TestNumeriqueRepository tests;
    private final PortfolioRealisationRepository portfolios;
    private final ValidationRepository validations;

    public ParcoursValidationService(CitoyenCompetenceRepository citoyenCompetences,
                                     ReglesModaliteService regles,
                                     CentreRepository centres,
                                     HabilitationService habilitation,
                                     EvaluateurCentreRepository evaluateurs,
                                     TestNumeriqueRepository tests,
                                     PortfolioRealisationRepository portfolios,
                                     ValidationRepository validations) {
        this.citoyenCompetences = citoyenCompetences;
        this.regles = regles;
        this.centres = centres;
        this.habilitation = habilitation;
        this.evaluateurs = evaluateurs;
        this.tests = tests;
        this.portfolios = portfolios;
        this.validations = validations;
    }

    /** Centre pouvant évaluer la compétence, avec sa position par rapport au citoyen. */
    private record CentreCandidat(Centre centre, Proximite proximite, boolean artisan) {
    }

    public ParcoursValidationResponse proposer(UUID citoyenCompetenceId, UUID citoyenId) {
        CitoyenCompetence cc = citoyenCompetences.findById(citoyenCompetenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence introuvable"));
        if (!cc.getCitoyen().getId().equals(citoyenId)) {
            throw new AccessDeniedException("Vous ne pouvez consulter que le parcours de vos propres compétences.");
        }

        Competence competence = cc.getCompetence();
        ParcoursCompetence parcours = parcoursEffectif(cc);
        StatutParcours statut = statut(cc);
        List<RegleModalite> reglesActives = regles.regles(competence, parcours).stream()
                .filter(RegleModalite::actif)
                .sorted(Comparator.comparingInt(RegleModalite::ordre))
                .toList();

        List<CentreCandidat> candidats = centresCandidats(cc, citoyenId);
        boolean portfolioNonVide = !portfolios.findByCitoyenId(citoyenId).isEmpty();

        List<ModaliteProposee> propositions = new ArrayList<>();
        for (RegleModalite regle : reglesActives) {
            propositions.add(evaluer(regle, parcours, competence, candidats, portfolioNonVide));
        }

        // Modalité recommandée : la première disponible dans l'ordre de préférence, sauf si tout est déjà réglé.
        boolean aTraiter = statut != StatutParcours.VALIDEE && statut != StatutParcours.EN_COURS_EVALUATION;
        ModaliteProposee choisie = aTraiter
                ? propositions.stream().filter(ModaliteProposee::disponible).findFirst().orElse(null)
                : null;
        boolean enAttente = aTraiter && choisie == null;
        if (choisie != null) {
            propositions = marquerRecommandee(propositions, choisie.modalite());
        }

        String message = message(statut, enAttente, choisie, propositions);
        return new ParcoursValidationResponse(cc.getId(), competence.getId(), competence.getNom(), parcours,
                statut, statut.libelle(), enAttente, choisie == null ? null : choisie.modalite(), message, propositions);
    }

    /** Vrai si la modalité est autorisée par les règles de cette compétence. */
    public boolean modaliteAutorisee(CitoyenCompetence cc, ModaliteValidation modalite) {
        return regles.regles(cc.getCompetence(), parcoursEffectif(cc)).stream()
                .anyMatch(regle -> regle.actif() && regle.modalite() == modalite);
    }

    /**
     * Le parcours est imposé au citoyen : s'il existe une modalité recommandée, c'est la seule qu'il peut
     * emprunter. Les autres ne servent qu'en repli, quand la première n'est pas réalisable.
     */
    public void verifierModaliteImposee(CitoyenCompetence cc, ModaliteValidation demandee) {
        ModaliteValidation imposee = proposer(cc.getId(), cc.getCitoyen().getId()).recommandee();
        if (imposee != null && imposee != demandee) {
            throw new IllegalStateException("Pour cette compétence, votre parcours est : " + imposee.libelle()
                    + ". Suivez la démarche proposée dans votre espace de validation.");
        }
    }

    /** Conditions préalables d'une demande d'évaluation (ex. un portfolio non vide). */
    public void verifierPrerequis(CitoyenCompetence cc, ModaliteValidation modalite) {
        if (modalite == ModaliteValidation.PORTFOLIO_MEDIA
                && portfolios.findByCitoyenId(cc.getCitoyen().getId()).isEmpty()) {
            throw new IllegalStateException("Ajoutez d'abord vos réalisations (photos, vidéos, projets) à votre portfolio.");
        }
    }

    // ---------------------------------------------------------------- évaluation de chaque modalité

    private ModaliteProposee evaluer(RegleModalite regle, ParcoursCompetence parcours, Competence competence,
                                     List<CentreCandidat> candidats, boolean portfolioNonVide) {
        ModaliteValidation modalite = regle.modalite();
        return switch (modalite) {
            case DOCUMENTAIRE -> {
                List<TypePreuve> pieces = regle.piecesAdmissibles().isEmpty()
                        ? ParcoursCompetence.preuvesDeposables(parcours) : regle.piecesAdmissibles();
                yield proposition(regle, true, "Déposez un justificatif de votre formation ou de votre expérience : "
                        + "il sera vérifié.", "DEPOSER_PREUVE", null, pieces, List.of());
            }
            case TEST_NUMERIQUE -> {
                List<TestNumerique> disponibles = tests.findByCompetenceIdAndActifTrue(competence.getId());
                if (disponibles.isEmpty()) {
                    yield proposition(regle, false, "Aucun test numérique n'existe encore pour cette compétence.",
                            null, null, List.of(), List.of());
                }
                yield proposition(regle, true, "Passez le test en ligne : réussite à partir de "
                        + seuil(regle) + " %.", "PASSER_TEST", disponibles.get(0).getId(), List.of(), List.of());
            }
            case PRATIQUE_CENTRE -> {
                List<CentreCandidat> proches = candidats.stream()
                        .filter(c -> c.proximite() != Proximite.AUTRE_REGION).toList();
                if (proches.isEmpty()) {
                    yield proposition(regle, false, candidats.isEmpty()
                            ? "Aucun centre agréé pour cette compétence pour le moment."
                            : "Aucun centre agréé dans votre commune ou votre région.",
                            null, null, List.of(), List.of());
                }
                yield proposition(regle, true, "Passez l'examen pratique dans un centre agréé près de chez vous.",
                        "DEMANDER_EVALUATION", null, List.of(), versCentres(proches));
            }
            case PORTFOLIO_MEDIA -> {
                if (candidats.isEmpty()) {
                    yield proposition(regle, false, "Aucun jury habilité n'est disponible pour le moment.",
                            null, null, List.of(), List.of());
                }
                // Évaluation à distance : la région du jury importe peu.
                yield portfolioNonVide
                        ? proposition(regle, true, "Votre portfolio sera analysé à distance par un jury habilité.",
                                "DEMANDER_EVALUATION", null, List.of(), versCentres(candidats))
                        : proposition(regle, true, "Ajoutez d'abord vos réalisations (photos, vidéos datées, projets) "
                                + "à votre portfolio.", "AJOUTER_REALISATIONS", null, List.of(), versCentres(candidats));
            }
            case MAITRE_ARTISAN -> {
                List<CentreCandidat> artisans = candidats.stream()
                        .filter(c -> c.artisan() && c.proximite() != Proximite.AUTRE_REGION).toList();
                if (artisans.isEmpty()) {
                    yield proposition(regle, false,
                            "Aucun maître artisan référent habilité dans votre commune ou votre région.",
                            null, null, List.of(), List.of());
                }
                yield proposition(regle, true, "Un maître artisan référent observe votre savoir-faire sur le terrain.",
                        "DEMANDER_EVALUATION", null, List.of(), versCentres(artisans));
            }
        };
    }

    private ModaliteProposee proposition(RegleModalite regle, boolean disponible, String raison, String action,
                                         UUID testId, List<TypePreuve> pieces, List<CentreProche> centresProches) {
        ModaliteValidation modalite = regle.modalite();
        Integer seuil = modalite == ModaliteValidation.TEST_NUMERIQUE ? seuil(regle) : null;
        return new ModaliteProposee(modalite, modalite.libelle(), modalite.description(), disponible, false,
                raison, disponible ? action : null, testId, seuil, pieces, regle.criteres(),
                regle.profilsHabilites(), centresProches);
    }

    private static int seuil(RegleModalite regle) {
        return regle.seuilReussite() != null ? regle.seuilReussite() : ReglesModaliteService.SEUIL_TEST_PAR_DEFAUT;
    }

    private static List<ModaliteProposee> marquerRecommandee(List<ModaliteProposee> liste, ModaliteValidation recommandee) {
        return liste.stream().map(p -> new ModaliteProposee(p.modalite(), p.libelle(), p.description(), p.disponible(),
                p.modalite() == recommandee, p.raison(), p.action(), p.testId(), p.seuilReussite(),
                p.piecesAdmissibles(), p.criteres(), p.profilsHabilites(), p.centres())).toList();
    }

    private static String message(StatutParcours statut, boolean enAttente, ModaliteProposee choisie,
                                  List<ModaliteProposee> propositions) {
        if (statut == StatutParcours.VALIDEE) {
            return "Cette compétence est validée.";
        }
        if (statut == StatutParcours.EN_COURS_EVALUATION) {
            return "Une évaluation est déjà en cours pour cette compétence. Vous serez prévenu de la décision.";
        }
        if (enAttente) {
            return "Aucune modalité d'évaluation n'est réalisable près de chez vous pour le moment. "
                    + "Votre dossier est « en attente d'évaluation » : il n'est pas rejeté et sera traité "
                    + "dès qu'une évaluation sera possible.";
        }
        ModaliteProposee preferee = propositions.isEmpty() ? null : propositions.get(0);
        if (preferee != null && !preferee.disponible()) {
            return "« " + preferee.libelle() + " » n'est pas réalisable près de chez vous. "
                    + "Alternative proposée : « " + choisie.libelle() + " ».";
        }
        return "Parcours recommandé : « " + choisie.libelle() + " ».";
    }

    // ---------------------------------------------------------------- territoire et statut

    private ParcoursCompetence parcoursEffectif(CitoyenCompetence cc) {
        if (cc.getParcours() != null) {
            return cc.getParcours();
        }
        List<ParcoursCompetence> possibles = cc.getCompetence().getParcoursAutorises();
        return possibles.size() == 1 ? possibles.get(0) : null;
    }

    private StatutParcours statut(CitoyenCompetence cc) {
        if (cc.getEtat() == EtatCompetence.VALIDEE) {
            return StatutParcours.VALIDEE;
        }
        if (validations.existsByCitoyenCompetenceIdAndStatut(cc.getId(), StatutValidation.EN_ATTENTE)) {
            return StatutParcours.EN_COURS_EVALUATION;
        }
        if (cc.getEtat() == EtatCompetence.ATTESTEE) {
            return StatutParcours.PREUVE_RECUE;
        }
        return StatutParcours.DECLAREE;
    }

    /** Centres actifs, agréés pour la compétence et disposant d'un évaluateur habilité (autre que le citoyen). */
    private List<CentreCandidat> centresCandidats(CitoyenCompetence cc, UUID citoyenId) {
        Competence competence = cc.getCompetence();
        Commune communeCitoyen = cc.getCitoyen().getCommune();
        UUID regionCitoyen = communeCitoyen == null || communeCitoyen.getRegion() == null
                ? null : communeCitoyen.getRegion().getId();

        List<CentreCandidat> candidats = new ArrayList<>();
        for (Centre centre : centres.findAll()) {
            if (centre.getStatut() != StatutCentre.ACTIF || !habilitation.centreAgree(centre, competence)) {
                continue;
            }
            List<EvaluateurCentre> habilites = evaluateurs.findByCentreIdAndActifTrue(centre.getId()).stream()
                    .filter(e -> !e.getCitoyen().getId().equals(citoyenId))
                    .filter(e -> habilitation.evaluateurHabilite(e, competence))
                    .toList();
            if (habilites.isEmpty()) {
                continue;
            }
            boolean artisan = habilites.stream()
                    .anyMatch(e -> e.getSpecialite() != null && !e.getSpecialite().isBlank());
            candidats.add(new CentreCandidat(centre, proximite(centre, communeCitoyen, regionCitoyen), artisan));
        }
        candidats.sort(Comparator.comparing(CentreCandidat::proximite));
        return candidats;
    }

    /** Un centre sans commune renseignée reste proposé (« localisation à confirmer ») : on ne l'écarte pas à tort. */
    private static Proximite proximite(Centre centre, Commune communeCitoyen, UUID regionCitoyen) {
        Commune communeCentre = centre.getCommune();
        if (communeCitoyen == null || communeCentre == null) {
            return Proximite.INCONNUE;
        }
        if (communeCentre.getId().equals(communeCitoyen.getId())) {
            return Proximite.MEME_COMMUNE;
        }
        UUID regionCentre = communeCentre.getRegion() == null ? null : communeCentre.getRegion().getId();
        if (regionCitoyen == null || regionCentre == null) {
            return Proximite.INCONNUE;
        }
        return regionCitoyen.equals(regionCentre) ? Proximite.MEME_REGION : Proximite.AUTRE_REGION;
    }

    private static List<CentreProche> versCentres(List<CentreCandidat> candidats) {
        return candidats.stream()
                .map(c -> new CentreProche(c.centre().getId(), c.centre().getNom(), c.centre().getAdresse(),
                        c.proximite(), c.proximite().libelle()))
                .toList();
    }
}
