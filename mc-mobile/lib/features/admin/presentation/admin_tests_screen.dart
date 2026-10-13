import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/admin/domain/admin_models.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';

class AdminTestsScreen extends ConsumerWidget {
  const AdminTestsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tests = ref.watch(adminTestsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Tests numériques')),
      body: tests.when(
        loading: () => const LoadingView(),
        error: (error, _) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(adminTestsProvider),
        ),
        data: (items) => RefreshIndicator(
          onRefresh: () => ref.refresh(adminTestsProvider.future),
          child: items.isEmpty
              ? const EmptyView(message: 'Aucun test numérique.')
              : ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: items.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 10),
                  itemBuilder: (context, index) {
                    final test = items[index];
                    return AppCard(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              const Icon(
                                Icons.quiz_outlined,
                                color: AppColors.green,
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Text(
                                  test.title,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w700,
                                  ),
                                ),
                              ),
                            ],
                          ),
                          Text(
                            '${test.questions.length} questions · ${test.status == 'PUBLIQUE' ? 'Publié' : 'Brouillon'}',
                            style: const TextStyle(color: AppColors.muted),
                          ),
                          if (test.description.isNotEmpty) ...[
                            const SizedBox(height: 6),
                            Text(test.description),
                          ],
                        ],
                      ),
                    );
                  },
                ),
        ),
      ),
    );
  }

}

class AdminTestEditorScreen extends ConsumerStatefulWidget {
  const AdminTestEditorScreen({super.key, this.test});

  final AdminQcmTest? test;

  @override
  ConsumerState<AdminTestEditorScreen> createState() =>
      _AdminTestEditorScreenState();
}

