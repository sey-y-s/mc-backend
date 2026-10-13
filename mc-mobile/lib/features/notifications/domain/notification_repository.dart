import 'package:mlc_mobile/features/notifications/domain/notification_models.dart';

abstract interface class NotificationRepository {
  Future<List<AppNotification>> list({int page = 0, int size = 20});
  Future<AppNotification> get(String id);
  Future<int> unreadCount();
  Future<AppNotification> markRead(String id);
  Future<void> markAllRead();
}
