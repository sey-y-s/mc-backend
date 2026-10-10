import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/notifications/data/mock_notification_repository.dart';

void main() {
  late MockNotificationRepository repository;
  setUp(() {
    MockNotificationRepository.resetForTests();
    repository = const MockNotificationRepository();
  });

  group('MockNotificationRepository', () {
    test('returns a date-sorted page', () async {
      final items = await repository.list(page: 0, size: 3);
      expect(items, hasLength(3));
      expect(
        items[0].createdAt.isAfter(items[1].createdAt) ||
            items[0].createdAt.isAtSameMomentAs(items[1].createdAt),
        isTrue,
      );
    });

    test('marks one or all notifications as read', () async {
      final before = await repository.unreadCount();
      await repository.markRead('nd-1');
      expect((await repository.get('nd-1')).isRead, isTrue);
      expect(await repository.unreadCount(), before - 1);
      await repository.markAllRead();
      expect(await repository.unreadCount(), 0);
    });

    test('rejects an unknown identifier', () {
      expect(() => repository.get('missing'), throwsA(isA<NotFoundFailure>()));
    });

    test('surfaces simulated network errors', () async {
      MockNotificationRepository.simulateError = true;
      expect(() => repository.list(), throwsA(isA<NetworkFailure>()));
    });
  });
}
