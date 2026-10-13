import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/upload.dart';
import 'package:mlc_mobile/features/competences/data/mock_competences_repository.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_repository.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

/// État en mémoire. S'appuie sur le mock des compétences (accepté : couche données de test uniquement).
class MockPreuveRepository implements PreuveRepository {
  const MockPreuveRepository();

  static final List<Preuve> _items = _initial();

  static List<Preuve> _initial() => [
        Preuve(
          id: 'p-1',
          type: PreuveType.certificat,
          citoyenCompetenceId: 'cc-1',
          competenceNom: "Soudure à l'arc",
          date: DateTime(2026, 1, 20),
          fichierNom: 'certificat-soudure.pdf',
          fichierUrl: 'https://example.invalid/mock/certificat-soudure.pdf',
          statut: ValidationStatut.approuvee,
        ),
        Preuve(
          id: 'p-2',
          type: PreuveType.attestationEmployeur,
          citoyenCompetenceId: 'cc-2',
          competenceNom: 'Couture professionnelle',
          date: DateTime(2026, 3, 10),
          fichierNom: 'attestation-atelier.pdf',
          fichierUrl: 'https://example.invalid/mock/attestation-atelier.pdf',
          statut: ValidationStatut.enAttente,
        ),
      ];

  /// Pour les tests : remet l'état initial.
  static void resetForTests() => _items
    ..clear()
    ..addAll(_initial());

  /// Simule le statut de la validation associée (demande envoyée, approuvée, rejetée).
  static void setStatut(String preuveId, ValidationStatut? statut) {
    final i = _items.indexWhere((p) => p.id == preuveId);
    if (i < 0) return;
    final p = _items[i];
    _items[i] = Preuve(
      id: p.id,
      type: p.type,
      citoyenCompetenceId: p.citoyenCompetenceId,
      competenceNom: p.competenceNom,
      date: p.date,
      fichierNom: p.fichierNom,
      fichierUrl: p.fichierUrl,
      statut: statut,
    );
  }

  static void addTestResult({
    required String preuveId,
    required String competenceId,
    required String competenceName,
    required DateTime date,
  }) {
    _items.removeWhere((item) => item.id == preuveId);
    _items.add(
      Preuve(
        id: preuveId,
        type: PreuveType.testNumerique,
        citoyenCompetenceId: competenceId,
        competenceNom: competenceName,
        date: date,
        fichierNom: 'resultat-test.pdf',
      ),
    );
  }

  Future<void> _latency([int ms = 300]) =>
      Future<void>.delayed(Duration(milliseconds: ms));

  @override
  Future<List<Preuve>> listMine({int page = 0, int size = 20}) async {
    await _latency(400);
    final sorted = [..._items]..sort((a, b) => b.date.compareTo(a.date));
    return sorted.skip(page * size).take(size).toList();
  }

  @override
  Future<List<Preuve>> listForCompetence(String citoyenCompetenceId) async {
    await _latency();
    final list = _items
        .where((p) => p.citoyenCompetenceId == citoyenCompetenceId)
        .toList()
      ..sort((a, b) => b.date.compareTo(a.date));
    return list;
  }

  @override
  Future<Preuve> get(String id) async {
    await _latency();
    return _items.firstWhere((p) => p.id == id,
        orElse: () => throw const NotFoundFailure());
  }

  @override
  Future<Preuve> add({
    required String citoyenCompetenceId,
    required PreuveType type,
    required PickedMedia fichier,
    void Function(double progress)? onProgress,
  }) async {
    final competence = await const MockCompetencesRepository()
        .get(citoyenCompetenceId); // NotFoundFailure si inconnue
    await simulateUpload(
        onProgress: onProgress, stepDelay: const Duration(milliseconds: 100));
    final created = Preuve(
      id: 'p-${_items.length + 1}',
      type: type,
      citoyenCompetenceId: citoyenCompetenceId,
      competenceNom: competence.competenceNom,
      date: DateTime.now(),
      fichierNom: fichier.name,
      fichierUrl: 'https://example.invalid/mock/${fichier.name}',
    );
    _items.add(created);
    MockCompetencesRepository.markAttested(citoyenCompetenceId);
    return created;
  }
}
