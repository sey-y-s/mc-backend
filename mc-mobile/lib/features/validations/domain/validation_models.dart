import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

enum ValidationType {
  diplomeVerifie('DIPLOME_VERIFIE', 'Diplôme vérifié'),
  partenaireAtteste('PARTENAIRE_ATTESTE', 'Attestation de partenaire'),
  validationPratique('VALIDATION_PRATIQUE', 'Évaluation pratique'),
  testNumerique('TEST_NUMERIQUE', 'Test numérique'),
  vae('VAE', 'Validation des acquis (VAE)'),

  /// Repli côté mobile pour une valeur inconnue du serveur (ne fait pas partie du contrat).
  autre('AUTRE', 'Validation');

  const ValidationType(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static ValidationType fromApi(String v) => ValidationType.values.firstWhere((t) => t.apiCode == v, orElse: () => ValidationType.autre);
}

/// Validation d'une compétence. Le type est décidé par la plateforme ou le validateur, jamais par le citoyen.
/// Contrat JSON provisoire (DTO plat) : voir fromJson.
class Validation {
  const Validation({
    required this.id,
    required this.type,
    required this.statut,
    required this.citoyenCompetenceId,
    required this.competenceNom,
    required this.dateDemande,
    this.preuveId,
    this.validateurLabel,
    this.dateDecision,
    this.commentaire,
  });

  final String id;
  final ValidationType type;
  final ValidationStatut statut;
  final String citoyenCompetenceId;
  final String competenceNom;
  final String? preuveId;

  /// Qui a décidé (centre ou évaluateur). Absent tant que la demande est en attente.
  final String? validateurLabel;
  final DateTime dateDemande;
  final DateTime? dateDecision;
  final String? commentaire;

  /// Date de la dernière activité : décision si elle existe, sinon demande.
  DateTime get activityDate => dateDecision ?? dateDemande;

  Validation copyWith({ValidationStatut? statut, DateTime? dateDecision, String? validateurLabel, String? commentaire}) => Validation(
        id: id,
        type: type,
        statut: statut ?? this.statut,
        citoyenCompetenceId: citoyenCompetenceId,
        competenceNom: competenceNom,
        preuveId: preuveId,
        validateurLabel: validateurLabel ?? this.validateurLabel,
        dateDemande: dateDemande,
        dateDecision: dateDecision ?? this.dateDecision,
        commentaire: commentaire ?? this.commentaire,
      );

  factory Validation.fromJson(Map<String, dynamic> j) => Validation(
        id: j['id'] as String,
        type: ValidationType.fromApi(j['type'] as String),
        statut: ValidationStatut.fromApi(j['statut'] as String),
        citoyenCompetenceId: j['citoyenCompetenceId'] as String,
        competenceNom: j['competenceNom'] as String,
        preuveId: j['preuveId'] as String?,
        validateurLabel: j['validateurLabel'] as String?,
        dateDemande: DateTime.parse(j['dateDemande'] as String),
        dateDecision: j['dateDecision'] == null ? null : DateTime.parse(j['dateDecision'] as String),
        commentaire: j['commentaire'] as String?,
      );
}