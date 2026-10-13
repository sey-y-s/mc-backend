import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_dialogs.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/info_row.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_models.dart';
import 'package:mlc_mobile/features/mise_en_relation/presentation/mise_en_relation_providers.dart';

class RelationDetailScreen extends ConsumerWidget {
  const RelationDetailScreen({super.key, this.id});
  final String? id;
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final relationId = id;
    if (relationId == null || relationId.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(relationDetailProvider(relationId));
    return Scaffold(
      appBar: AppBar(title: const Text('Demande de mise en relation')),
      body: AsyncValueView<MiseEnRelation>(
        value: value,
        onRetry: () => ref.invalidate(relationDetailProvider(relationId)),
        data: (item) {
          final accepted = item.status == RelationStatus.acceptee;
          final contact = accepted ? item.contact : null;
          final name = accepted
              ? (item.senderName ?? item.recipientName)
              : null;
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              AppCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Icon(AppIcons.lock, color: AppColors.green),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            name?.isNotEmpty == true
                                ? name!
                                : 'Profil anonymisé',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                        ),
                        StatusBadge(
                          label: item.status.label,
                          tone: accepted ? BadgeTone.success : BadgeTone.accent,
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Text(
                      item.message,
                      style: Theme.of(context).textTheme.bodyLarge
                          ?.copyWith(height: 1.5),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              AppCard(
                child: Column(
                  children: [
                    InfoRow(
                      icon: AppIcons.calendar,
                      label: 'Demande reçue',
                      value: formatDateFr(item.requestedAt),
                    ),
                    if (item.respondedAt != null)
                      InfoRow(
                        icon: AppIcons.selected,
                        label: 'Réponse',
                        value: formatDateFr(item.respondedAt!),
                      ),
                  ],
                ),
              ),
              if (accepted && contact != null) ...[
                const SizedBox(height: 16),
                AppCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Coordonnées transmises',
                        style: Theme.of(context).textTheme.titleSmall,
                      ),
                      if (contact.telephone?.isNotEmpty == true)
                        ListTile(
                          leading: const Icon(AppIcons.phone),
                          title: Text(contact.telephone!),
                        ),
                      if (contact.email?.isNotEmpty == true)
                        ListTile(
                          leading: const Icon(AppIcons.email),
                          title: Text(contact.email!),
                        ),
                      if (contact.isEmpty)
                        const Text('Aucune coordonnée fournie.'),
                    ],
                  ),
                ),
              ] else if (accepted)
                const Padding(
                  padding: EdgeInsets.only(top: 16),
                  child: AppCard(
                    child: Text(
                      'Demande acceptée, aucune coordonnée fournie par le serveur.',
                    ),
                  ),
                ),
              if (item.status == RelationStatus.enAttente) ...[
                const SizedBox(height: 24),
                AppButton(
                  label: 'Accepter la demande',
                  icon: AppIcons.check,
                  onPressed: () => _respond(context, ref, relationId, true),
                ),
                const SizedBox(height: 10),
                AppButton(
                  label: 'Refuser',
                  icon: AppIcons.close,
                  variant: AppButtonVariant.secondary,
                  onPressed: () => _respond(context, ref, relationId, false),
                ),
              ],
            ],
          );
        },
      ),
    );
  }

  Future<void> _respond(
    BuildContext context,
    WidgetRef ref,
    String id,
    bool accept,
  ) async {
    if (!accept) {
      final confirmed = await confirmDialog(
        context,
        title: 'Refuser cette demande ?',
        message: 'La demande sera refusée.',
        confirmLabel: 'Refuser',
        destructive: true,
      );
      if (!confirmed || !context.mounted) return;
    }
    try {
      await ref
          .read(miseEnRelationRepositoryProvider)
          .respond(id, accept: accept);
      ref.read(relationsRevisionProvider.notifier).state++;
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(accept ? 'Demande acceptée.' : 'Demande refusée.'),
          ),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(failureMessage(e))));
      }
    }
  }
}
