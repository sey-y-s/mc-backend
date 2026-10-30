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

   private odk.malicompetences.backend.notification.NotificationService notifications;

 @org.springframework.beans.factory.annotation.Autowired
 void setNotifications(odk.malicompetences.backend.notification.NotificationService notifications) {
  this.notifications = notifications;
 }

 private odk.malicompetences.backend.portfolio.PortfolioRealisationRepository portfolios;
 private odk.malicompetences.backend.portfolio.PortfolioRealisationMapper portfolioMapper;

 @org.springframework.beans.factory.annotation.Autowired
 void setPortfolio(odk.malicompetences.backend.portfolio.PortfolioRealisationRepository portfolios,
                   odk.malicompetences.backend.portfolio.PortfolioRealisationMapper portfolioMapper) {
  this.portfolios = portfolios;
  this.portfolioMapper = portfolioMapper;
 }

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

  // Notifié seulement pour une vraie nouvelle désignation (ou une réactivation), pas à chaque mise à jour.
  boolean nouvelleDesignation = evaluation.getId() == null || !evaluation.isActif();
  evaluation.setCitoyen(citoyen);
  evaluation.setCentre(centre);
  evaluation.setDesignePar(admin);
  evaluation.setActif(true);
  evaluation.setSpecialite(request.specialite());
  EvaluateurCentre enregistre = assignments.save(evaluation);
  if (nouvelleDesignation && notifications != null) {
   notifications.creerEtEnvoyerNotification(new odk.malicompetences.backend.notification.NotificationRequest(
           "Vous êtes évaluateur",
           "Le centre « " + centre.getNom() + " » vous a désigné évaluateur. Retrouvez vos demandes dans « Espace évaluateur ».",
           odk.malicompetences.backend.notification.TypeNotification.SYSTEME, List.of(citoyen.getId())));
  }
  return EvaluateurCentreResponse.from(enregistre);
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
  // Validation documentaire : pas de centre, c'est un administrateur qui examine la pièce jointe.
  if (request.type() == TypeValidation.DIPLOME_VERIFIE) {
   return creerDemandeDocumentaire(request, competence, citizenId);
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
  // Tous les évaluateurs habilités du centre sont prévenus : n'importe lequel peut traiter la demande.
  if (notifications != null) {
   List<UUID> aPrevenir = assignments.findByCentreIdAndActifTrue(request.centreId()).stream()
           .filter(e -> !e.getCitoyen().getId().equals(citizenId))
           .filter(e -> habilitation.evaluateurHabilite(e, competence.getCompetence()))
           .filter(e -> modalite != odk.malicompetences.backend.parcours.ModaliteValidation.MAITRE_ARTISAN
                   || (e.getSpecialite() != null && !e.getSpecialite().isBlank()))
           .map(e -> e.getCitoyen().getId())
           .toList();
   if (!aPrevenir.isEmpty()) {
    notifications.creerEtEnvoyerNotification(new odk.malicompetences.backend.notification.NotificationRequest(
            "Nouvelle demande d'évaluation",
            "Une demande d'évaluation (" + modalite.libelle() + ") attend votre décision pour la compétence « "
                    + competence.getCompetence().getNom() + " ». Ouvrez votre espace évaluateur.",
            odk.malicompetences.backend.notification.TypeNotification.VALIDATION, aPrevenir));
   }
  }


  Validation entity = validations.findById(created.id())
          .orElseThrow(() -> new ResourceNotFoundException("Demande créée introuvable"));

  return EvaluateurDemandeResponse.from(entity);
 }

 /**
  * La validation documentaire n'est liée à aucun centre : l'administrateur examine la pièce jointe
  * (diplôme, certificat, attestation…) et l'approuve ou la refuse. L'affectation de cet examen à un centre
  * ou à un maître artisan est une évolution prévue.
  */
 private EvaluateurDemandeResponse creerDemandeDocumentaire(
         ValidationRequest request,
         odk.malicompetences.backend.competence.CitoyenCompetence competence,
         UUID citizenId) {
  var modalite = odk.malicompetences.backend.parcours.ModaliteValidation.DOCUMENTAIRE;
  if (!parcoursValidationService.modaliteAutorisee(competence, modalite)) {
   throw new IllegalStateException("La validation documentaire n'est pas autorisée pour cette compétence.");
  }
  parcoursValidationService.verifierModaliteImposee(competence, modalite);
  if (request.preuveId() == null) {
   throw new IllegalArgumentException("Joignez d'abord un justificatif (diplôme, certificat, attestation…).");
  }
  if (validations.existsByCitoyenCompetenceIdAndStatut(request.citoyenCompetenceId(), StatutValidation.EN_ATTENTE)) {
   throw new IllegalStateException("Une demande est déjà en cours d'examen pour cette compétence.");
  }
  Utilisateur examinateur = utilisateurs.findAll().stream()
          .filter(Utilisateur::isActif)
          .filter(u -> u.getRole() == Role.SUPER_ADMIN || u.getRole() == Role.ADMIN)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Aucun administrateur n'est disponible pour examiner votre justificatif."));
  ValidationRequest sansCentre = new ValidationRequest(request.citoyenCompetenceId(), request.preuveId(), null,
          TypeValidation.DIPLOME_VERIFIE, request.commentaire(), request.dateExpiration());
  ValidationResponse created = validationService.creerValidation(sansCentre, examinateur.getId().toString());
    Validation entity = validations.findById(created.id())
          .orElseThrow(() -> new ResourceNotFoundException("Demande créée introuvable"));
  // Les administrateurs sont prévenus : un justificatif attend leur examen.
  List<UUID> admins = utilisateurs.findAll().stream()
          .filter(Utilisateur::isActif)
          .filter(u -> u.getRole() == Role.SUPER_ADMIN || u.getRole() == Role.ADMIN)
          .map(Utilisateur::getId)
          .toList();
  if (notifications != null && !admins.isEmpty()) {
   notifications.creerEtEnvoyerNotification(new odk.malicompetences.backend.notification.NotificationRequest(
           "Justificatif à examiner",
           "Un citoyen a envoyé un justificatif pour la compétence « "
                   + competence.getCompetence().getNom() + " ». Ouvrez les validations pour l'examiner.",
           odk.malicompetences.backend.notification.TypeNotification.VALIDATION, admins));
  }
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
          .filter(v -> !v.getCitoyenCompetence().getCitoyen().getId().equals(parseId(userId)))
          .distinct()
          .sorted(Comparator.comparing(Validation::getDateDemande).reversed())
          .map(EvaluateurDemandeResponse::from)
          .toList();
 }

  /** Décisions déjà rendues par les centres de l'évaluateur, les plus récentes d'abord. */
 @Transactional(readOnly = true)
 public List<EvaluateurDemandeResponse> historique(String userId) {
  Set<UUID> centreIds = assignments.findByCitoyenIdAndActifTrue(parseId(userId)).stream()
          .map(e -> e.getCentre().getId())
          .collect(Collectors.toSet());

  return centreIds.stream()
                    .flatMap(centreId -> validations.findByCentreIdAndStatutNot(centreId, StatutValidation.EN_ATTENTE).stream())
          .filter(v -> !v.getCitoyenCompetence().getCitoyen().getId().equals(parseId(userId)))
          .distinct()
          .sorted(Comparator.comparing(Validation::getDateDemande).reversed())
          .map(EvaluateurDemandeResponse::from)
          .toList();
 }

 /**
  * Réalisations du citoyen, pour une évaluation sur portfolio : seul un évaluateur actif du centre qui a reçu
  * la demande peut les consulter, et seulement tant que l'on évalue son portfolio.
  */
 @Transactional(readOnly = true)
 public List<odk.malicompetences.backend.portfolio.PortfolioRealisationResponse> portfolioDeLaDemande(UUID validationId, String userId) {
  UUID id = parseId(userId);
  Validation v = validations.findById(validationId)
          .orElseThrow(() -> new ResourceNotFoundException("Demande de validation introuvable"));
  if (v.getCentre() == null || !assignments.existsByCitoyenIdAndCentreIdAndActifTrue(id, v.getCentre().getId())) {
   throw new AccessDeniedException("Vous n'êtes pas évaluateur actif dans le centre concerné.");
  }
  if (v.getType() != TypeValidation.PORTFOLIO_MEDIA) {
   throw new AccessDeniedException("Le portfolio n'est consultable que pour une évaluation sur portfolio.");
  }
  UUID citoyenId = v.getCitoyenCompetence().getCitoyen().getId();
  return portfolios.findByCitoyenId(citoyenId).stream().map(portfolioMapper::toResponse).toList();
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