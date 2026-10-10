import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_repository.dart';

/// L'identifiant citoyen est le sujet UUID du JWT créé pour un compte citoyen.
class ApiExperienceRepository implements ExperienceRepository {
  ApiExperienceRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  Future<String> get _baseId => requireCurrentUserId(_storage);

  @override
  Future<List<Experience>> list({int page = 0, int size = 20}) async {
    final base = ApiEndpoints.citoyenExperiences(await _baseId);
    final res = await guardDio(
      () => _dio.get<dynamic>(
        base,
        queryParameters: {'page': page, 'size': size},
      ),
    );
    return pageItems(res.data)
        .map((e) => Experience.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  @override
  Future<Experience> get(String id) async {
    final base = ApiEndpoints.citoyenExperiences(await _baseId);
    final res = await guardDio(
      () => _dio.get<Map<String, dynamic>>('$base/$id'),
    );
    return Experience.fromJson(res.data!);
  }

  @override
  Future<Experience> create(ExperienceInput input) async {
    final citoyenId = await _baseId;
    final base = ApiEndpoints.citoyenExperiences(citoyenId);
    final res = await guardDio(
      () => _dio.post<Map<String, dynamic>>(
        base,
        data: {...input.toJson(), 'citoyenId': citoyenId},
      ),
    );
    return Experience.fromJson(res.data!);
  }

  @override
  Future<Experience> update(String id, ExperienceInput input) async {
    final citoyenId = await _baseId;
    final base = ApiEndpoints.citoyenExperiences(citoyenId);
    final res = await guardDio(
      () => _dio.put<Map<String, dynamic>>(
        '$base/$id',
        data: {...input.toJson(), 'citoyenId': citoyenId},
      ),
    );
    return Experience.fromJson(res.data!);
  }

  @override
  Future<void> delete(String id) async {
    final base = ApiEndpoints.citoyenExperiences(await _baseId);
    await guardDio(() => _dio.delete<void>('$base/$id'));
  }
}
