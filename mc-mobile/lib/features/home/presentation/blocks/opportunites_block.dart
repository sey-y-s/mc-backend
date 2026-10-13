import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';
import 'package:mlc_mobile/features/opportunites/presentation/opportunite_providers.dart';

/// Bloc « Opportunités récentes » pour la page d'accueil.
class OpportunitesBlock extends ConsumerWidget {
  const OpportunitesBlock({super.key});

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
    final oppsAsync = ref.watch(recentOpportunitesProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(
          title: 'Opportunités récentes',
          actionLabel: 'Voir tout',
          onAction: () => context.push('/opportunites'),
        ),
        oppsAsync.when(
          loading: () => const Padding(
            padding: EdgeInsets.symmetric(vertical: 16),
            child: Center(child: CircularProgressIndicator()),
          ),
          error: (e, _) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    failureMessage(e),
                    style: const TextStyle(
                      color: AppColors.error,
                      fontSize: 13,
                    ),
                  ),
                ),
                TextButton(
                  onPressed: () => ref.invalidate(recentOpportunitesProvider),
                  child: const Text('Réessayer'),
                ),
              ],
            ),
          ),
          data: (items) {
            if (items.isEmpty) {
              return AppCard(
                child: Row(
                  children: [
                    const Icon(
                      AppIcons.opportunites,
                      color: AppColors.muted,
                      size: 20,
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        'Aucune opportunité récente disponible.',
                        style: Theme.of(context).textTheme.bodyMedium
                            ?.copyWith(color: AppColors.muted),
                      ),
                    ),
                  ],
                ),
              );
            }

            return Column(
              children: [
                for (final opp in items) ...[
                  AppCard(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 12,
                    ),
                    onTap: () => context.push('/opportunites/${opp.id}'),
                    child: Row(
                      children: [
                        Container(
                          width: 38,
                          height: 38,
                          decoration: BoxDecoration(
                            color: AppColors.goldSoft,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: const Icon(
                            AppIcons.opportunites,
                            size: 20,
                            color: AppColors.goldDeep,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  StatusBadge(
                                    label: opp.type.label,
                                    tone: _toneForType(opp.type),
                                  ),
                                  const Spacer(),
                                  if (opp.expirationDate != null)
                                    Text(
                                      'Fin : ${formatDateFr(opp.expirationDate!)}',
                                      style: const TextStyle(
                                        fontSize: 10,
                                        color: AppColors.muted,
                                      ),
                                    ),
                                ],
                              ),
                              const SizedBox(height: 4),
                              Text(
                                opp.title,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.w600,
                                  color: AppColors.anthracite,
                                ),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 8),
                        const Icon(
                          AppIcons.chevron,
                          size: 16,
                          color: AppColors.muted,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
              ],
            );
          },
        ),
      ],
    );
  }
}