class _AdminTestEditorScreenState extends ConsumerState<AdminTestEditorScreen> {
  final _formKey = GlobalKey<FormState>();
  late final _title = TextEditingController(text: widget.test?.title ?? '');
  late final _description = TextEditingController(
    text: widget.test?.description ?? '',
  );
  late final List<_QuestionDraft> _questions = widget.test == null
      ? [_QuestionDraft()]
      : widget.test!.questions.map(_QuestionDraft.fromQuestion).toList();
  String? _competenceId;
  bool _active = true;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _competenceId = widget.test?.competenceId;
    _active = widget.test?.status == 'PUBLIQUE' || widget.test == null;
  }

  @override
  void dispose() {
    _title.dispose();
    _description.dispose();
    for (final question in _questions) {
      question.dispose();
    }
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    if (_competenceId == null || _competenceId!.isEmpty) {
      showError(context, 'Sélectionnez une compétence.');
      return;
    }
    for (var i = 0; i < _questions.length; i++) {
      final question = _questions[i];
      if (question.answers.length < 2 ||
          question.answers.every((answer) => !answer.correct)) {
        showError(
          context,
          'La question ${i + 1} doit avoir au moins deux réponses et une réponse correcte.',
        );
        return;
      }
      if (question.answers.any((answer) => answer.text.text.trim().isEmpty)) {
        showError(context, 'Toutes les propositions doivent être renseignées.');
        return;
      }
    }
    setState(() => _saving = true);
    try {
      await ref
          .read(apiAdminRepositoryProvider)
          .saveTest(
            id: widget.test?.id,
            competenceId: _competenceId!,
            title: _title.text,
            description: _description.text,
            active: _active,
            questions: [
              for (var i = 0; i < _questions.length; i++)
                AdminQcmQuestion(
                  text: _questions[i].text.text,
                  order: i + 1,
                  answers: [
                    for (final answer in _questions[i].answers)
                      AdminQcmAnswer(
                        text: answer.text.text,
                        correct: answer.correct,
                      ),
                  ],
                ),
            ],
          );
      ref.invalidate(adminTestsProvider);
      ref.invalidate(adminDashboardProvider);
      if (mounted) {
        showSuccess(context, 'Test enregistré');
        context.pop();
      }
    } catch (error) {
      if (mounted) showError(context, failureMessage(error));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final competences = ref.watch(adminCompetencesProvider);
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.test == null ? 'Créer un test' : 'Modifier le test'),
      ),
      body: competences.when(
        loading: () => const LoadingView(),
        error: (error, _) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(adminCompetencesProvider),
        ),
        data: (options) => Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
            children: [
              AppCard(
                child: Column(
                  children: [
                    DropdownButtonFormField<String>(
                      initialValue: options.any((item) => item.id == _competenceId)
                          ? _competenceId
                          : null,
                      decoration: const InputDecoration(
                        labelText: 'Compétence',
                        prefixIcon: Icon(Icons.workspace_premium_outlined),
                      ),
                      items: [
                        for (final option in options)
                          DropdownMenuItem(
                            value: option.id,
                            child: Text(option.name),
                          ),
                      ],
                      onChanged: _saving
                          ? null
                          : (value) => setState(() => _competenceId = value),
                      validator: (value) =>
                          value == null ? 'Choisissez une compétence.' : null,
                    ),
                    const SizedBox(height: 14),
                    TextFormField(
                      controller: _title,
                      decoration: const InputDecoration(labelText: 'Titre'),
                      textCapitalization: TextCapitalization.sentences,
                      validator: (value) => value == null || value.trim().isEmpty
                          ? 'Le titre est obligatoire.'
                          : null,
                    ),
                    const SizedBox(height: 14),
                    TextFormField(
                      controller: _description,
                      decoration: const InputDecoration(
                        labelText: 'Description',
                        alignLabelWithHint: true,
                      ),
                      minLines: 3,
                      maxLines: 5,
                    ),
                    SwitchListTile(
                      contentPadding: EdgeInsets.zero,
                      title: const Text('Publier immédiatement'),
                      value: _active,
                      onChanged: _saving
                          ? null
                          : (value) => setState(() => _active = value),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      'Questions QCM',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                  ),
                  TextButton.icon(
                    onPressed: _saving
                        ? null
                        : () => setState(() => _questions.add(_QuestionDraft())),
                    icon: const Icon(Icons.add),
                    label: const Text('Ajouter'),
                  ),
                ],
              ),
              const Text(
                'Chaque question accepte plusieurs bonnes réponses.',
                style: TextStyle(color: AppColors.muted),
              ),
              const SizedBox(height: 10),
              for (var i = 0; i < _questions.length; i++)
                _questionCard(i),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: _saving ? null : _save,
                icon: _saving
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.save_outlined),
                label: Text(_saving ? 'Enregistrement…' : 'Enregistrer le test'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _questionCard(int index) {
    final question = _questions[index];
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: AppCard(
        child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  'Question ${index + 1}',
                  style: const TextStyle(
                    color: AppColors.green,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              if (_questions.length > 1)
                IconButton(
                  tooltip: 'Supprimer la question',
                  onPressed: _saving
                      ? null
                      : () => setState(() {
                          _questions.removeAt(index).dispose();
                        }),
                  icon: const Icon(Icons.delete_outline, color: AppColors.error),
                ),
            ],
          ),
          TextFormField(
            controller: question.text,
            decoration: const InputDecoration(labelText: 'Énoncé'),
            minLines: 2,
            maxLines: 4,
            validator: (value) => value == null || value.trim().isEmpty
                ? 'Saisissez l’énoncé.'
                : null,
          ),
          const SizedBox(height: 8),
          for (var i = 0; i < question.answers.length; i++)
            Row(
              children: [
                Checkbox(
                  value: question.answers[i].correct,
                  onChanged: _saving
                      ? null
                      : (value) => setState(
                          () => question.answers[i].correct = value ?? false,
                        ),
                ),
                Expanded(
                  child: TextFormField(
                    controller: question.answers[i].text,
                    decoration: InputDecoration(
                      labelText: 'Proposition ${i + 1}',
                      isDense: true,
                    ),
                    validator: (value) => value == null || value.trim().isEmpty
                        ? 'Renseignez la proposition.'
                        : null,
                  ),
                ),
                if (question.answers.length > 2)
                  IconButton(
                    tooltip: 'Supprimer la proposition',
                    onPressed: _saving
                        ? null
                        : () => setState(
                            () => question.answers.removeAt(i).dispose(),
                          ),
                    icon: const Icon(Icons.close),
                  ),
              ],
            ),
          TextButton.icon(
            onPressed: _saving
                ? null
                : () => setState(() => question.answers.add(_AnswerDraft())),
            icon: const Icon(Icons.add),
            label: const Text('Ajouter une proposition'),
          ),
        ],
        ),
      ),
    );
  }
}

class _QuestionDraft {
  _QuestionDraft()
    : text = TextEditingController(),
      answers = [_AnswerDraft(correct: true), _AnswerDraft()];

  _QuestionDraft.fromQuestion(AdminQcmQuestion question)
    : text = TextEditingController(text: question.text),
      answers = question.answers.map(_AnswerDraft.fromAnswer).toList();

  final TextEditingController text;
  final List<_AnswerDraft> answers;

  void dispose() {
    text.dispose();
    for (final answer in answers) {
      answer.dispose();
    }
  }
}

class _AnswerDraft {
  _AnswerDraft({String value = '', this.correct = false})
    : text = TextEditingController(text: value);

  _AnswerDraft.fromAnswer(AdminQcmAnswer answer)
    : text = TextEditingController(text: answer.text),
      correct = answer.correct;

  final TextEditingController text;
  bool correct;

  void dispose() => text.dispose();
}
