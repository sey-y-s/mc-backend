import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_models.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_repository.dart';

class ApiNotificationRepository implements NotificationRepository {
  const ApiNotificationRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  Future<List<AppNotification>> _load({required bool unreadOnly}) async {
    final userId = await requireCurrentUserId(_storage);
    final path = unreadOnly
        ? ApiEndpoints.mobileUnreadNotifications(userId)
        : ApiEndpoints.mobileUserNotifications(userId);
    final response = await guardDio(() => _dio.get<dynamic>(path));
    return pageItems(response.data)
        .whereType<Map>()
        .map(
          (item) => AppNotification.fromJson(Map<String, dynamic>.from(item)),
        )
        .toList()
      ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
  }

  List<AppNotification> _page(List<AppNotification> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  @override
  Future<List<AppNotification>> list({int page = 0, int size = 20}) async =>
      _page(await _load(unreadOnly: false), page, size);

  @override
  Future<AppNotification> get(String id) async {
    final items = (await _load(unreadOnly: false))
        .where((e) => e.id == id)
        .toList();
    final item = items.isEmpty ? null : items.first;
    if (item == null) throw const NotFoundFailure();
    return item;
  }

  @override
  Future<int> unreadCount() async => (await _load(unreadOnly: true)).length;

  @override
  Future<AppNotification> markRead(String id) async {
    final response = await guardDio(
      () => _dio.patch<Map<String, dynamic>>(
        ApiEndpoints.mobileMarkNotificationRead(id),
      ),
    );
    return AppNotification.fromJson(response.data!);
  }

  @override
  Future<void> markAllRead() async {
    final unread = await _load(unreadOnly: true);
    for (final item in unread) {
      await markRead(item.id);
    }
  }
}
