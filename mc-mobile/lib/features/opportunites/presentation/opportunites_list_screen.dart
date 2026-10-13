import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';
import 'package:mlc_mobile/features/opportunites/presentation/opportunite_providers.dart';

/// Liste paginée des opportunités en lecture seule, filtrable par catégorie.
class OpportunitesListScreen extends ConsumerWidget {
  const OpportunitesListScreen({super.key});

  static const _categories = <_CategoryOption>[
    _CategoryOption(
      type: null,
      label: 'Toutes',
      icon: AppIcons.opportunityCategory,
    ),
    _CategoryOption(
      type: OpportuniteType.formationGratuite,
      label: 'Formations',
      icon: AppIcons.training,
    ),
    _CategoryOption(
      type: OpportuniteType.bourse,
      label: 'Bourses',
      icon: AppIcons.scholarship,
    ),
    _CategoryOption(
      type: OpportuniteType.programme,
      label: 'Programmes',
      icon: AppIcons.program,
    ),
    _CategoryOption(
      type: OpportuniteType.appelCandidature,
      label: 'Appels à candidatures',
      icon: AppIcons.callForApplications,
    ),
    _CategoryOption(
      type: OpportuniteType.concours,
      label: 'Concours',
      icon: AppIcons.competition,
    ),
    _CategoryOption(
      type: OpportuniteType.accompagnement,
      label: 'Accompagnement',
      icon: AppIcons.support,
    ),
    _CategoryOption(
      type: OpportuniteType.autre,
      label: 'Autres',
      icon: AppIcons.more,
    ),
  ];

