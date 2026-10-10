import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/network/upload.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_repository.dart';

class ApiPortfolioRepository implements PortfolioRepository {
  const ApiPortfolioRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  Future<String> get _userId => requireCurrentUserId(_storage);
  List<T> _page<T>(List<T> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    return start >= items.length
        ? const []
        : items.skip(start).take(size).toList();
  }

  Map<String, dynamic> _input(PortfolioInput input, String citizenId) => {
    'citoyenId': citizenId,
    'titre': input.title.trim(),
    'description': input.description.trim(),
    if (input.date != null)
      'dateRealisation': input.date!.toIso8601String().split('T').first,
    if (input.linkUrl != null) 'lienUrl': input.linkUrl,
  };

  @override
  Future<List<PortfolioRealisation>> list({int page = 0, int size = 20}) async {
    final uid = await _userId;
    final res = await guardDio(
      () => _dio.get<dynamic>(ApiEndpoints.mobilePortfolio(uid)),
    );
    final items =
        pageItems(res.data)
            .whereType<Map>()
            .map(
              (e) =>
                  PortfolioRealisation.fromJson(Map<String, dynamic>.from(e)),
            )
            .toList()
          ..sort(
            (a, b) => (b.date ?? DateTime(0)).compareTo(a.date ?? DateTime(0)),
          );
    return _page(items, page, size);
  }

  @override
  Future<PortfolioRealisation> get(String id) async {
    final uid = await _userId;
    final res = await guardDio(
      () => _dio.get<Map<String, dynamic>>(
        ApiEndpoints.mobilePortfolioItem(uid, id),
      ),
    );
    return PortfolioRealisation.fromJson(res.data!);
  }

  @override
  Future<PortfolioRealisation> create(PortfolioInput input) async {
    final uid = await _userId;
    final res = await guardDio(
      () => _dio.post<Map<String, dynamic>>(
        ApiEndpoints.mobilePortfolio(uid),
        data: _input(input, uid),
      ),
    );
    return PortfolioRealisation.fromJson(res.data!);
  }

  @override
  Future<PortfolioRealisation> update(String id, PortfolioInput input) async {
    final uid = await _userId;
    final res = await guardDio(
      () => _dio.put<Map<String, dynamic>>(
        ApiEndpoints.mobilePortfolioItem(uid, id),
        data: _input(input, uid),
      ),
    );
    return PortfolioRealisation.fromJson(res.data!);
  }

  @override
  Future<void> delete(String id) async {
    final uid = await _userId;
    await guardDio(
      () => _dio.delete<void>(ApiEndpoints.mobilePortfolioItem(uid, id)),
    );
  }

  @override
  Future<PortfolioMedia> addMedia(
    String portfolioId,
    PickedMedia file, {
    void Function(double progress)? onProgress,
  }) async {
    final type = switch (file.kind) {
      MediaKind.image => PortfolioMediaType.image,
      MediaKind.video => PortfolioMediaType.video,
      MediaKind.document => PortfolioMediaType.document,
    };
    final res = await guardDio(
      () => uploadMultipart<Map<String, dynamic>>(
        _dio,
        ApiEndpoints.mobilePortfolioMediaUpload(portfolioId),
        file: file,
        fields: {'type': type.apiCode, 'legende': file.name},
        onProgress: onProgress,
      ),
    );
    return PortfolioMedia.fromJson(res.data!, portfolioId: portfolioId);
  }

  @override
  Future<void> removeMedia(String portfolioId, String mediaId) async {
    await guardDio(
      () => _dio.delete<void>(
        ApiEndpoints.mobilePortfolioMedia(portfolioId, mediaId),
      ),
    );
  }
}
