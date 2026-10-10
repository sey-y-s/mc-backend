import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/presentation/test_numerique_providers.dart';

class TestsListScreen extends ConsumerWidget {
  const TestsListScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    appBar: AppBar(title: const Text('Tests numériques')),
    body: Column(
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(20, 12, 20, 0),
          child: Text(
            'Ces tests peuvent générer une preuve de vos connaissances. Ils ne constituent pas une certification officielle.',
            style: TextStyle(color: AppColors.muted),
          ),
        ),
        Expanded(
          child: PaginatedListView<TestNumerique>(
            pageSize: 20,
            emptyMessage:
                'Aucun test numérique disponible pour vos compétences.',
            fetchPage: (page, size) => ref
                .read(testNumeriqueRepositoryProvider)
                .list(page: page, size: size),
            itemBuilder: (context, item) => AppCard(
              onTap: () => context.push('/tests/${item.id}'),
              child: Row(
                children: [
                  const Icon(AppIcons.tests, color: AppColors.green, size: 26),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          item.title,
                          style: const TextStyle(fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 3),
                        Text(
                          item.competenceName,
                          style: const TextStyle(
                            color: AppColors.muted,
                            fontSize: 12,
                          ),
                        ),
                        if (item.lastResult != null) ...[
                          const SizedBox(height: 8),
                          StatusBadge(
                            label:
                                'Dernier score : ${item.lastResult!.score.toStringAsFixed(0)}%',
                            tone: item.lastResult!.passed
                                ? BadgeTone.success
                                : BadgeTone.neutral,
                          ),
                        ],
                      ],
                    ),
                  ),
                  if (item.durationMinutes != null)
                    Text('${item.durationMinutes} min'),
                  const Icon(AppIcons.chevron, color: AppColors.muted),
                ],
              ),
            ),
          ),
        ),
      ],
    ),
  );
}
