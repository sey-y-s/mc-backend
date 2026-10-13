import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_dialogs.dart';
import 'package:mlc_mobile/core/widgets/info_row.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_labels.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_providers.dart';

/// Route : /experiences/:id
class ExperienceDetailScreen extends ConsumerWidget {
  const ExperienceDetailScreen({super.key, this.id});
  final String? id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    if (id == null) {
      return Scaffold(appBar: AppBar(title: const Text('Expérience')), body: const ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(experienceDetailProvider(id));
    return Scaffold(
      appBar: AppBar(title: const Text('Expérience')),
      body: AsyncValueView<Experience>(
        value: value,
        onRetry: () => ref.invalidate(experienceDetailProvider(id)),
        data: (e) => _Content(experience: e),
      ),
    );
  }
}

class _Content extends ConsumerWidget {
  const _Content({required this.experience});
  final Experience experience;

  Future<void> _delete(BuildContext context, WidgetRef ref) async {
    final confirmed = await confirmDialog(
      context,
      title: 'Supprimer cette expérience ?',
      message: 'Cette action est définitive.',
      confirmLabel: 'Supprimer',
      destructive: true,
    );
    if (!confirmed || !context.mounted) return;
    final done = await ref.read(experienceActionsProvider.notifier).delete(experience.id);
    if (!context.mounted) return;
    if (done) {
      showSuccess(context, 'Expérience supprimée');
      Navigator.of(context).maybePop();
    } else {
      showError(context, failureMessage(ref.read(experienceActionsProvider).error ?? const UnknownFailure()));
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final text = Theme.of(context).textTheme;
    final busy = ref.watch(experienceActionsProvider).isLoading;
    final competenceOptions = ref.watch(competencesProvider);
    final e = experience;
    return ListView(padding: const EdgeInsets.all(20), children: [
      Text(e.titre, style: text.headlineSmall),
      if (e.entreprise != null) Text(e.entreprise!, style: text.bodyLarge?.copyWith(color: AppColors.muted)),
      if (e.enCours || e.reconversion) ...[
        const SizedBox(height: 12),
        Wrap(spacing: 8, runSpacing: 6, children: [
          if (e.enCours) const StatusBadge(label: 'En cours', tone: BadgeTone.success),
          if (e.reconversion) const StatusBadge(label: 'Reconversion', tone: BadgeTone.accent),
        ]),
      ],
      const SizedBox(height: 20),
      AppCard(
        child: Column(children: [
          InfoRow(label: 'Début', value: formatDateFr(e.dateDebut)),
          InfoRow(label: 'Fin', value: e.enCours ? "Aujourd'hui" : (e.dateFin == null ? 'Non précisée' : formatDateFr(e.dateFin!))),
          InfoRow(label: 'Période', value: experiencePeriodLabel(e)),
        ]),
      ),
      if (e.description != null) ...[
        const SizedBox(height: 24),
        const SectionHeader(title: 'Description'),
        AppCard(child: Text(e.description!, style: text.bodyMedium)),
      ],
      const SizedBox(height: 24),
      const SectionHeader(title: 'Compétences utilisées'),
      if (e.competences.isEmpty)
        Text('Aucune compétence liée à cette expérience.', style: text.bodyMedium?.copyWith(color: AppColors.muted))
      else
        Wrap(spacing: 8, runSpacing: 8, children: [
          for (final c in e.competences)
            ActionChip(
              label: Text(_competenceLabel(c, competenceOptions)),
              onPressed: () =>
                  context.push('/competences/${c.citoyenCompetenceId}'),
            ),
        ]),
      const SizedBox(height: 32),
      AppButton(
        label: 'Modifier',
        variant: AppButtonVariant.secondary,
        onPressed: busy ? null : () => context.push('/experiences/${e.id}/modifier'),
      ),
      const SizedBox(height: 8),
      TextButton(
        onPressed: busy ? null : () => _delete(context, ref),
        style: TextButton.styleFrom(foregroundColor: AppColors.error),
        child: const Text('Supprimer cette expérience'),
      ),
    ]);
  }
}

String _competenceLabel(
  ExperienceCompetence link,
  AsyncValue<List<CitoyenCompetence>> options,
) {
  for (final option in options.asData?.value ?? const <CitoyenCompetence>[]) {
    if (option.id == link.citoyenCompetenceId) return option.competenceNom;
  }
  return link.nom.isEmpty ? 'Compétence liée' : link.nom;
}