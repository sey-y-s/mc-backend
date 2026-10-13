import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/domain/competence_ref.dart';
import 'package:mlc_mobile/features/competences/domain/competences_repository.dart';

/// État en mémoire (statique) : un ajout reste visible tant que l'app tourne.
class MockCompetencesRepository implements CompetencesRepository {
  const MockCompetencesRepository();

  static final List<CitoyenCompetence> _items = _initial();

  static List<CitoyenCompetence> _initial() => [
        CitoyenCompetence(
            id: 'cc-1',
            competenceId: 'c-1',
            competenceNom: "Soudure à l'arc",
            secteurNom: 'Métallurgie',
            niveau: Niveau.expert,
            etat: EtatCompetence.validee,
            dateDeclaration: DateTime(2026, 1, 12)),
        CitoyenCompetence(
            id: 'cc-2',
            competenceId: 'c-2',
            competenceNom: 'Couture professionnelle',
            secteurNom: 'Textile',
            niveau: Niveau.intermediaire,
            etat: EtatCompetence.attestee,
            dateDeclaration: DateTime(2026, 3, 2)),
        CitoyenCompetence(
            id: 'cc-3',
            competenceId: 'c-3',
            competenceNom: 'Réparation de smartphones',
            secteurNom: 'Électronique',
            niveau: Niveau.debutant,
            etat: EtatCompetence.declaree,
            dateDeclaration: DateTime(2026, 6, 20)),
      ];

  /// Pour les tests : remet l'état initial.
  static void resetForTests() => _items
    ..clear()
    ..addAll(_initial());

  /// Simule la règle serveur : une première preuve rend la compétence « attestée ».
  static void markAttested(String citoyenCompetenceId) {
    final i = _items.indexWhere((c) => c.id == citoyenCompetenceId);
    if (i < 0 || _items[i].etat != EtatCompetence.declaree) return;
    final c = _items[i];
    _items[i] = CitoyenCompetence(
      id: c.id,
      competenceId: c.competenceId,
      competenceNom: c.competenceNom,
      secteurNom: c.secteurNom,
      niveau: c.niveau,
      etat: EtatCompetence.attestee,
      dateDeclaration: c.dateDeclaration,
    );
  }

  /// Simule la décision d'un évaluateur : la compétence devient « validée ».
  static void markValidated(String citoyenCompetenceId) {
    final i = _items.indexWhere((c) => c.id == citoyenCompetenceId);
    if (i < 0) return;
    final c = _items[i];
    _items[i] = CitoyenCompetence(
      id: c.id,
      competenceId: c.competenceId,
      competenceNom: c.competenceNom,
      secteurNom: c.secteurNom,
      niveau: c.niveau,
      etat: EtatCompetence.validee,
      dateDeclaration: c.dateDeclaration,
    );
  }

  static const _referentiel = [
    CompetenceRef(id: 'c-1', nom: "Soudure à l'arc", secteurNom: 'Métallurgie'),
    CompetenceRef(
        id: 'c-2', nom: 'Couture professionnelle', secteurNom: 'Textile'),
    CompetenceRef(
        id: 'c-3',
        nom: 'Réparation de smartphones',
        secteurNom: 'Électronique'),
    CompetenceRef(id: 'c-4', nom: 'Maçonnerie', secteurNom: 'Bâtiment'),
    CompetenceRef(id: 'c-5', nom: 'Menuiserie', secteurNom: 'Bâtiment'),
    CompetenceRef(id: 'c-6', nom: 'Plomberie', secteurNom: 'Bâtiment'),
    CompetenceRef(
        id: 'c-7', nom: 'Électricité du bâtiment', secteurNom: 'Bâtiment'),
    CompetenceRef(id: 'c-8', nom: 'Mécanique moto', secteurNom: 'Mécanique'),
    CompetenceRef(
        id: 'c-9', nom: 'Cuisine et restauration', secteurNom: 'Restauration'),
    CompetenceRef(id: 'c-10', nom: 'Maraîchage', secteurNom: 'Agriculture'),
    CompetenceRef(
        id: 'c-11', nom: 'Comptabilité de base', secteurNom: 'Gestion'),
    CompetenceRef(
        id: 'c-12', nom: 'Coiffure', secteurNom: 'Services à la personne'),
  ];

  Future<void> _latency([int ms = 300]) =>
      Future<void>.delayed(Duration(milliseconds: ms));

  @override
  Future<List<CitoyenCompetence>> list({int page = 0, int size = 20}) async {
    await _latency(400);
    return _items.skip(page * size).take(size).toList();
  }

  @override
  Future<CitoyenCompetence> get(String id) async {
    await _latency();
    return _items.firstWhere((c) => c.id == id,
        orElse: () => throw const NotFoundFailure());
  }

  @override
  Future<CitoyenCompetence> add(
      {required String competenceId, required Niveau niveau}) async {
    await _latency();
    if (_items.any((c) => c.competenceId == competenceId)) {
      throw ValidationFailure('Vous avez déjà déclaré cette compétence.');
    }
    final ref = _referentiel.firstWhere((r) => r.id == competenceId,
        orElse: () => throw const NotFoundFailure());
    final created = CitoyenCompetence(
      id: 'cc-${_items.length + 1}',
      competenceId: ref.id,
      competenceNom: ref.nom,
      secteurNom: ref.secteurNom,
      niveau: niveau,
      etat: EtatCompetence.declaree,
      dateDeclaration: DateTime.now(),
    );
    _items.insert(0, created);
    return created;
  }

  @override
  Future<List<CompetenceRef>> searchReferentiel(String query) async {
    await _latency(200);
    final q = _fold(query.trim());
    return _referentiel
        .where((r) =>
            _fold(r.nom).contains(q) || _fold(r.secteurNom ?? '').contains(q))
        .take(20)
        .toList();
  }

  /// Minuscules sans accents : « electricite » trouve « Électricité ».
  static String _fold(String s) {
    const from = 'àâäáãçéèêëíîïìñóôöòõúùûüýÿ';
    const to = 'aaaaaceeeeiiiinooooouuuuyy';
    final b = StringBuffer();
    for (final r in s.toLowerCase().runes) {
      final ch = String.fromCharCode(r);
      final i = from.indexOf(ch);
      b.write(i >= 0 ? to[i] : ch);
    }
    return b.toString();
  }
}
