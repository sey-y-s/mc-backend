import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/domain/competence_ref.dart';
import 'package:mlc_mobile/features/competences/domain/competences_repository.dart';

/// Les endpoints de citoyen utilisent l'UUID `sub` du JWT.
class ApiCompetencesRepository implements CompetencesRepository {
  ApiCompetencesRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  Future<String> get _citoyenId => requireCurrentUserId(_storage);

  @override
  Future<List<CitoyenCompetence>> list({int page = 0, int size = 20}) async {
    final base = ApiEndpoints.citoyenCompetences(await _citoyenId);
    final res = await guardDio(
      () => _dio.get<dynamic>(
        base,
        queryParameters: {'page': page, 'size': size},
      ),
    );
    return pageItems(res.data)
        .map((e) => CitoyenCompetence.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  @override
  Future<CitoyenCompetence> get(String id) async {
    final items = await list(size: 1000);
    for (final item in items) {
      if (item.id == id) return item;
    }
    throw const NotFoundFailure();
  }

  @override
  Future<CitoyenCompetence> add({
    required String competenceId,
    required Niveau niveau,
  }) async {
    final base = ApiEndpoints.citoyenCompetences(await _citoyenId);
    final res = await guardDio(
      () => _dio.post<Map<String, dynamic>>(
        base,
        data: {
          'competenceId': competenceId,
          'niveau': niveau.name.toUpperCase(),
        },
      ),
    );
    return CitoyenCompetence.fromJson(res.data!);
  }

  @override
  Future<List<CompetenceRef>> searchReferentiel(String query) async {
    final res = await guardDio(
      () => _dio.get<dynamic>(ApiEndpoints.competences),
    );
    final normalizedQuery = query.trim().toLowerCase();
    return pageItems(res.data)
        .map((e) => CompetenceRef.fromJson(e as Map<String, dynamic>))
        .where(
          (competence) =>
              normalizedQuery.isEmpty ||
              competence.nom.toLowerCase().contains(normalizedQuery),
        )
        .take(20)
        .toList();
  }
}
