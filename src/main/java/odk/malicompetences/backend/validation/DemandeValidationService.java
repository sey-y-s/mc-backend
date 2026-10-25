package odk.malicompetences.backend.validation;

import odk.malicompetences.backend.centre.*;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.competence.*;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.common.regle.*;
import odk.malicompetences.backend.common.statut.*;
import odk.malicompetences.backend.evaluateur.*;
import odk.malicompetences.backend.notification.*;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @Transactional
public class DemandeValidationService {
    private final DemandeValidationRepository demandes;
    private final MethodeValidationRepository methodes;
    private final CompetenceMethodeValidationRepository methodesAutorisees;
    private final CitoyenCompetenceRepository competences;
    private final CitoyenRepository citoyens;
    private final CentreRepository centres;
    private final CentreCompetenceValidationRepository agrements;
    private final UtilisateurRepository utilisateurs;
    private final EvaluateurRepository evaluateurs;
    private final HabilitationEvaluateurRepository habilitations;
    private final DecisionValidationRepository decisions;
    private final EvaluationPratiqueRepository evaluationsPratiques;
    private final NotificationService notifications;

    @Value("${app.regles.validite-competence-mois:}")
    private String validiteCompetenceMoisConfiguree;

    private Integer validiteCompetenceMois() {
        if (validiteCompetenceMoisConfiguree == null || validiteCompetenceMoisConfiguree.isBlank()) return null;
        try {
            return Integer.valueOf(validiteCompetenceMoisConfiguree.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("La propriété app.regles.validite-competence-mois doit être un entier ou vide.", e);
        }
    }

    public DemandeValidationService(DemandeValidationRepository demandes, MethodeValidationRepository methodes, CompetenceMethodeValidationRepository methodesAutorisees,
            CitoyenCompetenceRepository competences, CitoyenRepository citoyens, CentreRepository centres,
            CentreCompetenceValidationRepository agrements, UtilisateurRepository utilisateurs, EvaluateurRepository evaluateurs,
            HabilitationEvaluateurRepository habilitations, DecisionValidationRepository decisions, EvaluationPratiqueRepository evaluationsPratiques, NotificationService notifications) {
        this.demandes=demandes; this.methodes=methodes; this.methodesAutorisees=methodesAutorisees; this.competences=competences; this.citoyens=citoyens;
        this.centres=centres; this.agrements=agrements; this.utilisateurs=utilisateurs; this.evaluateurs=evaluateurs;
        this.habilitations=habilitations; this.decisions=decisions; this.evaluationsPratiques=evaluationsPratiques; this.notifications=notifications;
    }

    public DemandeValidationResponse creer(UUID citoyenCompetenceId, DemandeValidationRequest r, String authId) {
        UUID userId = parse(authId);
        var citoyen = citoyens.findByUtilisateurId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        CitoyenCompetence cc = competences.findById(citoyenCompetenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Compétence du citoyen introuvable"));
        if (!cc.getCitoyen().getId().equals(citoyen.getId())) {
            throw new AccessDeniedException("Vous ne pouvez demander que la validation de vos propres compétences.");
        }

        LocalDate aujourdHui = LocalDate.now();
        if (cc.getStatutValidation() == StatutValidationCompetence.VALIDEE) {
            if (cc.getValideJusquAu() == null || !cc.getValideJusquAu().isBefore(aujourdHui)) {
                throw new IllegalStateException("Cette compétence est déjà validée et sa validation n'a pas expiré.");
            }
            Transitions.COMPETENCE_VALIDATION.verifier(cc.getStatutValidation(), StatutValidationCompetence.A_REVALIDER);
            cc.setStatutValidation(StatutValidationCompetence.A_REVALIDER);
        }

        MethodeValidation methode = methodes.findByCodeAndActiveTrue(r.methodeCode().name())
                .orElseThrow(() -> new ResourceNotFoundException("Méthode de validation introuvable ou inactive"));
        if (!methodesAutorisees.estAutorisee(cc.getCompetence().getId(), methode.getId())) {
            throw new IllegalArgumentException("Cette méthode n’est pas autorisée pour cette compétence.");
        }

        Centre centre = null;
        if (r.methodeCode() == MethodeValidationCode.EVALUATION_CENTRE) {
            if (r.centreId() == null) throw new IllegalArgumentException("Un centre est requis pour cette méthode.");
            centre = centres.findById(r.centreId()).orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));
            verifierCentre(centre, cc.getCompetence().getId(), aujourdHui);
        } else if (r.centreId() != null) {
            throw new IllegalArgumentException("Cette méthode ne nécessite pas de centre.");
        }

        if (demandes.existeDemandeActive(cc.getId(), methode.getCode(), List.of(
                StatutDemandeValidation.EN_ATTENTE, StatutDemandeValidation.EN_COURS,
                StatutDemandeValidation.COMPLEMENT_REQUIS))) {
            throw new IllegalStateException("Une demande active existe déjà pour cette compétence et cette méthode.");
        }

        DemandeValidation d = new DemandeValidation();
        d.setCitoyenCompetence(cc);
        d.setMethode(methode);
        d.setCentre(centre);
        d.setMessageCitoyen(r.messageCitoyen());

        if (r.methodeCode() == MethodeValidationCode.MAITRE_ARTISAN) {
            if (r.evaluateurId() == null) throw new IllegalArgumentException("Un évaluateur habilité est requis pour la méthode MAITRE_ARTISAN.");
            Evaluateur ev = evaluateurs.findById(r.evaluateurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Évaluateur introuvable"));
            verifierHabilitation(ev, d);
            d.setEvaluateurAssigneId(ev.getId());
        } else if (r.evaluateurId() != null) {
            throw new IllegalArgumentException("evaluateurId n'est accepté que pour la méthode MAITRE_ARTISAN.");
        }

        if (cc.getStatutValidation() != StatutValidationCompetence.EN_COURS) {
            Transitions.COMPETENCE_VALIDATION.verifier(cc.getStatutValidation(), StatutValidationCompetence.EN_COURS);
            cc.setStatutValidation(StatutValidationCompetence.EN_COURS);
        }
        DemandeValidation saved = demandes.save(d);
        notifier(saved, "Demande de validation créée", "Votre demande de validation a été enregistrée.", userId, "DEMANDE_CREEE");
        return DemandeValidationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DemandeValidationResponse> demandesCitoyen(UUID citoyenId, String authId,
            StatutDemandeValidation statut, MethodeValidationCode methodeCode) {
        UUID userId = parse(authId);
        var citoyen = citoyens.findById(citoyenId).orElseThrow(() -> new ResourceNotFoundException("Profil citoyen introuvable"));
        if (!citoyen.getUtilisateur().getId().equals(userId)) throw new AccessDeniedException("Vous ne pouvez consulter que vos demandes.");
        return demandes.findByCitoyenCompetenceCitoyenIdOrderByCreeeLeDesc(citoyen.getId()).stream()
                .filter(d -> statut == null || d.getStatut() == statut)
                .filter(d -> methodeCode == null || methodeCode.name().equals(d.getMethode().getCode()))
                .map(DemandeValidationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DemandeValidationResponse> demandesCitoyenCompetence(UUID ccId, String authId) {
        UUID userId = parse(authId);
        var citoyen = citoyens.findByUtilisateurId(userId).orElseThrow(() -> new AccessDeniedException("Profil citoyen requis."));
        CitoyenCompetence cc = competences.findById(ccId).orElseThrow(() -> new ResourceNotFoundException("Compétence du citoyen introuvable"));
        if (!cc.getCitoyen().getId().equals(citoyen.getId())) throw new AccessDeniedException("Cette compétence ne vous appartient pas.");
        return demandes.findByCitoyenCompetence_IdOrderByCreeeLeDesc(ccId).stream().map(DemandeValidationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DemandeValidationDetailResponse detailCitoyen(UUID ccId, UUID demandeId, String authId) {
        UUID userId = parse(authId);
        var citoyen = citoyens.findByUtilisateurId(userId).orElseThrow(() -> new AccessDeniedException("Profil citoyen requis."));
        DemandeValidation d = demandes.findByIdWithRelations(demandeId).orElseThrow(() -> new ResourceNotFoundException("Demande de validation introuvable"));
        if (!d.getCitoyenCompetence().getId().equals(ccId)
                || !d.getCitoyenCompetence().getCitoyen().getId().equals(citoyen.getId())) {
            throw new AccessDeniedException("Cette demande ne vous appartient pas.");
        }
        return detailResponse(d);
    }

    public DemandeValidationResponse annulerParCitoyen(UUID ccId, UUID demandeId, String authId) {
        UUID userId = parse(authId);
        var citoyen = citoyens.findByUtilisateurId(userId).orElseThrow(() -> new AccessDeniedException("Profil citoyen requis."));
        DemandeValidation d = demandes.findByIdForUpdate(demandeId).orElseThrow(() -> new ResourceNotFoundException("Demande de validation introuvable"));
        if (!d.getCitoyenCompetence().getId().equals(ccId)
                || !d.getCitoyenCompetence().getCitoyen().getId().equals(citoyen.getId())) {
            throw new AccessDeniedException("Cette demande ne vous appartient pas.");
        }
        if (d.getStatut() != StatutDemandeValidation.EN_ATTENTE && d.getStatut() != StatutDemandeValidation.COMPLEMENT_REQUIS) {
            throw new IllegalStateException("Seules les demandes EN_ATTENTE ou COMPLEMENT_REQUIS peuvent être annulées.");
        }
        var cc = d.getCitoyenCompetence();
        LocalDateTime maintenant = LocalDateTime.now();
        EtatValidationCompetence actuel = new EtatValidationCompetence(cc.getStatutValidation(), cc.getNiveauValide(), null, cc.getValideeLe(), cc.getValideJusquAu());
        EtatValidationCompetence etat = EffetDecisionValidation.appliquer(actuel, DecisionValidation.ANNULEE, null, code(d.getMethode()), maintenant, null);
        Transitions.DEMANDE_VALIDATION.verifier(d.getStatut(), StatutDemandeValidation.ANNULEE);
        Utilisateur decideur = utilisateurs.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Compte citoyen introuvable"));
        DecisionValidationEntity historique = new DecisionValidationEntity();
        historique.setDemande(d); historique.setDecideur(decideur); historique.setDecision(DecisionValidation.ANNULEE);
        decisions.save(historique);
        cc.setStatutValidation(etat.statut()); cc.setNiveauValide(etat.niveauValide());
        cc.setValideeLe(etat.valideeLe()); cc.setValideJusquAu(etat.valideJusquAu());
        d.setStatut(StatutDemandeValidation.ANNULEE); d.setClotureeLe(maintenant);
        DemandeValidation saved = demandes.save(d);
        notifier(saved, "Demande de validation annulée", "Votre demande de validation a été annulée.", userId, "ANNULEE");
        return DemandeValidationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DemandeValidationResponse> demandesPersonnel(StatutDemandeValidation statut, MethodeValidationCode methodeCode, UUID competenceId, String authId) {
        UUID userId = parse(authId);
        Utilisateur user = utilisateurs.findById(userId).orElseThrow(() -> new AccessDeniedException("Compte introuvable."));
        boolean admin = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getCode()) || "SUPER_ADMIN".equals(r.getCode()));
        UUID evaluateurId = null;
        if (!admin) {
            var citoyen = citoyens.findByUtilisateurId(userId).orElseThrow(() -> new AccessDeniedException("Accès réservé au personnel de validation."));
            Evaluateur ev = evaluateurs.findByCitoyenId(citoyen.getId()).filter(e -> e.getStatut() == StatutEvaluateur.ACTIF)
                    .orElseThrow(() -> new AccessDeniedException("Votre compte n'est pas un évaluateur actif."));
            evaluateurId = ev.getId();
        }
        return demandes.rechercher(statut, methodeCode == null ? null : methodeCode.name(), competenceId, evaluateurId)
                .stream().map(DemandeValidationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DemandeValidationDetailResponse detailPersonnel(UUID id, String authId) {
        UUID userId = parse(authId);
        DemandeValidation d = demandes.findByIdWithRelations(id).orElseThrow(() -> new ResourceNotFoundException("Demande de validation introuvable"));
        if (!estAdmin(userId) && !estDemandeAssignee(d, userId)) throw new AccessDeniedException("Vous ne pouvez consulter que les demandes qui vous sont assignées.");
        return detailResponse(d);
    }

    private DemandeValidationDetailResponse detailResponse(DemandeValidation d) {
        return new DemandeValidationDetailResponse(d.getId(), d.getCitoyenCompetence().getId(), d.getMethode().getCode(),
                d.getCentre() == null ? null : d.getCentre().getId(), d.getStatut(), d.getMessageCitoyen(),
                d.getCreeeLe(), d.getClotureeLe(), decisions.findByDemandeIdOrderByDecideeLeAsc(d.getId()).stream()
                .map(DecisionValidationResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public List<EvaluationPratiqueResponse> evaluations(UUID id, String authId) {
        UUID userId = parse(authId);
        DemandeValidation d = demandes.findByIdWithRelations(id).orElseThrow(() -> new ResourceNotFoundException("Demande de validation introuvable"));
        if (!estAdmin(userId) && !estDemandeAssignee(d, userId)) throw new AccessDeniedException("Vous ne pouvez consulter que les évaluations des demandes qui vous sont assignées.");
        return evaluationsPratiques.findByDemandeIdOrderByRealiseeLeDesc(id).stream().map(EvaluationPratiqueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EvaluateurProfilResponse profilEvaluateur(String authId) {
        UUID userId = parse(authId);
        var citoyen = citoyens.findByUtilisateurId(userId).orElseThrow(() -> new AccessDeniedException("Un profil évaluateur est requis."));
        Evaluateur ev = evaluateurs.findByCitoyenId(citoyen.getId()).orElseThrow(() -> new AccessDeniedException("Votre compte n'est pas enregistré comme évaluateur."));
        return new EvaluateurProfilResponse(ev.getId(), citoyen.getId(), ev.getStatut());
    }

    private boolean estAdmin(UUID userId) {
        return utilisateurs.findById(userId).map(u -> u.getRoles() != null && u.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getCode()) || "SUPER_ADMIN".equals(r.getCode()))).orElse(false);
    }

    private boolean estDemandeAssignee(DemandeValidation d, UUID userId) {
        var citoyen = citoyens.findByUtilisateurId(userId).orElse(null);
        return citoyen != null && evaluateurs.findByCitoyenId(citoyen.getId())
                .map(ev -> Objects.equals(d.getEvaluateurAssigneId(), ev.getId())).orElse(false);
    }

    public DemandeValidationResponse assigner(UUID demandeId, UUID evaluateurId) {
        DemandeValidation d=demandes.findByIdForUpdate(demandeId).orElseThrow(()->new ResourceNotFoundException("Demande de validation introuvable"));
        if (d.getStatut()!=StatutDemandeValidation.EN_ATTENTE && d.getStatut()!=StatutDemandeValidation.EN_COURS) throw new IllegalStateException("La demande est déjà clôturée.");
        Evaluateur ev=evaluateurs.findById(evaluateurId).orElseThrow(()->new ResourceNotFoundException("Évaluateur introuvable"));
        verifierHabilitation(ev,d);
        d.setEvaluateurAssigneId(ev.getId());
        if (d.getStatut()==StatutDemandeValidation.EN_ATTENTE) { Transitions.DEMANDE_VALIDATION.verifier(d.getStatut(),StatutDemandeValidation.EN_COURS); d.setStatut(StatutDemandeValidation.EN_COURS); }
        DemandeValidation saved=demandes.save(d);
        UUID destinataire=ev.getCitoyen().getUtilisateur().getId();
        notifier(saved,"Nouvelle demande assignée","Une demande de validation vous a été assignée.",destinataire,"ASSIGNEE");
        return DemandeValidationResponse.from(saved);
    }

    @Transactional(readOnly=true) public List<DemandeValidationResponse> demandesAssignees(String authId, StatutDemandeValidation statut) {
        UUID userId=parse(authId); var citoyen=citoyens.findByUtilisateurId(userId).orElseThrow(()->new AccessDeniedException("Un profil évaluateur est requis."));
        Evaluateur ev=evaluateurs.findByCitoyenId(citoyen.getId()).orElseThrow(()->new AccessDeniedException("Votre compte n'est pas enregistré comme évaluateur."));
        return demandes.findAssignees(ev.getId()).stream()
                .filter(d -> statut == null || d.getStatut() == statut)
                .map(DemandeValidationResponse::from).toList();
    }

    @Transactional(readOnly=true) public List<HabilitationEvaluateurResponse> mesHabilitations(String authId) {
        UUID userId=parse(authId); var citoyen=citoyens.findByUtilisateurId(userId).orElseThrow(()->new AccessDeniedException("Un profil évaluateur est requis."));
        Evaluateur ev=evaluateurs.findByCitoyenId(citoyen.getId()).orElseThrow(()->new AccessDeniedException("Votre compte n'est pas enregistré comme évaluateur."));
        return habilitations.findByEvaluateur_Citoyen_Id(citoyen.getId()).stream().map(HabilitationEvaluateurResponse::from).toList();
    }

    @Transactional(readOnly=true) public List<DecisionValidationResponse> historique(UUID id, String authId) {
        UUID userId=parse(authId);
        DemandeValidation d=demandes.findById(id).orElseThrow(()->new ResourceNotFoundException("Demande de validation introuvable"));
        boolean autorise=d.getCitoyenCompetence().getCitoyen().getUtilisateur().getId().equals(userId);
        if (!autorise) {
            var citoyen=citoyens.findByUtilisateurId(userId).orElse(null);
            if (citoyen!=null) autorise=evaluateurs.findByCitoyenId(citoyen.getId()).map(e->Objects.equals(d.getEvaluateurAssigneId(),e.getId())).orElse(false);
        }
        if (!autorise) {
            Utilisateur user=utilisateurs.findById(userId).orElseThrow(()->new AccessDeniedException("Accès refusé."));
            autorise=user.getRoles()!=null && user.getRoles().stream().anyMatch(role->"ADMIN".equals(role.getCode()) || "SUPER_ADMIN".equals(role.getCode()));
        }
        if (!autorise) throw new AccessDeniedException("Vous ne pouvez pas consulter l'historique de cette demande.");
        return decisions.findByDemandeIdOrderByDecideeLeAsc(id).stream().map(DecisionValidationResponse::from).toList();
    }

    public DemandeValidationResponse envoyerComplement(UUID id, String message, String authId) {
        UUID userId=parse(authId);
        var citoyen=citoyens.findByUtilisateurId(userId).orElseThrow(()->new AccessDeniedException("Profil citoyen requis."));
        DemandeValidation d=demandes.findByIdForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Demande de validation introuvable"));
        if (!d.getCitoyenCompetence().getCitoyen().getId().equals(citoyen.getId())) throw new AccessDeniedException("Cette demande ne vous appartient pas.");
        if (d.getStatut()!=StatutDemandeValidation.COMPLEMENT_REQUIS) throw new IllegalStateException("Cette demande n'attend pas de complément.");
        Transitions.DEMANDE_VALIDATION.verifier(d.getStatut(),StatutDemandeValidation.EN_COURS);
        d.setMessageCitoyen(message); d.setStatut(StatutDemandeValidation.EN_COURS);
        DemandeValidation saved=demandes.save(d);
        if (d.getEvaluateurAssigneId()!=null) {
            Evaluateur ev=evaluateurs.findById(d.getEvaluateurAssigneId()).orElse(null);
            if (ev!=null && ev.getCitoyen().getUtilisateur()!=null) notifier(saved,"Complément reçu","Le citoyen a transmis les informations complémentaires demandées.",ev.getCitoyen().getUtilisateur().getId(),"COMPLEMENT_RECU");
        }
        return DemandeValidationResponse.from(saved);
    }

    public DemandeValidationResponse decider(UUID id, DecisionValidationRequest r, String authId) {
        UUID userId=parse(authId); DemandeValidation d=demandes.findByIdForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Demande de validation introuvable"));
        if (d.getStatut()!=StatutDemandeValidation.EN_ATTENTE && d.getStatut()!=StatutDemandeValidation.EN_COURS) throw new IllegalStateException("Cette demande n’est pas prête pour une décision. Le citoyen doit d’abord transmettre le complément demandé.");
        var citoyen=citoyens.findByUtilisateurId(userId).orElseThrow(()->new AccessDeniedException("Un profil évaluateur est requis."));
        Evaluateur ev=evaluateurs.findByCitoyenId(citoyen.getId()).filter(e->e.getStatut()==StatutEvaluateur.ACTIF).orElseThrow(()->new AccessDeniedException("Votre compte n'est pas un évaluateur actif."));
        if (!Objects.equals(d.getEvaluateurAssigneId(),ev.getId())) throw new AccessDeniedException("Cette demande ne vous est pas assignée.");
        verifierHabilitation(ev,d);
        if (r.decision()==DecisionValidation.VALIDEE && r.niveauValide()==null) throw new IllegalArgumentException("Le niveau validé est obligatoire.");
        if ((r.decision()==DecisionValidation.REJETEE || r.decision()==DecisionValidation.COMPLEMENT_REQUIS) && (r.motif()==null || r.motif().isBlank())) throw new IllegalArgumentException("Un motif est obligatoire pour cette décision.");
        if (r.decision()==DecisionValidation.ANNULEE) throw new IllegalArgumentException("L'annulation se fait par le citoyen ou un administrateur, pas par une décision d'évaluation.");
        Utilisateur decideur=utilisateurs.findById(userId).orElseThrow(()->new ResourceNotFoundException("Compte évaluateur introuvable"));
        MethodeValidationCode methode=code(d.getMethode());
        CitoyenCompetence cc=d.getCitoyenCompetence();
        EtatValidationCompetence actuel=new EtatValidationCompetence(cc.getStatutValidation(),cc.getNiveauValide(),null,cc.getValideeLe(),cc.getValideJusquAu());
        LocalDateTime maintenant=LocalDateTime.now();
        EtatValidationCompetence etat=EffetDecisionValidation.appliquer(actuel,r.decision(),r.niveauValide(),methode,maintenant,validiteCompetenceMois());
        Transitions.DEMANDE_VALIDATION.verifier(d.getStatut(),EffetDecisionValidation.statutDemandeApres(r.decision()));
        DecisionValidationEntity historique=new DecisionValidationEntity(); historique.setDemande(d); historique.setDecideur(decideur); historique.setDecision(r.decision()); historique.setNiveauValide(r.niveauValide()); historique.setMotif(r.motif()); decisions.save(historique);
        if (methode==MethodeValidationCode.EVALUATION_CENTRE || methode==MethodeValidationCode.MAITRE_ARTISAN) {
            EvaluationPratique evaluation=new EvaluationPratique(); evaluation.setDemande(d); evaluation.setEvaluateur(ev); evaluation.setCentre(d.getCentre());
            evaluation.setScore(r.score()); evaluation.setObservations(r.observations()); evaluation.setGrilleJson(r.grille()); evaluationsPratiques.save(evaluation);
        }
        cc.setStatutValidation(etat.statut()); cc.setNiveauValide(etat.niveauValide()); cc.setValideeLe(etat.valideeLe()); cc.setValideJusquAu(etat.valideJusquAu());
        if (r.decision()==DecisionValidation.VALIDEE) cc.setDerniereMethodeId(d.getMethode().getId());
        d.setStatut(EffetDecisionValidation.statutDemandeApres(r.decision()));
        if (d.getStatut()==StatutDemandeValidation.VALIDEE || d.getStatut()==StatutDemandeValidation.REJETEE || d.getStatut()==StatutDemandeValidation.ANNULEE) d.setClotureeLe(maintenant);
        DemandeValidation saved=demandes.save(d);
        UUID proprietaire=cc.getCitoyen().getUtilisateur().getId();
        notifier(saved,"Décision de validation",message(r.decision()),proprietaire,"DECISION_"+r.decision().name());
        return DemandeValidationResponse.from(saved);
    }

    private void verifierHabilitation(Evaluateur ev, DemandeValidation d) {
        UUID competenceId=d.getCitoyenCompetence().getCompetence().getId(); MethodeValidationCode methode=code(d.getMethode()); LocalDate aujourdHui=LocalDate.now();
        var demandeInfo=new PolitiqueHabilitation.DemandeInfo(d.getCitoyenCompetence().getCitoyen().getId(),competenceId,methode,d.getCentre()==null?null:d.getCentre().getId());
        boolean autorise=false;
        for (HabilitationEvaluateur h: habilitations.trouverPourEvaluation(ev.getId(),competenceId,methode.name())) {
            var info=new PolitiqueHabilitation.HabilitationInfo(ev.getCitoyen().getId(),ev.getStatut(),h.getStatut(),h.getCompetence().getId(),code(h.getMethode()),h.getCentre()==null?null:h.getCentre().getId(),h.getDateDebut(),h.getDateFin());
            if (PolitiqueHabilitation.verifier(info,demandeInfo,aujourdHui).isEmpty()) {
                if (d.getCentre()!=null) { if (h.getCentre()==null || !h.getCentre().getId().equals(d.getCentre().getId())) continue; verifierCentre(d.getCentre(),competenceId,aujourdHui); }
                autorise=true; break;
            }
        }
        if (!autorise) throw new AccessDeniedException("Aucune habilitation active ne couvre cette compétence, cette méthode et ce centre.");
    }

    private void verifierCentre(Centre centre, UUID competenceId, LocalDate date) {
        boolean agree=agrements.findByCentre_IdAndCompetence_IdAndActiveTrue(centre.getId(),competenceId).stream().anyMatch(a->a.couvre(date));
        var refus=PolitiqueHabilitation.verifierCentre(centre.getStatut(),agree);
        if (refus.isPresent()) throw new IllegalArgumentException(refus.get()==PolitiqueHabilitation.Refus.CENTRE_INACTIF?"Le centre n'est pas actif.":"Le centre ne dispose pas d'un agrément actif pour cette compétence à la date demandée.");
    }

    private void notifier(DemandeValidation d,String titre,String contenu,UUID destinataire,String suffixe) {
        notifications.creerEtEnvoyerNotification(new NotificationRequest(titre,contenu,TypeNotification.VALIDATION,List.of(destinataire),"DEMANDE_VALIDATION",d.getId(),"VALIDATION:"+d.getId()+":"+suffixe));
    }
    private String message(DecisionValidation d) { return switch(d) { case VALIDEE -> "Votre compétence a été validée."; case REJETEE -> "Votre demande de validation a été rejetée."; case COMPLEMENT_REQUIS -> "Des informations complémentaires sont nécessaires."; case ANNULEE -> "Votre demande a été annulée."; }; }
    private MethodeValidationCode code(MethodeValidation methode) { try { return MethodeValidationCode.valueOf(methode.getCode()); } catch(Exception e) { throw new IllegalStateException("Code de méthode de validation inconnu : "+methode.getCode()); } }
    private UUID parse(String raw) { try { return UUID.fromString(raw); } catch(Exception e) { throw new AccessDeniedException("Identifiant de compte invalide."); } }
}
