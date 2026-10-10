import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_switch_tile.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/date_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/core/widgets/multi_select_chips.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_validators.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_providers.dart';

/// Routes : /experiences/nouvelle (initial == null) et, via ExperienceEditScreen, /experiences/:id/modifier.
/// Un seul formulaire pour la création et la modification.
class ExperienceFormScreen extends ConsumerStatefulWidget {
  const ExperienceFormScreen({super.key, this.initial});
  final Experience? initial;

  @override
  ConsumerState<ExperienceFormScreen> createState() => _ExperienceFormScreenState();
}

class _ExperienceFormScreenState extends ConsumerState<ExperienceFormScreen> {
  final _formKey = GlobalKey<FormState>();
  late final _titre = TextEditingController(text: widget.initial?.titre);
  late final _entreprise = TextEditingController(text: widget.initial?.entreprise);
  late final _description = TextEditingController(text: widget.initial?.description);
  late DateTime? _debut = widget.initial?.dateDebut;
  late DateTime? _fin = widget.initial?.dateFin;
  late bool _enCours = widget.initial?.enCours ?? false;
  late bool _reconversion = widget.initial?.reconversion ?? false;
  late Set<String> _competenceIds = {for (final c in widget.initial?.competences ?? const <ExperienceCompetence>[]) c.citoyenCompetenceId};
  bool _showErrors = false;

  bool get _isEdit => widget.initial != null;

  @override
  void dispose() {
    _titre.dispose();
    _entreprise.dispose();
    _description.dispose();
    super.dispose();
  }

  String? _optional(TextEditingController c) => c.text.trim().isEmpty ? null : c.text.trim();

  Future<void> _submit() async {
    setState(() => _showErrors = true);
    final formOk = _formKey.currentState!.validate();
    final dateErrors = ExperienceValidators.dates(debut: _debut, fin: _fin, enCours: _enCours);
    if (!formOk || dateErrors.isNotEmpty) return;
    final ok = await ref.read(experienceActionsProvider.notifier).save(
          id: widget.initial?.id,
          input: ExperienceInput(
            titre: _titre.text.trim(),
            entreprise: _optional(_entreprise),
            description: _optional(_description),
            dateDebut: _debut!,
            dateFin: _enCours ? null : _fin,
            enCours: _enCours,
            reconversion: _reconversion,
            citoyenCompetenceIds: _competenceIds.toList(),
          ),
        );
    if (!ok || !mounted) return;
    showSuccess(context, _isEdit ? 'Expérience modifiée' : 'Expérience ajoutée');
    Navigator.of(context).maybePop();
  }

  @override
  Widget build(BuildContext context) {
    final actions = ref.watch(experienceActionsProvider);
    final busy = actions.isLoading;
    final dateErrors = _showErrors ? ExperienceValidators.dates(debut: _debut, fin: _fin, enCours: _enCours) : const <String, String>{};
    final text = Theme.of(context).textTheme;
    final competences = ref.watch(competencesProvider);

    return FormScaffold(
      title: _isEdit ? "Modifier l'expérience" : 'Nouvelle expérience',
      formKey: _formKey,
      submitLabel: 'Enregistrer',
      onSubmit: _submit,
      isLoading: busy,
      error: actions.hasError ? actions.error : null,
      children: [
        AppTextField(
          label: "Titre du poste ou de l'activité",
          controller: _titre,
          enabled: !busy,
          textInputAction: TextInputAction.next,
          validator: ExperienceValidators.titre,
        ),
        AppTextField(label: 'Entreprise ou lieu (facultatif)', controller: _entreprise, enabled: !busy, textInputAction: TextInputAction.next),
        DateField(
          label: 'Date de début',
          value: _debut,
          enabled: !busy,
          errorText: dateErrors['dateDebut'],
          onChanged: (d) => setState(() => _debut = d),
        ),
        AppSwitchTile(
          title: 'Poste actuel',
          subtitle: 'Je fais encore cette activité',
          value: _enCours,
          onChanged: busy
              ? null
              : (v) => setState(() {
                    _enCours = v;
                    if (v) _fin = null;
                  }),
        ),
        if (!_enCours)
          DateField(
            label: 'Date de fin',
            value: _fin,
            enabled: !busy,
            errorText: dateErrors['dateFin'],
            onChanged: (d) => setState(() => _fin = d),
          ),
        AppTextField(label: 'Description (facultatif)', controller: _description, enabled: !busy, maxLines: 4),
        AppSwitchTile(
          title: 'Reconversion',
          subtitle: 'Cette expérience marque un changement de métier',
          value: _reconversion,
          onChanged: busy ? null : (v) => setState(() => _reconversion = v),
        ),
        Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Compétences utilisées', style: text.titleSmall),
          const SizedBox(height: 2),
          Text(
            'Facultatif. Choisissez parmi les compétences que vous avez déjà déclarées.',
            style: text.bodySmall?.copyWith(color: AppColors.muted),
          ),
          const SizedBox(height: 12),
          competences.when(
            loading: () => const SizedBox(height: 48, child: Center(child: CircularProgressIndicator())),
            error: (e, _) => Text(failureMessage(e), style: text.bodySmall?.copyWith(color: AppColors.error)),
            data: (items) => items.isEmpty
                ? Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                    Text("Vous n'avez pas encore déclaré de compétence.", style: text.bodyMedium?.copyWith(color: AppColors.muted)),
                    const SizedBox(height: 12),
                    AppButton(
                      label: 'Ajouter une compétence',
                      variant: AppButtonVariant.secondary,
                      onPressed: busy ? null : () => context.push('/competences/ajouter'),
                    ),
                  ])
                : MultiSelectChips<CitoyenCompetence>(
                    options: items,
                    idOf: (c) => c.id,
                    labelOf: (c) => c.competenceNom,
                    selectedIds: _competenceIds,
                    enabled: !busy,
                    onChanged: (ids) => setState(() => _competenceIds = ids),
                  ),
          ),
        ]),
      ],
    );
  }
}