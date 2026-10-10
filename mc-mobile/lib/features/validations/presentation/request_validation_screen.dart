import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/choice_sheet_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/core/widgets/selectable_option_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/preuves/presentation/preuve_providers.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_providers.dart';

/// Route : /validations/demander?competenceId=&preuveId= (paramètres facultatifs).
/// Deux choix : la compétence (pas déjà validée), puis l'une de ses preuves.
class RequestValidationScreen extends ConsumerStatefulWidget {
  const RequestValidationScreen({super.key, this.competenceId, this.preuveId});
  final String? competenceId;
  final String? preuveId;

  @override
  ConsumerState<RequestValidationScreen> createState() => _RequestValidationScreenState();
}

class _RequestValidationScreenState extends ConsumerState<RequestValidationScreen> {
  final _formKey = GlobalKey<FormState>();
  late String? _competenceId = widget.competenceId;
  late String? _preuveId = widget.preuveId;
  bool _showErrors = false;

  Future<void> _submit() async {
    setState(() => _showErrors = true);
    if (_competenceId == null || _preuveId == null) return;
    final ok = await ref.read(requestValidationControllerProvider.notifier).submit(
          citoyenCompetenceId: _competenceId!,
          preuveId: _preuveId!,
        );
    if (!ok || !mounted) return;
    showSuccess(context, 'Demande envoyée');
    Navigator.of(context).maybePop();
  }

  Widget _shell(Widget body) => Scaffold(appBar: AppBar(title: const Text('Demander une validation')), body: body);

  @override
  Widget build(BuildContext context) {
    final comps = ref.watch(competencesProvider);
    final request = ref.watch(requestValidationControllerProvider);
    final busy = request.isLoading;
    final text = Theme.of(context).textTheme;

    return comps.when(
      skipLoadingOnReload: true,
      loading: () => _shell(const LoadingView()),
      error: (e, _) => _shell(ErrorView(error: e, onRetry: () => ref.invalidate(competencesProvider))),
      data: (items) {
        final eligible = items.where((c) => c.etat != EtatCompetence.validee).toList();
        if (eligible.isEmpty) {
          return _shell(EmptyView(
            message: items.isEmpty
                ? "Ajoutez d'abord une compétence pour pouvoir demander sa validation."
                : 'Toutes vos compétences sont déjà validées.',
            icon: AppIcons.validations,
            actionLabel: items.isEmpty ? 'Ajouter une compétence' : null,
            onAction: items.isEmpty ? () => context.push('/competences/ajouter') : null,
          ));
        }
        final selected = eligible.where((c) => c.id == _competenceId).firstOrNull;
        return FormScaffold(
          title: 'Demander une validation',
          formKey: _formKey,
          submitLabel: 'Envoyer la demande',
          onSubmit: _submit,
          isLoading: busy,
          error: request.hasError ? request.error : null,
          children: [
            Text(
              'Un évaluateur ou un centre examinera votre preuve et confirmera votre compétence.',
              style: text.bodyMedium?.copyWith(color: AppColors.muted),
            ),
            ChoiceSheetField<CitoyenCompetence>(
              label: 'Compétence',
              placeholder: 'Choisir la compétence',
              options: eligible,
              labelOf: (c) => c.competenceNom,
              selected: selected,
              enabled: !busy,
              errorText: _showErrors && selected == null ? 'Choisissez la compétence à faire valider' : null,
              onChanged: (c) => setState(() {
                if (c.id != _competenceId) _preuveId = null; // la preuve dépend de la compétence
                _competenceId = c.id;
              }),
            ),
            if (selected == null)
              Text("Choisissez d'abord la compétence.", style: text.bodySmall?.copyWith(color: AppColors.muted))
            else
              _ProofsPicker(
                competenceId: selected.id,
                selectedId: _preuveId,
                busy: busy,
                showError: _showErrors && _preuveId == null,
                onSelect: (id) => setState(() => _preuveId = id),
              ),
          ],
        );
      },
    );
  }
}

class _ProofsPicker extends ConsumerWidget {
  const _ProofsPicker({required this.competenceId, required this.selectedId, required this.busy, required this.showError, required this.onSelect});
  final String competenceId;
  final String? selectedId;
  final bool busy;
  final bool showError;
  final ValueChanged<String> onSelect;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final text = Theme.of(context).textTheme;
    final value = ref.watch(preuvesForCompetenceProvider(competenceId));
    return value.when(
      loading: () => const SizedBox(height: 80, child: LoadingView()),
      error: (e, _) => SizedBox(height: 160, child: ErrorView(error: e, onRetry: () => ref.invalidate(preuvesForCompetenceProvider(competenceId)))),
      data: (proofs) {
        if (proofs.isEmpty) {
          return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            Text(
              "Cette compétence n'a pas encore de preuve. Ajoutez-en une pour demander une validation.",
              style: text.bodyMedium?.copyWith(color: AppColors.muted),
            ),
            const SizedBox(height: 12),
            AppButton(
              label: 'Ajouter une preuve',
              variant: AppButtonVariant.secondary,
              onPressed: () => context.push('/preuves/ajouter?competenceId=$competenceId'),
            ),
          ]);
        }
        return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          Text('Quelle preuve soumettre ?', style: text.bodySmall?.copyWith(color: AppColors.muted)),
          const SizedBox(height: 8),
          for (final p in proofs) ...[
            Builder(builder: (_) {
              final available = p.statut == null || p.statut == ValidationStatut.rejetee;
              return Opacity(
                opacity: available ? 1 : 0.5,
                child: SelectableOptionCard(
                  title: p.type.label,
                  description: available
                      ? 'Ajoutée le ${formatDateFr(p.date)}'
                      : (p.statut == ValidationStatut.enAttente ? 'Validation en cours' : 'Déjà validée'),
                  selected: selectedId == p.id,
                  onTap: available && !busy ? () => onSelect(p.id) : null,
                ),
              );
            }),
            const SizedBox(height: 10),
          ],
          if (showError) Text('Choisissez la preuve à soumettre', style: text.bodySmall?.copyWith(color: AppColors.error)),
        ]);
      },
    );
  }
}