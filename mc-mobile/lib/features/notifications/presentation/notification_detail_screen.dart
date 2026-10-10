import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/info_row.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/notifications/domain/notification_models.dart';
import 'package:mlc_mobile/features/notifications/presentation/notification_providers.dart';

class NotificationDetailScreen extends ConsumerStatefulWidget {
  const NotificationDetailScreen({super.key, this.id});
  final String? id;
  @override
  ConsumerState<NotificationDetailScreen> createState() =>
      _NotificationDetailScreenState();
}

class _NotificationDetailScreenState
    extends ConsumerState<NotificationDetailScreen> {
  bool _marked = false;
  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final id = widget.id;
    if (!_marked && id != null && id.isNotEmpty) {
      _marked = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) {
          ref.read(notificationsControllerProvider.notifier).markAsRead(id);
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final id = widget.id;
    if (id == null || id.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(notificationDetailProvider(id));
    return Scaffold(
      appBar: AppBar(title: const Text('Notification')),
      body: AsyncValueView<AppNotification>(
        value: value,
        onRetry: () => ref.invalidate(notificationDetailProvider(id)),
        data: (item) {
          final action = switch (item.type) {
            NotificationType.miseEnRelation => AppButton(
              label: 'Voir les mises en relation',
              icon: AppIcons.relations,
              onPressed: () => context.push(
                item.referenceId == null
                    ? '/relations'
                    : '/relations/${item.referenceId!}',
              ),
            ),
            NotificationType.validation => AppButton(
              label: 'Voir les validations',
              icon: AppIcons.validations,
              onPressed: () => context.push(
                item.referenceId == null
                    ? '/validations'
                    : '/validations/${item.referenceId!}',
              ),
            ),
            NotificationType.opportunite => AppButton(
              label: 'Voir les opportunités',
              icon: AppIcons.opportunites,
              onPressed: () => context.push(
                item.referenceId == null
                    ? '/opportunites'
                    : '/opportunites/${item.referenceId!}',
              ),
            ),
            NotificationType.besoinCompetence => AppButton(
              label: 'Ouvrir mon passeport',
              icon: AppIcons.passport,
              onPressed: () => context.push('/passeport'),
            ),
            NotificationType.systeme => null,
          };
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Text(item.title, style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 8),
              StatusBadge(
                label: item.type.label,
                tone: item.isRead ? BadgeTone.neutral : BadgeTone.accent,
              ),
              const SizedBox(height: 20),
              AppCard(
                child: Text(
                  item.message,
                  style: Theme.of(context).textTheme.bodyLarge
                      ?.copyWith(height: 1.5, color: AppColors.anthracite),
                ),
              ),
              const SizedBox(height: 16),
              AppCard(
                child: Column(
                  children: [
                    InfoRow(
                      icon: AppIcons.calendar,
                      label: 'Reçue le',
                      value: formatDateFr(item.receivedAt),
                    ),
                    if (item.readAt != null)
                      InfoRow(
                        icon: AppIcons.selected,
                        label: 'Lue le',
                        value: formatDateFr(item.readAt!),
                      ),
                  ],
                ),
              ),
              if (action != null) ...[const SizedBox(height: 20), action],
            ],
          );
        },
      ),
    );
  }
}
