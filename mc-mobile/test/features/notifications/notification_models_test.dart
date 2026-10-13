import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_models.dart';

void main() {
  group('NotificationType', () {
    test('parses backend enum values and uses a safe fallback', () {
      expect(
        NotificationType.fromApi('MISE_EN_RELATION'),
        NotificationType.miseEnRelation,
      );
      expect(
        NotificationType.fromApi('VALIDATION'),
        NotificationType.validation,
      );
      expect(
        NotificationType.fromApi('OPPORTUNITE'),
        NotificationType.opportunite,
      );
      expect(NotificationType.fromApi('SYSTEME'), NotificationType.systeme);
      expect(NotificationType.fromApi('UNKNOWN'), NotificationType.systeme);
    });
  });

  group('AppNotification', () {
    test('maps recipient link separately from notification id', () {
      final item = AppNotification.fromJson({
        'id': 'notification-1',
        'destinataireId': 'recipient-link-1',
        'titre': 'Demande reçue',
        'message': 'Une demande vous attend.',
        'type': 'MISE_EN_RELATION',
        'lu': false,
        'dateReception': '2026-05-10T10:00:00.000Z',
        'dateCreation': '2026-05-10T09:55:00.000Z',
        'referenceId': 'relation-1',
      });

      expect(item.id, 'recipient-link-1');
      expect(item.notificationId, 'notification-1');
      expect(item.title, 'Demande reçue');
      expect(item.isRead, isFalse);
      expect(item.referenceId, 'relation-1');
    });

    test('copyWith marks a notification as read', () {
      final item = AppNotification(
        id: 'recipient-link-1',
        notificationId: 'notification-1',
        title: 'Titre',
        message: 'Message',
        type: NotificationType.systeme,
        receivedAt: DateTime(2026, 5, 10),
        createdAt: DateTime(2026, 5, 10),
        isRead: false,
      );

      final read = item.copyWith(isRead: true, readAt: DateTime(2026, 5, 11));
      expect(read.isRead, isTrue);
      expect(read.readAt, DateTime(2026, 5, 11));
    });
  });
}
