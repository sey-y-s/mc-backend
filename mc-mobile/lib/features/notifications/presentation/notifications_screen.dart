import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_models.dart';
import 'package:mlc_mobile/features/notifications/presentation/notification_providers.dart';

class NotificationsScreen extends ConsumerWidget {
  const NotificationsScreen({super.key});
  IconData _icon(NotificationType type) => switch (type) {
    NotificationType.miseEnRelation => AppIcons.relations,
    NotificationType.validation => AppIcons.validations,
    NotificationType.opportunite => AppIcons.opportunites,
    NotificationType.besoinCompetence => AppIcons.competences,
    NotificationType.systeme => AppIcons.passport,
  };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final count = ref.watch(unreadNotificationCountProvider).value ?? 0;
    final revision = ref.watch(notificationsRevisionProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Alertes'),
        actions: [
          if (count > 0)
            TextButton.icon(
              onPressed: () async {
                final ok = await ref
                    .read(notificationsControllerProvider.notifier)
                    .markAllAsRead();
                if (!context.mounted) return;
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text(
                      ok
                          ? 'Toutes les notifications sont lues.'
                          : 'Impossible de mettre à jour les notifications.',
                    ),
                  ),
                );
              },
              icon: const Icon(AppIcons.markAllRead, size: 18),
              label: const Text('Tout lire'),
            ),
        ],
      ),
      body: PaginatedListView<AppNotification>(
        key: ValueKey(revision),
        pageSize: 20,
        emptyMessage: 'Aucune notification pour le moment.',
        fetchPage: (page, size) => ref
            .read(notificationRepositoryProvider)
            .list(page: page, size: size),
        itemBuilder: (context, item) {
          final unread = !item.isRead;
          return AppCard(
            accentColor: unread ? AppColors.gold : null,
            onTap: () => context.push('/notifications/${item.id}'),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 42,
                  height: 42,
                  decoration: BoxDecoration(
                    color: unread ? AppColors.goldSoft : AppColors.greenSoft,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    _icon(item.type),
                    color: unread ? AppColors.goldDeep : AppColors.green,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              item.title,
                              style: TextStyle(
                                fontWeight: unread
                                    ? FontWeight.w700
                                    : FontWeight.w500,
                              ),
                            ),
                          ),
                          if (unread)
                            const Icon(
                              AppIcons.unreadDot,
                              size: 10,
                              color: AppColors.goldDeep,
                            ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        item.message,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          color: AppColors.muted,
                          height: 1.3,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              formatDateFr(item.receivedAt),
                              style: const TextStyle(
                                fontSize: 11,
                                color: AppColors.muted,
                              ),
                            ),
                          ),
                          StatusBadge(
                            label: item.type.label,
                            tone: unread ? BadgeTone.accent : BadgeTone.neutral,
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
