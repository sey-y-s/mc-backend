import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/presentation/test_numerique_providers.dart';

class TestIntroScreen extends ConsumerWidget {
  const TestIntroScreen({super.key, this.id});
  final String? id;
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final testId = id;
    if (testId == null || testId.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final test = ref.watch(testDetailProvider(testId));
    return Scaffold(
      appBar: AppBar(title: const Text('Avant de commencer')),
      body: AsyncValueView<TestNumerique>(
        value: test,
        onRetry: () => ref.invalidate(testDetailProvider(testId)),
        data: (item) => ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(item.title, style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 8),
            Text(
              item.competenceName,
              style: const TextStyle(color: AppColors.muted),
            ),
            const SizedBox(height: 20),
            AppCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'À propos de ce test',
                    style: TextStyle(fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    item.description.isEmpty
                        ? 'Répondez aux questions pour évaluer vos connaissances.'
                        : item.description,
                  ),
                  const SizedBox(height: 14),
                  if (item.durationMinutes != null)
                    _Info(
                      icon: AppIcons.clock,
                      text:
                          'Durée estimée : ${item.durationMinutes} minutes',
                    ),
                  _Info(
                    icon: AppIcons.tests,
                    text: '${item.questions.length} question(s)',
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            const AppCard(
              child: Text(
                'Vous pouvez avancer une question à la fois. Une tentative terminée peut créer une preuve dans votre passeport. Cette preuve ne vaut pas une certification officielle.',
              ),
            ),
            const SizedBox(height: 24),
            AppButton(
              label: 'Commencer le test',
              icon: AppIcons.arrowRight,
              onPressed: () {
                ref.read(testRunProvider(testId).notifier).state = TestRun(
                  testId: testId,
                );
                context.go('/tests/$testId/question');
              },
            ),
          ],
        ),
      ),
    );
  }
}

class _Info extends StatelessWidget {
  const _Info({required this.icon, required this.text});
  final IconData icon;
  final String text;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(top: 6),
    child: Row(
      children: [
        Icon(icon, size: 17, color: AppColors.green),
        const SizedBox(width: 8),
        Text(text),
      ],
    ),
  );
}
