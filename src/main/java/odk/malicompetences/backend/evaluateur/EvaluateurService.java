package odk.malicompetences.backend.evaluateur;

import odk.malicompetences.backend.centre.Centre;
import odk.malicompetences.backend.centre.CentreRepository;
import odk.malicompetences.backend.centre.CentreResponse;
import odk.malicompetences.backend.citoyen.Citoyen;
import odk.malicompetences.backend.citoyen.CitoyenRepository;
import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.competence.CitoyenCompetenceRepository;
import odk.malicompetences.backend.competence.EtatCompetence;
import odk.malicompetences.backend.parcours.ParcoursValidationService;
import odk.malicompetences.backend.utilisateur.Role;
import odk.malicompetences.backend.utilisateur.Utilisateur;
import odk.malicompetences.backend.utilisateur.UtilisateurRepository;
import odk.malicompetences.backend.validation.StatutValidation;
import odk.malicompetences.backend.validation.TypeValidation;
import odk.malicompetences.backend.validation.Validation;
import odk.malicompetences.backend.validation.ValidationRepository;
import odk.malicompetences.backend.validation.ValidationRequest;
import odk.malicompetences.backend.validation.ValidationResponse;
import odk.malicompetences.backend.validation.ValidationService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvaluateurService {

 private final EvaluateurCentreRepository assignments;
 private final CitoyenRepository citoyens;
 private final CentreRepository centres;
 private final UtilisateurRepository utilisateurs;
 private final ValidationRepository validations;
 private final ValidationService validationService;
 private final CitoyenCompetenceRepository citoyenCompetences;
 private final ParcoursValidationService parcoursValidationService;

 private odk.malicompetences.backend.centre.HabilitationService habilitation;
 private EvaluateurHabilitationRepository habilitations;
 private odk.malicompetences.backend.competence.CompetenceRepository competences;

 @org.springframework.beans.factory.annotation.Autowired
 void setControles(odk.malicompetences.backend.centre.HabilitationService habilitation,
                   EvaluateurHabilitationRepository habilitations,
                   odk.malicompetences.backend.competence.CompetenceRepository competences) {
  this.habilitation = habilitation;
  this.habilitations = habilitations;
  this.competences = competences;
 }

 public EvaluateurService(
         EvaluateurCentreRepository assignments,
         CitoyenRepository citoyens,
         CentreRepository centres,
         UtilisateurRepository utilisateurs,
         ValidationRepository validations,
         ValidationService validationService,
         CitoyenCompetenceRepository citoyenCompetences,
         ParcoursValidationService parcoursValidationService) {
  this.assignments = assignments;
  this.citoyens = citoyens;
  this.centres = centres;
  this.utilisateurs = utilisateurs;
  this.validations = validations;
  this.validationService = validationService;
  this.citoyenCompetences = citoyenCompetences;
  this.parcoursValidationService = parcoursValidationService;
 }

 public EvaluateurCentreResponse designer(DesignationEvaluateurRequest request, String authenticatedId) {
  UUID adminId = parseId(authenticatedId);

  Citoyen citoyen = citoyens.findById(request.citoyenId())
          .orElseThrow(() -> new ResourceNotFoundException("Citoyen introuvable"));

  if (citoyen.getUtilisateur() == null || citoyen.getUtilisateur().getRole() != Role.CITOYEN) {
   throw new IllegalArgumentException(
           "Seul un compte citoyen ordinaire peut être désigné évaluateur. Son rôle reste CITOYEN.");
  }

  Centre centre = centres.findById(request.centreId())
          .orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));

  Utilisateur admin = utilisateurs.findById(adminId)
          .orElseThrow(() -> new ResourceNotFoundException("Administrateur introuvable"));

  boolean adminGlobal = admin.getRole() == Role.ADMIN || admin.getRole() == Role.SUPER_ADMIN;
  // Droit CENTRE_ADMIN = rattachement au centre (consignes V1.2).
  boolean adminDuCentre = centre.getAdministrateur() != null
          && centre.getAdministrateur().getId().equals(adminId);

  if (!adminGlobal && !adminDuCentre) {
   throw new AccessDeniedException(
           "Seul l'administrateur de ce centre ou un administrateur national peut désigner un évaluateur.");
  }

  EvaluateurCentre evaluation = assignments.findByCitoyenIdAndCentreId(request.citoyenId(), request.centreId())
          .orElseGet(EvaluateurCentre::new);

  evaluation.setCitoyen(citoyen);
  evaluation.setCentre(centre);
  evaluation.setDesignePar(admin);
  evaluation.setActif(true);
  evaluation.setSpecialite(request.specialite());

  return EvaluateurCentreResponse.from(assignments.save(evaluation));
 }

 public EvaluateurDemandeResponse creerDemande(ValidationRequest request, String userId) {
  UUID citizenId = parseId(userId);

  var competence = citoyenCompetences.findById(request.citoyenCompetenceId())
          .orElseThrow(() -> new ResourceNotFoundException("Compétence introuvable"));

  if (!competence.getCitoyen().getId().equals(citizenId)) {
   throw new AccessDeniedException("Vous ne pouvez demander la validation que de vos propres compétences.");
  }

  if (competence.getEtat() == EtatCompetence.VALIDEE) {
   throw new IllegalStateException("Cette compétence est déjà validée.");
  }

  if (request.type() == null) {
   throw new IllegalArgumentException("Le type d'évaluation est obligatoire.");
  }
  // Une demande auprès d'un centre : évaluation pratique, sur portfolio, ou par un maître artisan référent.
  odk.malicompetences.backend.parcours.ModaliteValidation modalite = switch (request.type()) {
   case VALIDATION_PRATIQUE -> odk.malicompetences.backend.parcours.ModaliteValidation.PRATIQUE_CENTRE;
   case PORTFOLIO_MEDIA -> odk.malicompetences.backend.parcours.ModaliteValidation.PORTFOLIO_MEDIA;
   case MAITRE_ARTISAN -> odk.malicompetences.backend.parcours.ModaliteValidation.MAITRE_ARTISAN;
   default -> throw new IllegalArgumentException(
           "Une demande auprès d'un centre doit être une évaluation pratique, sur portfolio ou par un maître artisan.");
  };
  // Les règles de la compétence décident des modalités permises.
  if (!parcoursValidationService.modaliteAutorisee(competence, modalite)) {
   throw new IllegalStateException("Cette modalité d'évaluation n'est pas autorisée pour cette compétence.");
  }
  parcoursValidationService.verifierModaliteImposee(competence, modalite);
  parcoursValidationService.verifierPrerequis(competence, modalite);

  // L'évaluation est elle-même la preuve : aucun document à joindre pour une modalité autorisée.
  if (request.centreId() == null) {
   throw new IllegalArgumentException("Le centre d'évaluation est obligatoire.");
  }

  if (validations.existsByCitoyenCompetenceIdAndCentreIdAndStatut(
          request.citoyenCompetenceId(), request.centreId(), StatutValidation.EN_ATTENTE)) {
   throw new IllegalStateException("Une demande est déjà en attente pour cette compétence dans ce centre.");
  }

  // Centre actif et agréé pour la compétence ; évaluateur habilité pour cette compétence.
  Centre centreChoisi = centres.findById(request.centreId())
          .orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));
  habilitation.verifierCentre(centreChoisi, competence.getCompetence());
  EvaluateurCentre evaluateur = assignments.findByCentreIdAndActifTrue(request.centreId()).stream()
          .filter(e -> !e.getCitoyen().getId().equals(citizenId))
          .filter(e -> habilitation.evaluateurHabilite(e, competence.getCompetence()))
          .filter(e -> modalite != odk.malicompetences.backend.parcours.ModaliteValidation.MAITRE_ARTISAN
                  || (e.getSpecialite() != null && !e.getSpecialite().isBlank()))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Ce centre n'a pas d'évaluateur habilité disponible pour cette compétence."));

  // Le validateur associé à la demande est l'évaluateur du centre, pas le citoyen demandeur.
  ValidationResponse created = validationService.creerValidation(request, evaluateur.getCitoyen().getId().toString());

  Validation entity = validations.findById(created.id())
          .orElseThrow(() -> new ResourceNotFoundException("Demande créée introuvable"));

  return EvaluateurDemandeResponse.from(entity);
 }

 @Transactional(readOnly = true)
 public List<EvaluateurCentreResponse> mesCentres(String userId) {
  return assignments.findByCitoyenIdAndActifTrue(parseId(userId)).stream()
          .map(EvaluateurCentreResponse::from)
          .toList();
 }

 @Transactional(readOnly = true)
 public List<CentreResponse> centresDisponibles(String userId) {
  return centresDisponibles(userId, null);
 }

 /** Centres actifs disposant d'un évaluateur habilité ; filtrés sur la compétence si elle est connue. */
 @Transactional(readOnly = true)
 public List<CentreResponse> centresDisponibles(String userId, UUID citoyenCompetenceId) {
  UUID citizenId = parseId(userId);
  var competence = citoyenCompetenceId == null ? null : citoyenCompetences.findById(citoyenCompetenceId)
          .filter(cc -> cc.getCitoyen().getId().equals(citizenId))
          .map(cc -> cc.getCompetence())
          .orElse(null);
  return centres.findAll().stream()
          .filter(c -> c.getStatut() == odk.malicompetences.backend.centre.StatutCentre.ACTIF)
          .filter(c -> assignments.findByCentreIdAndActifTrue(c.getId()).stream()
                  .anyMatch(e -> !e.getCitoyen().getId().equals(citizenId)
                          && (competence == null || habilitation.evaluateurHabilite(e, competence))))
          .map(c -> new CentreResponse(c.getId(), c.getNom(), c.getDescription(), c.getAdresse(), c.getTelephone()))
          .toList();
 }

 @Transactional(readOnly = true)
 public List<EvaluateurCentreResponse> listerPourCentre(UUID centreId, String authenticatedId) {
  UUID userId = parseId(authenticatedId);

  Centre centre = centres.findById(centreId)
          .orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));

  Utilisateur user = utilisateurs.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

  boolean global = user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN;
  boolean centreAdmin = centre.getAdministrateur() != null
          && centre.getAdministrateur().getId().equals(userId);

  if (!global && !centreAdmin) {
   throw new AccessDeniedException("Vous ne pouvez consulter que les évaluateurs de votre centre.");
  }

  return assignments.findByCentreId(centreId).stream()
          .map(EvaluateurCentreResponse::from)
          .toList();
 }

 public EvaluateurCentreResponse modifierActivation(UUID centreId, UUID citoyenId, boolean actif, String authenticatedId) {
  UUID userId = parseId(authenticatedId);

  Centre centre = centres.findById(centreId)
          .orElseThrow(() -> new ResourceNotFoundException("Centre introuvable"));

  Utilisateur user = utilisateurs.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

  boolean global = user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN;
  boolean centreAdmin = centre.getAdministrateur() != null
          && centre.getAdministrateur().getId().equals(userId);

  if (!global && !centreAdmin) {
   throw new AccessDeniedException("Seul l'administrateur de ce centre peut modifier l'agrément.");
  }

  EvaluateurCentre evaluation = assignments.findByCitoyenIdAndCentreId(citoyenId, centreId)
          .orElseThrow(() -> new ResourceNotFoundException("Désignation introuvable"));

  evaluation.setActif(actif);
  return EvaluateurCentreResponse.from(assignments.save(evaluation));
 }

 @Transactional(readOnly = true)
 public List<EvaluateurDemandeResponse> demandes(String userId) {
  Set<UUID> centreIds = assignments.findByCitoyenIdAndActifTrue(parseId(userId)).stream()
          .map(e -> e.getCentre().getId())
          .collect(Collectors.toSet());

  return centreIds.stream()
          .flatMap(centreId -> validations.findByCentreIdAndStatut(centreId, StatutValidation.EN_ATTENTE).stream())
          .distinct()
          .sorted(Comparator.comparing(Validation::getDateDemande).reversed())
          .map(EvaluateurDemandeResponse::from)
          .toList();
 }

 public ValidationResponse decider(UUID validationId, StatutValidation statut, String commentaire, String userId) {
  return decider(validationId, statut, commentaire, null, userId);
 }

 public ValidationResponse decider(UUID validationId, StatutValidation statut, String commentaire,
                                   odk.malicompetences.backend.competence.NiveauCompetence niveauValide, String userId) {
  UUID id = parseId(userId);

  Validation v = validations.findByIdForUpdate(validationId)
          .orElseThrow(() -> new ResourceNotFoundException("Demande de validation introuvable"));

  if (v.getCentre() == null || !assignments.existsByCitoyenIdAndCentreIdAndActifTrue(id, v.getCentre().getId())) {
   throw new AccessDeniedException("Vous n'êtes pas évaluateur actif dans le centre concerné.");
  }
  // Habilitation active, valable et adaptée à la compétence et à la méthode (évaluation pratique en centre).
  EvaluateurCentre moi = assignments.findByCitoyenIdAndCentreId(id, v.getCentre().getId()).orElse(null);
  if (v.getType() != TypeValidation.VALIDATION_PRATIQUE && v.getType() != TypeValidation.PORTFOLIO_MEDIA
          && v.getType() != TypeValidation.MAITRE_ARTISAN) {
   throw new AccessDeniedException("Un évaluateur de centre ne décide que des évaluations pratiques, sur portfolio ou par maître artisan.");
  }
  if (!habilitation.evaluateurHabilite(moi, v.getCitoyenCompetence().getCompetence())) {
   throw new AccessDeniedException("Votre habilitation ne couvre pas cette compétence, a expiré, ou le centre n'est plus agréé.");
  }

  if (v.getCitoyenCompetence().getCitoyen().getId().equals(id)) {
   throw new AccessDeniedException("Un évaluateur ne peut pas valider sa propre compétence.");
  }

  if (v.getStatut() != StatutValidation.EN_ATTENTE) {
   throw new IllegalStateException("Cette demande a déjà été traitée.");
  }

  if (statut == null || statut == StatutValidation.EN_ATTENTE) {
   throw new IllegalArgumentException("La décision doit être APPROUVEE ou REJETEE.");
  }

  return validationService.decider(validationId, statut, commentaire, niveauValide, id);
 }

 private UUID parseId(String raw) {
  try {
   return UUID.fromString(raw);
  } catch (Exception e) {
   throw new AccessDeniedException("Identifiant utilisateur invalide.");
  }
 }
}