import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';

abstract interface class ExperienceRepository {
  /// Expériences du citoyen connecté : en cours d'abord, puis par date de début décroissante (paginé).
  Future<List<Experience>> list({int page = 0, int size = 20});

  /// Lève NotFoundFailure (inexistante) ou ForbiddenFailure (pas au citoyen).
  Future<Experience> get(String id);

  /// Lève ValidationFailure si titre ou dates invalides ; NotFoundFailure si une compétence liée est inconnue.
  Future<Experience> create(ExperienceInput input);

  Future<Experience> update(String id, ExperienceInput input);

  Future<void> delete(String id);
}