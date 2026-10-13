import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

abstract interface class ValidationRepository {
  /// Validations du citoyen connecté, dernière activité d'abord (paginé), filtrables par statut.
  Future<List<Validation>> listMine({ValidationStatut? statut, int page = 0, int size = 20});

  /// Validations d'une compétence, demande la plus récente d'abord.
  Future<List<Validation>> listForCompetence(String citoyenCompetenceId);

  /// Lève NotFoundFailure (inexistante) ou ForbiddenFailure (pas au citoyen).
  Future<Validation> get(String id);

  /// Demande la validation d'une compétence à partir d'une de ses preuves. Statut initial : EN_ATTENTE.
  /// Lève ValidationFailure si : compétence déjà validée, preuve d'une autre compétence,
  /// demande déjà en cours ou preuve déjà validée.
  Future<Validation> request({required String citoyenCompetenceId, required String preuveId});
}