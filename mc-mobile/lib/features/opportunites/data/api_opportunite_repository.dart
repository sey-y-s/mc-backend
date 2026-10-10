import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_repository.dart';

class ApiOpportuniteRepository implements OpportuniteRepository {
  const ApiOpportuniteRepository(this._dio);
  final Dio _dio;

  Future<List<Opportunite>> _all() async {
    final response = await guardDio(
      () => _dio.get<dynamic>(ApiEndpoints.mobileOpportunities),
    );
    return pageItems(response.data)
        .whereType<Map>()
        .map((item) => Opportunite.fromJson(Map<String, dynamic>.from(item)))
        .where(
          (item) => item.isVisible && item.type != OpportuniteType.insertion,
        )
        .toList()
      ..sort((a, b) => b.publishedAt.compareTo(a.publishedAt));
  }

  List<Opportunite> _page(List<Opportunite> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  @override
  Future<List<Opportunite>> list({
    OpportuniteType? type,
    String? categoryId,
    int page = 0,
    int size = 20,
  }) async {
    final filtered = (await _all())
        .where(
          (item) =>
              (type == null || item.type == type) &&
              (categoryId == null || item.categoryId == categoryId),
        )
        .toList();
    return _page(filtered, page, size);
  }

  @override
  Future<Opportunite> get(String id) async {
    final response = await guardDio(
      () => _dio.get<Map<String, dynamic>>(ApiEndpoints.mobileOpportunity(id)),
    );
    final item = Opportunite.fromJson(response.data!);
    if (!item.isVisible || item.type == OpportuniteType.insertion) {
      throw const NotFoundFailure();
    }
    return item;
  }
}
