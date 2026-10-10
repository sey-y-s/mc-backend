import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/presentation/test_numerique_providers.dart';

class TestQuestionScreen extends ConsumerStatefulWidget {
  const TestQuestionScreen({super.key, this.id});
  final String? id;
  @override
  ConsumerState<TestQuestionScreen> createState() => _TestQuestionScreenState();
}

class _TestQuestionScreenState extends ConsumerState<TestQuestionScreen> {
  bool _sending = false;
  @override
  Widget build(BuildContext context) {
    final id = widget.id;
    if (id == null || id.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final async = ref.watch(testDetailProvider(id));
    final run = ref.watch(testRunProvider(id));
    return Scaffold(
      appBar: AppBar(title: const Text('Question du test')),
      body: AsyncValueView<TestNumerique>(
        value: async,
        onRetry: () => ref.invalidate(testDetailProvider(id)),
        data: (test) {
          if (test.questions.isEmpty) {
            return const EmptyView(
              message: 'Ce test ne contient aucune question.',
            );
          }
          if (run == null) {
            return Center(
              child: AppButton(
                label: 'Démarrer le test',
                onPressed: () {
                  ref.read(testRunProvider(id).notifier).state = TestRun(
                    testId: id,
                  );
                },
              ),
            );
          }
          final index = run.index.clamp(0, test.questions.length - 1).toInt();
          final question = test.questions[index];
          final answer = run.answers[question.id] ?? const TestAnswer();
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Row(
                children: [
                  Expanded(
                    child: LinearProgressIndicator(
                      value: (index + 1) / test.questions.length,
                      color: AppColors.green,
                      backgroundColor: AppColors.greenSoft,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    '${index + 1} / ${test.questions.length}',
                  ),
                ],
              ),
              const SizedBox(height: 24),
              Text(
                question.type.label.toUpperCase(),
                style: const TextStyle(
                  color: AppColors.green,
                  fontWeight: FontWeight.w700,
                  fontSize: 12,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                question.prompt,
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 20),
              if (question.type == QuestionType.texteLibre)
                TextFormField(
                  key: ValueKey(question.id),
                  initialValue: answer.text,
                  maxLines: 5,
                  onChanged: (value) =>
                      _save(id, run, question, TestAnswer(text: value)),
                  decoration: const InputDecoration(
                    labelText: 'Votre réponse',
                    border: OutlineInputBorder(),
                  ),
                )
              else
                AppCard(
                  child: Column(
                    children: [
                      if (question.type == QuestionType.choixMultiple)
                        for (final option in question.options)
                          CheckboxListTile(
                            value: answer.optionIds.contains(option.id),
                            title: Text(option.label),
                            controlAffinity: ListTileControlAffinity.leading,
                            onChanged: (checked) {
                              final ids = [...answer.optionIds];
                              if (checked == true && !ids.contains(option.id)) {
                                ids.add(option.id);
                              }
                              if (checked != true) ids.remove(option.id);
                              _save(
                                id,
                                run,
                                question,
                                TestAnswer(optionIds: ids),
                              );
                            },
                          )
                        else
                          RadioGroup<String>(
                            groupValue: answer.optionIds.isEmpty
                                ? null
                                : answer.optionIds.first,
                            onChanged: (value) => _save(
                              id,
                              run,
                              question,
                              TestAnswer(
                                optionIds: value == null ? [] : [value],
                              ),
                            ),
                            child: Column(
                              children: [
                                for (final option in question.options)
                                  RadioListTile<String>(
                                    value: option.id,
                                    title: Text(option.label),
                                  ),
                              ],
                            ),
                          ),
                    ],
                  ),
                ),
              if (question.type == QuestionType.texteLibre) ...[
                const SizedBox(height: 12),
                const Text(
                  'Les réponses libres sont conservées dans le parcours, mais le serveur actuel ne les inclut pas dans le calcul du score.',
                  style: TextStyle(color: AppColors.muted, fontSize: 12),
                ),
              ],
              const SizedBox(height: 24),
              Row(
                children: [
                  if (index > 0)
                    Expanded(
                      child: AppButton(
                        label: 'Précédente',
                        variant: AppButtonVariant.secondary,
                        onPressed: () =>
                            ref.read(testRunProvider(id).notifier).state = run
                                .copyWith(index: index - 1),
                      ),
                    ),
                  if (index > 0) const SizedBox(width: 10),
                  Expanded(
                    child: AppButton(
                      label: index == test.questions.length - 1
                          ? 'Terminer'
                          : 'Suivante',
                      isLoading: _sending,
                      onPressed: () => index == test.questions.length - 1
                          ? _submit(context, id, run, test.questions.length)
                          : ref.read(testRunProvider(id).notifier).state = run
                                .copyWith(index: index + 1),
                    ),
                  ),
                ],
              ),
            ],
          );
        },
      ),
    );
  }

  void _save(String id, TestRun run, TestQuestion question, TestAnswer answer) {
    final answers = Map<String, TestAnswer>.from(run.answers)
      ..[question.id] = answer;
    ref.read(testRunProvider(id).notifier).state = run.copyWith(
      answers: answers,
    );
  }

  Future<void> _submit(
    BuildContext context,
    String id,
    TestRun run,
    int count,
  ) async {
    if (_sending) return;
    setState(() => _sending = true);
    try {
      final result = await ref
          .read(testNumeriqueRepositoryProvider)
          .submit(id, TestAnswers(run.answers));
      ref.read(testsRevisionProvider.notifier).state++;
      ref.read(testRunProvider(id).notifier).state = null;
      if (context.mounted) {
        context.go('/tests/$id/resultat', extra: result);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(failureMessage(e))));
      }
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }
}
