import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/domain/competence_ref.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/core/widgets/selectable_option_card.dart';

/// Route : /competences/ajouter
/// Étape 1 : chercher dans le référentiel.
/// Étape 2 : choisir son niveau, puis ajouter.
class AddCompetenceScreen extends ConsumerStatefulWidget {
  const AddCompetenceScreen({super.key});

  @override
  ConsumerState<AddCompetenceScreen> createState() =>
      _AddCompetenceScreenState();
}

class _AddCompetenceScreenState extends ConsumerState<AddCompetenceScreen> {
  final _search = TextEditingController();
  String _query = '';
  CompetenceRef? _selected;
  Niveau? _niveau;

  @override
  void initState() {
    super.initState();
    _search.addListener(() {
      final q = _search.text.trim();
      if (q != _query) setState(() => _query = q);
    });
  }

  @override
  void dispose() {
    _search.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final ok = await ref
        .read(addCompetenceControllerProvider.notifier)
        .submit(competence: _selected!, niveau: _niveau!);
    if (!ok || !mounted) return;
    showSuccess(context, 'Compétence ajoutée');
    Navigator.of(context).maybePop();
  }

  @override
  Widget build(BuildContext context) {
    final add = ref.watch(addCompetenceControllerProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Ajouter une compétence')),
      body: SafeArea(
        child: _selected == null ? _searchStep() : _levelStep(add),
      ),
    );
  }

  Widget _searchStep() => Padding(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 0),
        child:
            Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          Text('Quelle compétence voulez-vous ajouter ?',
              style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 12),
          AppTextField(
              label: 'Rechercher (ex. soudure, couture)',
              controller: _search,
              textInputAction: TextInputAction.search),
          const SizedBox(height: 8),
          Expanded(
            child: _query.length < 2
                ? const EmptyView(
                    message:
                        'Tapez au moins 2 lettres pour chercher une compétence.',
                    icon: AppIcons.competences)
                : _Results(
                    query: _query,
                    onSelect: (c) => setState(() => _selected = c)),
          ),
        ]),
      );

  Widget _levelStep(AsyncValue<void> add) {
    final busy = add.isLoading;
    return ListView(padding: const EdgeInsets.all(20), children: [
      AppCard(
        child: Row(children: [
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(_selected!.nom,
                  style: Theme.of(context).textTheme.titleMedium),
              if (_selected!.secteurNom != null)
                Text(_selected!.secteurNom!,
                    style: Theme.of(context)
                        .textTheme
                        .bodySmall
                        ?.copyWith(color: AppColors.muted)),
            ]),
          ),
          TextButton(
            onPressed: busy
                ? null
                : () => setState(() {
                      _selected = null;
                      _niveau = null;
                    }),
            child: const Text('Changer'),
          ),
        ]),
      ),
      const SizedBox(height: 24),
      const SectionHeader(title: 'Votre niveau'),
      for (final n in Niveau.values) ...[
        SelectableOptionCard(
          title: n.label,
          description: _levelDescription(n),
          selected: _niveau == n,
          onTap: busy ? null : () => setState(() => _niveau = n),
        ),
        const SizedBox(height: 12),
      ],
      if (add.hasError) ...[
        const SizedBox(height: 4),
        Text(failureMessage(add.error!),
            style: const TextStyle(color: AppColors.error)),
        const SizedBox(height: 12),
      ],
      const SizedBox(height: 8),
      AppButton(
          label: 'Ajouter cette compétence',
          onPressed: _niveau == null ? null : _submit,
          isLoading: busy),
    ]);
  }
}

class _Results extends ConsumerWidget {
  const _Results({required this.query, required this.onSelect});
  final String query;
  final ValueChanged<CompetenceRef> onSelect;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(referentielSearchProvider(query));
    return AsyncValueView<List<CompetenceRef>>(
      value: value,
      onRetry: () => ref.invalidate(referentielSearchProvider(query)),
      isEmpty: (d) => d.isEmpty,
      emptyMessage: 'Aucune compétence trouvée. Essayez un autre mot.',
      data: (items) => ListView.separated(
        padding: const EdgeInsets.only(top: 4, bottom: 16),
        itemCount: items.length,
        separatorBuilder: (_, _) => const SizedBox(height: 10),
        itemBuilder: (_, i) => AppCard(
          onTap: () => onSelect(items[i]),
          child: Row(children: [
            Expanded(
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(items[i].nom,
                        style: Theme.of(context).textTheme.titleSmall),
                    if (items[i].secteurNom != null)
                      Text(items[i].secteurNom!,
                          style: Theme.of(context)
                              .textTheme
                              .bodySmall
                              ?.copyWith(color: AppColors.muted)),
                  ]),
            ),
            const Icon(AppIcons.chevron, color: AppColors.muted),
          ]),
        ),
      ),
    );
  }
}

String _levelDescription(Niveau n) => switch (n) {
      Niveau.debutant => "Je découvre ou j'apprends encore.",
      Niveau.intermediaire => 'Je réalise seul les tâches courantes.',
      Niveau.expert => 'Je maîtrise et je peux former les autres.',
    };
