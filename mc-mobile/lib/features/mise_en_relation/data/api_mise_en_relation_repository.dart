import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_models.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_repository.dart';

class ApiMiseEnRelationRepository implements MiseEnRelationRepository {
  const ApiMiseEnRelationRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  Future<List<MiseEnRelation>> _all() async {
    final response = await guardDio(
      () => _dio.get<dynamic>(ApiEndpoints.mobileRelations),
    );
    return pageItems(response.data)
        .whereType<Map>()
        .map((item) => MiseEnRelation.fromJson(Map<String, dynamic>.from(item)))
        .toList()
      ..sort((a, b) => b.requestedAt.compareTo(a.requestedAt));
  }

  List<MiseEnRelation> _page(List<MiseEnRelation> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  @override
  Future<List<MiseEnRelation>> listReceived({
    int page = 0,
    int size = 20,
  }) async {
    final userId = await requireCurrentUserId(_storage);
    return _page(
      (await _all()).where((e) => e.recipientId == userId).toList(),
      page,
      size,
    );
  }

  @override
  Future<List<MiseEnRelation>> listSent({int page = 0, int size = 20}) async {
    final userId = await requireCurrentUserId(_storage);
    return _page(
      (await _all()).where((e) => e.senderId == userId).toList(),
      page,
      size,
    );
  }

  @override
  Future<MiseEnRelation> get(String id) async {
    final response = await guardDio(
      () => _dio.get<Map<String, dynamic>>(ApiEndpoints.mobileRelation(id)),
    );
    return MiseEnRelation.fromJson(response.data!);
  }

  @override
  Future<MiseEnRelation> send(String talentId, {String? message}) async {
    final userId = await requireCurrentUserId(_storage);
    final text = message?.trim().isNotEmpty == true
        ? message!.trim()
        : 'Je souhaite échanger au sujet de vos compétences.';
    final response = await guardDio(
      () => _dio.post<Map<String, dynamic>>(
        ApiEndpoints.mobileRelations,
        data: {
          'demandeurId': userId,
          'destinataireId': talentId,
          'statut': RelationStatus.enAttente.apiCode,
          'message': text,
        },
      ),
    );
    return MiseEnRelation.fromJson(response.data!);
  }

  @override
  Future<MiseEnRelation> respond(String id, {required bool accept}) async {
    final userId = await requireCurrentUserId(_storage);
    final request = await get(id);
    if (request.recipientId != userId) throw const ForbiddenFailure();
    if (request.status != RelationStatus.enAttente) {
      throw ValidationFailure('Cette demande a déjà reçu une réponse.');
    }
    final response = await guardDio(
      () => _dio.put<Map<String, dynamic>>(
        ApiEndpoints.mobileRelation(id),
        data: {
          'demandeurId': request.senderId,
          'destinataireId': request.recipientId,
          'SuiviBesoinTalentId': request.followUpId,
          'statut': (accept ? RelationStatus.acceptee : RelationStatus.refusee)
              .apiCode,
          'message': request.message,
        },
      ),
    );
    return MiseEnRelation.fromJson(response.data!);
  }
}
