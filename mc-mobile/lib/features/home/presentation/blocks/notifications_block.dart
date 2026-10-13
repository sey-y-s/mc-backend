import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_models.dart';
import 'package:mlc_mobile/features/notifications/presentation/notification_providers.dart';

class NotificationsBlock extends ConsumerWidget {
  const NotificationsBlock({super.key});
  IconData _icon(NotificationType type) => switch (type) {
    NotificationType.miseEnRelation => AppIcons.relations,
    NotificationType.validation => AppIcons.validations,
    NotificationType.opportunite => AppIcons.opportunites,
    NotificationType.besoinCompetence => AppIcons.competences,
    NotificationType.systeme => AppIcons.passport,
  };
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final async = ref.watch(recentNotificationsProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(
          title: 'Dernières notifications',
          actionLabel: 'Voir tout',
          onAction: () => context.push('/notifications'),
        ),
        async.when(
          loading: () => const Padding(
            padding: EdgeInsets.all(16),
            child: Center(child: CircularProgressIndicator()),
          ),
          error: (e, _) => Row(
            children: [
              Expanded(
                child: Text(
                  failureMessage(e),
                  style: const TextStyle(color: AppColors.error),
                ),
              ),
              TextButton(
                onPressed: () => ref.invalidate(recentNotificationsProvider),
                child: const Text('Réessayer'),
              ),
            ],
          ),
          data: (items) => items.isEmpty
              ? const AppCard(child: Text('Aucune notification récente.'))
              : Column(
                  children: [
                    for (final item in items)
                      AppCard(
                        padding: const EdgeInsets.all(12),
                        accentColor: item.isRead ? null : AppColors.gold,
                        onTap: () => context.push('/notifications/${item.id}'),
                        child: Row(
                          children: [
                            Icon(_icon(item.type), color: AppColors.green),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    item.title,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: TextStyle(
                                      fontWeight: item.isRead
                                          ? FontWeight.w500
                                          : FontWeight.bold,
                                    ),
                                  ),
                                  Text(
                                    formatDateFr(item.receivedAt),
                                    style: const TextStyle(
                                      color: AppColors.muted,
                                      fontSize: 11,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            if (!item.isRead)
                              const Icon(
                                AppIcons.unreadDot,
                                size: 10,
                                color: AppColors.goldDeep,
                              ),
                          ],
                        ),
                      ),
                  ],
                ),
        ),
      ],
    );
  }
}
