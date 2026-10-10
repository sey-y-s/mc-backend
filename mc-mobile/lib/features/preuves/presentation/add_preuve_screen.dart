import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/choice_sheet_field.dart';
import 'package:mlc_mobile/core/widgets/file_picker_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/core/widgets/selectable_option_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/preuves/presentation/preuve_providers.dart';

/// Route : `/preuves/ajouter?competenceId=<id d'une CitoyenCompetence>` (facultatif).
/// Trois choix : compétence, type de preuve, fichier (photo ou document).
class AddPreuveScreen extends ConsumerStatefulWidget {
  const AddPreuveScreen({super.key, this.competenceId});
  final String? competenceId;

  @override
  ConsumerState<AddPreuveScreen> createState() => _AddPreuveScreenState();
}

class _AddPreuveScreenState extends ConsumerState<AddPreuveScreen> {
  final _formKey = GlobalKey<FormState>();
  late String? _competenceId = widget.competenceId;
  PreuveType? _type;
  PickedMedia? _media;
  bool _showErrors = false;

  Future<void> _submit() async {
    setState(() => _showErrors = true);
    if (_competenceId == null || _type == null || _media == null) return;
    final ok = await ref
        .read(addPreuveControllerProvider.notifier)
        .submit(
          citoyenCompetenceId: _competenceId!,
          type: _type!,
          media: _media!,
        );
    if (!ok || !mounted) return;
    showSuccess(context, 'Preuve ajoutée');
    Navigator.of(context).maybePop();
  }

  Widget _shell(Widget body) => Scaffold(
    appBar: AppBar(title: const Text('Ajouter une preuve')),
    body: body,
  );

  @override
  Widget build(BuildContext context) {
    final comps = ref.watch(competencesProvider);
    final add = ref.watch(addPreuveControllerProvider);
    final progress = ref.watch(preuveProgressProvider);
    final busy = add.isLoading;
    final text = Theme.of(context).textTheme;

    return comps.when(
      skipLoadingOnReload: true,
      loading: () => _shell(const LoadingView()),
      error: (e, _) => _shell(
        ErrorView(error: e, onRetry: () => ref.invalidate(competencesProvider)),
      ),
      data: (items) {
        if (items.isEmpty) {
          return _shell(
            EmptyView(
              message: "Ajoutez d'abord une compétence pour pouvoir lui associer une preuve.",
              icon: Icons.workspace_premium_outlined,
              actionLabel: 'Ajouter une compétence',
              onAction: () => context.push('/competences/ajouter'),
            ),
          );
        }
        final selected = items.where((c) => c.id == _competenceId).firstOrNull;
        return FormScaffold(
          title: 'Ajouter une preuve',
          formKey: _formKey,
          submitLabel: 'Ajouter la preuve',
          onSubmit: _submit,
          isLoading: busy,
          error: add.hasError ? add.error : null,
          children: [
            ChoiceSheetField<CitoyenCompetence>(
              label: 'Compétence',
              placeholder: 'Choisir la compétence',
              options: items,
              labelOf: (c) => c.competenceNom,
              selected: selected,
              enabled: !busy,
              errorText: _showErrors && selected == null
                  ? 'Choisissez la compétence concernée'
                  : null,
              onChanged: (c) => setState(() => _competenceId = c.id),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  'Type de preuve',
                  style: text.bodySmall?.copyWith(color: AppColors.muted),
                ),
                const SizedBox(height: 8),
                for (final t in PreuveType.addable) ...[
                  SelectableOptionCard(
                    title: t.label,
                    description: t.description,
                    selected: _type == t,
                    onTap: busy ? null : () => setState(() => _type = t),
                  ),
                  const SizedBox(height: 10),
                ],
                if (_showErrors && _type == null)
                  Text(
                    'Choisissez le type de preuve',
                    style: text.bodySmall?.copyWith(color: AppColors.error),
                  ),
              ],
            ),
            FilePickerField(
              label: 'Ajouter le fichier de la preuve',
              allowed: const {MediaKind.image, MediaKind.document},
              value: _media,
              enabled: !busy,
              uploadProgress: busy ? progress : null,
              errorText: _showErrors && _media == null
                  ? 'Ajoutez la photo ou le document'
                  : null,
              onChanged: (m) => setState(() => _media = m),
            ),
          ],
        );
      },
    );
  }
}