  Future<void> _chooseCategory(
    BuildContext context,
    WidgetRef ref,
    OpportuniteType? selected,
  ) async {
    final choice = await showModalBottomSheet<_CategoryOption>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) =>
          _CategorySheet(categories: _categories, selected: selected),
    );
    if (choice != null) {
      ref.read(opportuniteTypeFilterProvider.notifier).state = choice.type;
    }
  }

  BadgeTone _toneForType(OpportuniteType type) {
    return switch (type) {
      OpportuniteType.formationGratuite => BadgeTone.success,
      OpportuniteType.bourse => BadgeTone.accent,
      OpportuniteType.programme => BadgeTone.accent,
      OpportuniteType.concours => BadgeTone.success,
      _ => BadgeTone.neutral,
    };
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selectedType = ref.watch(opportuniteTypeFilterProvider);
    final selectedCategory = _categories.firstWhere(
      (category) => category.type == selectedType,
      orElse: () => _categories.first,
    );

    return Scaffold(
      appBar: AppBar(title: const Text('Opportunités')),
      body: Column(
        children: [
          Container(
            margin: const EdgeInsets.fromLTRB(16, 12, 16, 14),
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.greenDark, AppColors.green],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(22),
            ),
            child: Stack(
              children: [
                Positioned(
                  right: -6,
                  top: -10,
                  child: Icon(
                    AppIcons.opportunites,
                    size: 92,
                    color: const Color(0x17FFFFFF),
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'À SAISIR',
                      style: TextStyle(
                        color: AppColors.goldSoft,
                        fontSize: 11,
                        letterSpacing: 1.5,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Faites avancer vos projets.',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 21,
                        height: 1.15,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 7),
                    Text(
                      'Formations, bourses et programmes pour votre parcours.',
                      style: TextStyle(
                        color: const Color(0xD1FFFFFF),
                        fontSize: 13,
                        height: 1.35,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Material(
              color: AppColors.card,
              borderRadius: BorderRadius.circular(16),
              child: InkWell(
                borderRadius: BorderRadius.circular(16),
                onTap: () => _chooseCategory(context, ref, selectedType),
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 13,
                  ),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: Row(
                    children: [
                      Container(
                        width: 42,
                        height: 42,
                        decoration: BoxDecoration(
                          color: AppColors.greenSoft,
                          borderRadius: BorderRadius.circular(13),
                        ),
                        child: Icon(
                          selectedCategory.icon,
                          color: AppColors.green,
                          size: 22,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Explorer par catégorie',
                              style: TextStyle(
                                color: AppColors.muted,
                                fontSize: 11,
                              ),
                            ),
                            const SizedBox(height: 3),
                            Text(
                              selectedCategory.label,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: AppColors.anthracite,
                                fontWeight: FontWeight.w700,
                                fontSize: 15,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const Icon(AppIcons.expand, color: AppColors.green),
                    ],
                  ),
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(18, 18, 18, 2),
            child: Row(
              children: [
                const Expanded(
                  child: Text(
                    'À découvrir',
                    style: TextStyle(
                      color: AppColors.anthracite,
                      fontSize: 17,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                Text(
                  selectedType == null
                      ? 'Toutes les catégories'
                      : selectedCategory.label,
                  style: const TextStyle(color: AppColors.muted, fontSize: 12),
                ),
              ],
            ),
          ),
          Expanded(
            child: PaginatedListView<Opportunite>(
              key: ValueKey(selectedType),
              pageSize: 20,
              emptyMessage: 'Aucune opportunité disponible dans cette catégorie pour le moment.',
              fetchPage: (page, size) => ref
                  .read(opportuniteRepositoryProvider)
                  .list(type: selectedType, page: page, size: size),
              itemBuilder: (context, item) {
                final isUrgent =
                    item.expirationDate != null &&
                    item.expirationDate!.difference(DateTime.now()).inDays <= 5;

                return AppCard(
                  onTap: () => context.push('/opportunites/${item.id}'),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          StatusBadge(
                            label: item.type.label,
                            tone: _toneForType(item.type),
                          ),
                          if (item.categoryName != null) ...[
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                item.categoryName!,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 12,
                                  color: AppColors.muted,
                                ),
                              ),
                            ),
                          ],
                        ],
                      ),
                      const SizedBox(height: 10),
                      Text(
                        item.title,
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                          color: AppColors.anthracite,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        item.description,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontSize: 13,
                          color: AppColors.muted,
                          height: 1.3,
                        ),
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          if (item.expirationDate != null) ...[
                            Icon(
                              AppIcons.calendar,
                              size: 15,
                              color: isUrgent
                                  ? AppColors.error
                                  : AppColors.muted,
                            ),
                            const SizedBox(width: 6),
                            Text(
                              'Expire le ${formatDateFr(item.expirationDate!)}',
                              style: TextStyle(
                                fontSize: 12,
                                color: isUrgent
                                    ? AppColors.error
                                    : AppColors.muted,
                                fontWeight: isUrgent
                                    ? FontWeight.bold
                                    : FontWeight.normal,
                              ),
                            ),
                          ] else ...[
                            Text(
                              'Publié le ${formatDateFr(item.publishedAt)}',
                              style: const TextStyle(
                                fontSize: 12,
                                color: AppColors.muted,
                              ),
                            ),
                          ],
                          const Spacer(),
                          const Icon(
                            AppIcons.chevron,
                            size: 18,
                            color: AppColors.muted,
                          ),
                        ],
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _CategorySheet extends StatelessWidget {
  const _CategorySheet({required this.categories, required this.selected});

  final List<_CategoryOption> categories;
  final OpportuniteType? selected;

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 38,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.border,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ),
            const SizedBox(height: 20),
            const Text(
              'Choisir une catégorie',
              style: TextStyle(
                color: AppColors.anthracite,
                fontSize: 19,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 5),
            const Text(
              'Affinez la liste selon ce que vous recherchez.',
              style: TextStyle(color: AppColors.muted, fontSize: 13),
            ),
            const SizedBox(height: 18),
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              mainAxisSpacing: 10,
              crossAxisSpacing: 10,
              childAspectRatio: 2.5,
              children: [
                for (final category in categories)
                  _CategoryTile(
                    option: category,
                    selected: category.type == selected,
                    onTap: () => Navigator.of(context).pop(category),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _CategoryTile extends StatelessWidget {
  const _CategoryTile({
    required this.option,
    required this.selected,
    required this.onTap,
  });

  final _CategoryOption option;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final foreground = selected ? AppColors.greenDark : AppColors.anthracite;
    return Material(
      color: selected ? AppColors.greenSoft : AppColors.surface,
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(
              color: selected ? AppColors.green : AppColors.border,
              width: selected ? 1.5 : 1,
            ),
          ),
          child: Row(
            children: [
              Icon(option.icon, size: 20, color: AppColors.green),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  option.label,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    color: foreground,
                    fontSize: 12,
                    fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                  ),
                ),
              ),
              if (selected)
                const Icon(AppIcons.check, size: 16, color: AppColors.green),
            ],
          ),
        ),
      ),
    );
  }
}

class _CategoryOption {
  const _CategoryOption({
    required this.type,
    required this.label,
    required this.icon,
  });

  final OpportuniteType? type;
  final String label;
  final IconData icon;
}
