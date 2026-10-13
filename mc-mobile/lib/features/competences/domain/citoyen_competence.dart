enum Niveau {
  debutant('Débutant'),
  intermediaire('Intermédiaire'),
  expert('Expert');

  const Niveau(this.label);
  final String label;
  static Niveau fromApi(String v) =>
      Niveau.values.firstWhere((e) => e.name.toUpperCase() == v.toUpperCase(), orElse: () => Niveau.debutant);
}

enum EtatCompetence {
  declaree('Déclarée'),
  attestee('Attestée'),
  validee('Validée');

  const EtatCompetence(this.label);
  final String label;
  static EtatCompetence fromApi(String v) =>
      EtatCompetence.values.firstWhere((e) => e.name.toUpperCase() == v.toUpperCase(), orElse: () => EtatCompetence.declaree);
}

/// Classe d'association Citoyen <-> Competence (niveau, état, dateDeclaration).
/// Contrat JSON provisoire (DTO plat) : voir fromJson.
class CitoyenCompetence {
  const CitoyenCompetence({
    required this.id,
    required this.competenceId,
    required this.competenceNom,
    required this.niveau,
    required this.etat,
    required this.dateDeclaration,
    this.secteurNom,
  });

  final String id;
  final String competenceId;
  final String competenceNom;
  final String? secteurNom;
  final Niveau niveau;
  final EtatCompetence etat;
  final DateTime dateDeclaration;

  factory CitoyenCompetence.fromJson(Map<String, dynamic> j) => CitoyenCompetence(
        id: j['id'] as String,
        competenceId: j['competenceId'] as String,
        competenceNom: j['competenceNom'] as String,
        secteurNom: j['secteurNom'] as String?,
        niveau: Niveau.fromApi(j['niveau'] as String),
        etat: EtatCompetence.fromApi(j['etat'] as String),
        dateDeclaration: DateTime.parse(j['dateDeclaration'] as String),
      );
}
