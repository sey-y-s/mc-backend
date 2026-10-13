import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

enum PreuveType {
  diplome('DIPLOME', 'Diplôme', 'Diplôme obtenu après une formation.'),
  certificat(
      'CERTIFICAT', 'Certificat', 'Certificat de formation ou de stage.'),
  attestationEmployeur('ATTESTATION_EMPLOYEUR', "Attestation d'employeur",
      "Document d'un employeur ou d'un client qui confirme votre travail."),
  portfolio('PORTFOLIO', 'Portfolio',
      'Réalisations présentées dans votre portfolio.'),
  evaluationPratique('EVALUATION_PRATIQUE', 'Évaluation pratique',
      'Évaluation réalisée par un centre ou un évaluateur.'),
  testNumerique('TEST_NUMERIQUE', 'Test numérique',
      "Résultat d'un test passé dans l'application."),
  dossierVae('DOSSIER_VAE', 'Dossier VAE',
      "Dossier de validation des acquis de l'expérience."),
  autre('AUTRE', 'Autre document',
      'Tout autre document qui montre votre compétence.');

  const PreuveType(this.apiCode, this.label, this.description);
  final String apiCode;
  final String label;
  final String description;

  /// Types que le citoyen peut ajouter lui-même. Les autres sont produits par la plateforme ou un centre.
  static const addable = [diplome, certificat, attestationEmployeur, autre];

  static PreuveType fromApi(String v) => PreuveType.values
      .firstWhere((t) => t.apiCode == v, orElse: () => PreuveType.autre);
}

/// Preuve liée à une compétence du citoyen. Contrat JSON provisoire (DTO plat) : voir fromJson.
class Preuve {
  const Preuve({
    required this.id,
    required this.type,
    required this.citoyenCompetenceId,
    required this.competenceNom,
    required this.date,
    this.fichierNom,
    this.fichierUrl,
    this.statut,
  });

  final String id;
  final PreuveType type;
  final String citoyenCompetenceId;
  final String competenceNom;
  final DateTime date;
  final String? fichierNom;
  final String? fichierUrl;

  /// Statut de la validation associée ; null = aucune validation demandée.
  final ValidationStatut? statut;

  bool get isImage {
    final n = (fichierNom ?? '').toLowerCase();
    return n.endsWith('.jpg') ||
        n.endsWith('.jpeg') ||
        n.endsWith('.png') ||
        n.endsWith('.webp');
  }

  factory Preuve.fromJson(Map<String, dynamic> j) => Preuve(
        id: j['id'] as String,
        type: PreuveType.fromApi(j['type'] as String),
        citoyenCompetenceId: j['citoyenCompetenceId'] as String,
        competenceNom: j['competenceNom'] as String,
        date: DateTime.parse(j['date'] as String),
        fichierNom: j['fichierNom'] as String?,
        fichierUrl: j['fichierUrl'] as String?,
        statut: j['statut'] == null
            ? null
            : ValidationStatut.fromApi(j['statut'] as String),
      );
}
