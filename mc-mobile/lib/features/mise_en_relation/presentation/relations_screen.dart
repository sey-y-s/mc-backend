import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_models.dart';
import 'package:mlc_mobile/features/mise_en_relation/presentation/mise_en_relation_providers.dart';

class RelationsScreen extends StatelessWidget {
  const RelationsScreen({super.key});
  @override
  Widget build(BuildContext context) => DefaultTabController(
    length: 2,
    child: Scaffold(
      appBar: AppBar(
        title: const Text('Mises en relation'),
        bottom: const TabBar(
          tabs: [
            Tab(text: 'Reçues'),
            Tab(text: 'Envoyées'),
          ],
        ),
      ),
      body: const TabBarView(
        children: [
          _RelationList(received: true),
          _RelationList(received: false),
        ],
      ),
    ),
  );
}

class _RelationList extends ConsumerWidget {
  const _RelationList({required this.received});
  final bool received;
  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      PaginatedListView<MiseEnRelation>(
        key: ValueKey<String>(
          received.toString() + ref.watch(relationsRevisionProvider).toString(),
        ),
        pageSize: 20,
        emptyMessage: received
            ? 'Aucune demande reçue.'
            : 'Aucune demande envoyée.',
        fetchPage: (page, size) {
          final repo = ref.read(miseEnRelationRepositoryProvider);
          return received
              ? repo.listReceived(page: page, size: size)
              : repo.listSent(page: page, size: size);
        },
        itemBuilder: (context, relation) {
          final acceptedName = relation.status == RelationStatus.acceptee
              ? (received ? relation.senderName : relation.recipientName)
              : null;
          final label = acceptedName?.isNotEmpty == true
              ? acceptedName!
              : 'Profil anonymisé';
          return AppCard(
            onTap: () => context.push('/relations/${relation.id}'),
            child: Row(
              children: [
                CircleAvatar(
                  backgroundColor: AppColors.greenSoft,
                  child: Icon(
                    relation.status == RelationStatus.acceptee
                        ? AppIcons.person
                        : AppIcons.lock,
                    color: AppColors.green,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        label,
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        relation.message,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          color: AppColors.muted,
                          fontSize: 12,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        formatDateFr(relation.requestedAt),
                        style: const TextStyle(
                          color: AppColors.muted,
                          fontSize: 11,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                StatusBadge(
                  label: relation.status.label,
                  tone: relation.status == RelationStatus.acceptee
                      ? BadgeTone.success
                      : relation.status == RelationStatus.refusee
                      ? BadgeTone.neutral
                      : BadgeTone.accent,
                ),
              ],
            ),
          );
        },
      );
}
