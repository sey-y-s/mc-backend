import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/competences/data/mock_competences_repository.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_repository.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_validators.dart';

/// État en mémoire, cohérent avec le mock des compétences (couche de test uniquement).
class MockExperienceRepository implements ExperienceRepository {
  const MockExperienceRepository();

  static final List<Experience> _items = _initial();
  static int _next = 3;

  static List<Experience> _initial() => [
        Experience(
          id: 'e-1',
          titre: 'Soudeur',
          entreprise: 'Atelier Diarra, Bamako',
          description: 'Fabrication de portails et de grilles de fenêtres. Travail à l\'arc et au chalumeau.',
          dateDebut: DateTime(2019, 3, 1),
          dateFin: DateTime(2023, 6, 30),
          competences: const [ExperienceCompetence(citoyenCompetenceId: 'cc-1', nom: "Soudure à l'arc")],
        ),
        Experience(
          id: 'e-2',
          titre: 'Couturière indépendante',
          entreprise: 'À mon compte',
          dateDebut: DateTime(2023, 9, 1),
          enCours: true,
          reconversion: true,
          competences: const [ExperienceCompetence(citoyenCompetenceId: 'cc-2', nom: 'Couture professionnelle')],
        ),
      ];

  /// Pour les tests : remet l'état initial.
  static void resetForTests() {
    _items
      ..clear()
      ..addAll(_initial());
    _next = 3;
  }

  Future<void> _latency([int ms = 300]) => Future<void>.delayed(Duration(milliseconds: ms));

  void _validate(ExperienceInput i) {
    final t = ExperienceValidators.titre(i.titre);
    if (t != null) throw ValidationFailure(t);
    final d = ExperienceValidators.dates(debut: i.dateDebut, fin: i.dateFin, enCours: i.enCours);
    if (d.isNotEmpty) throw ValidationFailure(d.values.first);
  }

  Future<List<ExperienceCompetence>> _resolve(List<String> ids) async {
    final all = await const MockCompetencesRepository().list(size: 100);
    return [
      for (final id in ids)
        () {
          final c = all.firstWhere((x) => x.id == id, orElse: () => throw const NotFoundFailure());
          return ExperienceCompetence(citoyenCompetenceId: c.id, nom: c.competenceNom);
        }(),
    ];
  }

  Experience _build(String id, ExperienceInput i, List<ExperienceCompetence> competences) => Experience(
        id: id,
        titre: i.titre.trim(),
        entreprise: i.entreprise,
        description: i.description,
        dateDebut: i.dateDebut,
        dateFin: i.enCours ? null : i.dateFin,
        enCours: i.enCours,
        reconversion: i.reconversion,
        competences: competences,
      );

  @override
  Future<List<Experience>> list({int page = 0, int size = 20}) async {
    await _latency(400);
    final sorted = [..._items]..sort((a, b) {
        if (a.enCours != b.enCours) return a.enCours ? -1 : 1;
        return b.dateDebut.compareTo(a.dateDebut);
      });
    return sorted.skip(page * size).take(size).toList();
  }

  @override
  Future<Experience> get(String id) async {
    await _latency();
    return _items.firstWhere((e) => e.id == id, orElse: () => throw const NotFoundFailure());
  }

  @override
  Future<Experience> create(ExperienceInput input) async {
    await _latency();
    _validate(input);
    final created = _build('e-${_next++}', input, await _resolve(input.citoyenCompetenceIds));
    _items.add(created);
    return created;
  }

  @override
  Future<Experience> update(String id, ExperienceInput input) async {
    await _latency();
    final index = _items.indexWhere((e) => e.id == id);
    if (index < 0) throw const NotFoundFailure();
    _validate(input);
    final updated = _build(id, input, await _resolve(input.citoyenCompetenceIds));
    _items[index] = updated;
    return updated;
  }

  @override
  Future<void> delete(String id) async {
    await _latency();
    final before = _items.length;
    _items.removeWhere((e) => e.id == id);
    if (_items.length == before) throw const NotFoundFailure();
  }
}