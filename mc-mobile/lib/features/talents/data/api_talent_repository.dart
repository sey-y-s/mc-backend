import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/features/talents/domain/talent_models.dart';
import 'package:mlc_mobile/features/talents/domain/talent_repository.dart';

class ApiTalentRepository implements TalentRepository {
  const ApiTalentRepository(this._dio);
  final Dio _dio;

  @override
  Future<List<TalentSummary>> search(
    TalentFilters filters, {
    int page = 0,
    int size = 20,
  }) async {
    final response = await guardDio(
      () => _dio.get<dynamic>(
        ApiEndpoints.mobileTalentSearch,
        queryParameters: {
          'q': filters.query,
          if (filters.competenceId != null)
            'competenceId': filters.competenceId,
          if (filters.metierId != null) 'metierId': filters.metierId,
          if (filters.regionId != null) 'regionId': filters.regionId,
          if (filters.communeId != null) 'communeId': filters.communeId,
          if (filters.availability != null)
            'disponibilite': filters.availability!.apiCode,
          if (filters.minimumLevel != null)
            'niveauMin': filters.minimumLevel!.name.toUpperCase(),
          'page': page,
          'size': size,
        },
      ),
    );
    // Whitelist-only model parsing: accidental backend PII fields are never kept by the client.
    return pageItems(response.data)
        .whereType<Map>()
        .map((item) => TalentSummary.fromJson(Map<String, dynamic>.from(item)))
        .toList();
  }

  @override
  Future<TalentProfileAnonymized> getAnonymized(String id) async {
    final response = await guardDio(
      () => _dio.get<Map<String, dynamic>>(
        ApiEndpoints.mobileAnonymizedTalent(id),
      ),
    );
    return TalentProfileAnonymized.fromJson(response.data!);
  }
}
