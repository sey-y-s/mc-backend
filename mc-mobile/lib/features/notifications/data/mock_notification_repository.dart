import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_models.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_repository.dart';

class MockNotificationRepository implements NotificationRepository {
  const MockNotificationRepository();

  static final List<AppNotification> _items = _seed();
  static bool simulateError = false;

  static List<AppNotification> _seed() {
    final now = DateTime.now();
    return [
      AppNotification(
        id: 'nd-1',
        notificationId: 'n-1',
        title: 'Votre compétence a été validée',
        message:
            'Votre compétence en soudure à l’arc a été validée par un centre.',
        type: NotificationType.validation,
        receivedAt: now.subtract(const Duration(hours: 2)),
        createdAt: now.subtract(const Duration(hours: 2)),
        isRead: false,
      ),
      AppNotification(
        id: 'nd-2',
        notificationId: 'n-2',
        title: 'Nouvelle opportunité à Bamako',
        message: 'Une formation en énergie solaire vient d’être publiée.',
        type: NotificationType.opportunite,
        receivedAt: now.subtract(const Duration(days: 1)),
        createdAt: now.subtract(const Duration(days: 1)),
        isRead: false,
      ),
      AppNotification(
        id: 'nd-3',
        notificationId: 'n-3',
        title: 'Demande de mise en relation',
        message:
            'Une organisation souhaite échanger au sujet de vos compétences.',
        type: NotificationType.miseEnRelation,
        receivedAt: now.subtract(const Duration(days: 3)),
        createdAt: now.subtract(const Duration(days: 3)),
        isRead: true,
        readAt: now.subtract(const Duration(days: 2)),
      ),
      AppNotification(
        id: 'nd-4',
        notificationId: 'n-4',
        title: 'Bienvenue sur MaliCompétences',
        message: 'Complétez votre passeport pour présenter vos compétences.',
        type: NotificationType.systeme,
        receivedAt: now.subtract(const Duration(days: 5)),
        createdAt: now.subtract(const Duration(days: 5)),
        isRead: true,
        readAt: now.subtract(const Duration(days: 5)),
      ),
    ];
  }

  static void resetForTests() {
    _items
      ..clear()
      ..addAll(_seed());
    simulateError = false;
  }

  Future<void> _wait() async {
    await Future<void>.delayed(const Duration(milliseconds: 380));
    if (simulateError) throw const NetworkFailure();
  }

  List<AppNotification> _page(List<AppNotification> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  @override
  Future<List<AppNotification>> list({int page = 0, int size = 20}) async {
    await _wait();
    final sorted = [..._items]
      ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return _page(sorted, page, size);
  }

  @override
  Future<AppNotification> get(String id) async {
    await _wait();
    if (id == 'network-error') throw const NetworkFailure();
    return _items.firstWhere(
      (e) => e.id == id,
      orElse: () => throw const NotFoundFailure(),
    );
  }

  @override
  Future<int> unreadCount() async {
    await _wait();
    return _items.where((e) => !e.isRead).length;
  }

  @override
  Future<AppNotification> markRead(String id) async {
    await _wait();
    final i = _items.indexWhere((e) => e.id == id);
    if (i < 0) throw const NotFoundFailure();
    final updated = _items[i].copyWith(isRead: true, readAt: DateTime.now());
    _items[i] = updated;
    return updated;
  }

  @override
  Future<void> markAllRead() async {
    await _wait();
    final now = DateTime.now();
    for (var i = 0; i < _items.length; i++) {
      if (!_items[i].isRead) {
        _items[i] = _items[i].copyWith(isRead: true, readAt: now);
      }
    }
  }
}
