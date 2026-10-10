import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/competences/data/mock_competences_repository.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/preuves/data/mock_preuve_repository.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_repository.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

/// État en mémoire, cohérent avec les mocks des compétences et des preuves (couche de test uniquement).
class MockValidationRepository implements ValidationRepository {
  const MockValidationRepository();

  static final List<Validation> _items = _initial();

  static List<Validation> _initial() => [
        Validation(
          id: 'v-1',
          type: ValidationType.validationPratique,
          statut: ValidationStatut.approuvee,
          citoyenCompetenceId: 'cc-1',
          competenceNom: "Soudure à l'arc",
          preuveId: 'p-1',
          validateurLabel: 'Centre de formation de Kati',
          dateDemande: DateTime(2026, 1, 25),
          dateDecision: DateTime(2026, 2, 5),
        ),
        Validation(
          id: 'v-3',
          type: ValidationType.partenaireAtteste,
          statut: ValidationStatut.rejetee,
          citoyenCompetenceId: 'cc-2',
          competenceNom: 'Couture professionnelle',
          preuveId: 'p-2',
          validateurLabel: 'Évaluateur MaliCompétences',
          dateDemande: DateTime(2026, 3, 12),
          dateDecision: DateTime(2026, 3, 20),
          commentaire: 'Le document est illisible. Ajoutez une photo plus nette.',
        ),
        Validation(
          id: 'v-2',
          type: ValidationType.partenaireAtteste,
          statut: ValidationStatut.enAttente,
          citoyenCompetenceId: 'cc-2',
          competenceNom: 'Couture professionnelle',
          preuveId: 'p-2',
          dateDemande: DateTime(2026, 4, 2),
        ),
      ];

  /// Pour les tests : remet l'état initial.
  static void resetForTests() => _items
    ..clear()
    ..addAll(_initial());

  /// Simule la décision d'un évaluateur ou d'un centre. Aucun écran mobile ne décide.
  static void decide(String id, {required bool approved, String? commentaire, String validateur = 'Évaluateur MaliCompétences'}) {
    final i = _items.indexWhere((v) => v.id == id);
    if (i < 0) return;
    final statut = approved ? ValidationStatut.approuvee : ValidationStatut.rejetee;
    final v = _items[i].copyWith(statut: statut, dateDecision: DateTime.now(), validateurLabel: validateur, commentaire: commentaire);
    _items[i] = v;
    if (v.preuveId != null) MockPreuveRepository.setStatut(v.preuveId!, statut);
    if (approved) MockCompetencesRepository.markValidated(v.citoyenCompetenceId);
  }

  /// Simule la règle serveur qui déduit le type de validation à partir de la preuve.
  static ValidationType _typeFor(PreuveType t) => switch (t) {
        PreuveType.diplome || PreuveType.certificat => ValidationType.diplomeVerifie,
        PreuveType.attestationEmployeur => ValidationType.partenaireAtteste,
        PreuveType.testNumerique => ValidationType.testNumerique,
        PreuveType.dossierVae => ValidationType.vae,
        PreuveType.evaluationPratique || PreuveType.portfolio || PreuveType.autre => ValidationType.validationPratique,
      };

  Future<void> _latency([int ms = 300]) => Future<void>.delayed(Duration(milliseconds: ms));

  @override
  Future<List<Validation>> listMine({ValidationStatut? statut, int page = 0, int size = 20}) async {
    await _latency(400);
    final list = _items.where((v) => statut == null || v.statut == statut).toList()
      ..sort((a, b) => b.activityDate.compareTo(a.activityDate));
    return list.skip(page * size).take(size).toList();
  }

  @override
  Future<List<Validation>> listForCompetence(String citoyenCompetenceId) async {
    await _latency();
    return _items.where((v) => v.citoyenCompetenceId == citoyenCompetenceId).toList()
      ..sort((a, b) => b.dateDemande.compareTo(a.dateDemande));
  }

  @override
  Future<Validation> get(String id) async {
    await _latency();
    return _items.firstWhere((v) => v.id == id, orElse: () => throw const NotFoundFailure());
  }

  @override
  Future<Validation> request({required String citoyenCompetenceId, required String preuveId}) async {
    await _latency();
    final competence = await const MockCompetencesRepository().get(citoyenCompetenceId); // NotFoundFailure si inconnue
    if (competence.etat == EtatCompetence.validee) throw ValidationFailure('Cette compétence est déjà validée.');
    final preuve = await const MockPreuveRepository().get(preuveId); // NotFoundFailure si inconnue
    if (preuve.citoyenCompetenceId != citoyenCompetenceId) throw ValidationFailure('Cette preuve ne correspond pas à la compétence.');
    if (preuve.statut == ValidationStatut.enAttente) throw ValidationFailure('Une demande est déjà en cours pour cette preuve.');
    if (preuve.statut == ValidationStatut.approuvee) throw ValidationFailure('Cette preuve est déjà validée.');
    final created = Validation(
      id: 'v-${_items.length + 1}',
      type: _typeFor(preuve.type),
      statut: ValidationStatut.enAttente,
      citoyenCompetenceId: citoyenCompetenceId,
      competenceNom: competence.competenceNom,
      preuveId: preuveId,
      dateDemande: DateTime.now(),
    );
    _items.add(created);
    MockPreuveRepository.setStatut(preuveId, ValidationStatut.enAttente);
    return created;
  }
}