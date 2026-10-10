import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/presentation/test_numerique_providers.dart';

class TestResultScreen extends ConsumerWidget {
  const TestResultScreen({super.key, this.id, this.result});
  final String? id;
  final ResultatTest? result;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final testId = id;
    if (testId == null || testId.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final submittedResult = result;
    if (submittedResult != null && submittedResult.testId == testId) {
      return Scaffold(
        appBar: AppBar(title: const Text('Résultat du test')),
        body: _ResultContent(result: submittedResult),
      );
    }
    final results = ref.watch(testResultsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Résultat du test')),
      body: AsyncValueView<List<ResultatTest>>(
        value: results,
        onRetry: () => ref.invalidate(testResultsProvider),
        isEmpty: (items) => !items.any((e) => e.testId == testId),
        emptyMessage: 'Aucun résultat disponible pour ce test.',
        data: (items) {
          final matches = items.where((e) => e.testId == testId).toList()
            ..sort((a, b) => b.date.compareTo(a.date));
          return _ResultContent(result: matches.first);
        },
      ),
    );
  }
}

class _ResultContent extends StatelessWidget {
  const _ResultContent({required this.result});

  final ResultatTest result;

  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(20),
    children: [
      Center(
        child: Container(
          padding: const EdgeInsets.all(22),
          decoration: const BoxDecoration(
            color: AppColors.greenSoft,
            shape: BoxShape.circle,
          ),
          child: Icon(
            result.passed ? AppIcons.check : AppIcons.tests,
            size: 42,
            color: result.passed ? AppColors.green : AppColors.goldDeep,
          ),
        ),
      ),
      const SizedBox(height: 18),
      Text(
        '${result.score.toStringAsFixed(0)}%',
        textAlign: TextAlign.center,
        style: Theme.of(context).textTheme.displayMedium,
      ),
      const SizedBox(height: 8),
      Center(
        child: StatusBadge(
          label: result.passed ? 'Résultat favorable' : 'À retenter',
          tone: result.passed ? BadgeTone.success : BadgeTone.neutral,
        ),
      ),
      const SizedBox(height: 16),
      AppCard(
        child: Column(
          children: [
            Text('Date : ${formatDateFr(result.date)}'),
            const SizedBox(height: 8),
            const Text(
              'Ce résultat est une preuve de parcours, pas une certification officielle.',
              style: TextStyle(color: AppColors.muted),
            ),
          ],
        ),
      ),
      if (result.proofId != null && result.proofId!.isNotEmpty) ...[
        const SizedBox(height: 20),
        AppButton(
          label: 'Voir la preuve',
          icon: AppIcons.preuves,
          onPressed: () => context.push('/preuves/${result.proofId!}'),
        ),
      ],
      const SizedBox(height: 12),
      AppButton(
        label: 'Retour aux tests',
        variant: AppButtonVariant.secondary,
        onPressed: () => context.go('/tests'),
      ),
    ],
  );
}
