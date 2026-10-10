import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_progress_bar.dart';
import 'package:mlc_mobile/core/widgets/info_row.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/preuves/presentation/preuve_providers.dart';
import 'package:mlc_mobile/shared/widgets/etat_badge.dart';
import 'package:mlc_mobile/shared/widgets/preuve_tile.dart';

import 'package:mlc_mobile/features/validations/presentation/validation_labels.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_providers.dart';
import 'package:mlc_mobile/shared/widgets/validation_tile.dart';

/// Route : /competences/:id
class CompetenceDetailScreen extends ConsumerWidget {
  const CompetenceDetailScreen({super.key, this.id});
  final String? id;

  static double _trust(EtatCompetence e) => switch (e) {
        EtatCompetence.declaree => 1 / 3,
        EtatCompetence.attestee => 2 / 3,
        EtatCompetence.validee => 1,
      };

  static String _trustHelp(EtatCompetence e) => switch (e) {
        EtatCompetence.declaree =>
          'Vous avez déclaré cette compétence. Ajoutez une preuve pour la rendre attestée.',
        EtatCompetence.attestee =>
          'Une preuve est jointe. Une validation par un évaluateur ou un centre la rendra validée.',
        EtatCompetence.validee =>
          'Cette compétence a été confirmée par un évaluateur ou un centre.',
      };

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    if (id == null) {
      return Scaffold(
          appBar: AppBar(title: const Text('Compétence')),
          body: const ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(competenceDetailProvider(id));
    return Scaffold(
      appBar: AppBar(title: const Text('Compétence')),
      body: AsyncValueView<CitoyenCompetence>(
        value: value,
        onRetry: () => ref.invalidate(competenceDetailProvider(id)),
        data: (c) => _Content(item: c),
      ),
    );
  }
}

class _Content extends StatelessWidget {
  const _Content({required this.item});
  final CitoyenCompetence item;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return ListView(padding: const EdgeInsets.all(20), children: [
      Text(item.competenceNom, style: text.headlineSmall),
      if (item.secteurNom != null)
        Text(item.secteurNom!,
            style: text.bodyMedium?.copyWith(color: AppColors.muted)),
      const SizedBox(height: 12),
      Wrap(spacing: 8, runSpacing: 6, children: [
        StatusBadge(label: item.niveau.label),
        EtatBadge(item.etat)
      ]),
      const SizedBox(height: 24),
      AppCard(
        child:
            Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          AppProgressBar(
              label: 'Niveau de confiance',
              value: CompetenceDetailScreen._trust(item.etat)),
          const SizedBox(height: 10),
          Text(CompetenceDetailScreen._trustHelp(item.etat),
              style: text.bodySmall?.copyWith(color: AppColors.muted)),
        ]),
      ),
      const SizedBox(height: 12),
      AppCard(
          child: InfoRow(
              label: 'Déclarée le', value: formatDateFr(item.dateDeclaration))),
      const SizedBox(height: 28),
      SectionHeader(
        title: 'Preuves',
        actionLabel: 'Ajouter',
        onAction: () =>
            context.push('/preuves/ajouter?competenceId=${item.id}'),
      ),
      _ProofsBlock(competenceId: item.id),
      const SizedBox(height: 20),

      SectionHeader(
        title: 'Validations',
        actionLabel: item.etat == EtatCompetence.validee ? null : 'Demander',
        onAction: item.etat == EtatCompetence.validee ? null : () => context.push('/validations/demander?competenceId=${item.id}'),
      ),
      _ValidationsBlock(competenceId: item.id),

    ]);
  }
}

class _ProofsBlock extends ConsumerWidget {
  const _ProofsBlock({required this.competenceId});
  final String competenceId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(preuvesForCompetenceProvider(competenceId));
    return value.when(
      loading: () => const SizedBox(height: 80, child: LoadingView()),
      error: (e, _) => SizedBox(
          height: 160,
          child: ErrorView(
              error: e,
              onRetry: () =>
                  ref.invalidate(preuvesForCompetenceProvider(competenceId)))),
      data: (items) => items.isEmpty
          ? const _PendingBlock(
              'Aucune preuve pour cette compétence. Ajoutez un diplôme, un certificat ou une attestation.')
          : Column(children: [
              for (final p in items) ...[
                PreuveTile(
                  typeLabel: p.type.label,
                  competenceNom: p.competenceNom,
                  dateLabel: formatDateFr(p.date),
                  statut: p.statut,
                  onTap: () => context.push('/preuves/${p.id}'),
                ),
                const SizedBox(height: 12),
              ],
            ]),
    );
  }
}

class _PendingBlock extends StatelessWidget {
  const _PendingBlock(this.message);
  final String message;

  @override
  Widget build(BuildContext context) => AppCard(
      child: Text(message,
          style: Theme.of(context)
              .textTheme
              .bodyMedium
              ?.copyWith(color: AppColors.muted)));
}

class _ValidationsBlock extends ConsumerWidget {
  const _ValidationsBlock({required this.competenceId});
  final String competenceId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(validationsForCompetenceProvider(competenceId));
    return value.when(
      loading: () => const SizedBox(height: 80, child: LoadingView()),
      error: (e, _) => SizedBox(height: 160, child: ErrorView(error: e, onRetry: () => ref.invalidate(validationsForCompetenceProvider(competenceId)))),
      data: (items) => items.isEmpty
          ? const _PendingBlock("Aucune validation pour l'instant. Ajoutez une preuve, puis demandez une validation.")
          : Column(children: [
              for (final v in items) ...[
                ValidationTile(
                  typeLabel: v.type.label,
                  statut: v.statut,
                  dateLabel: validationDateLabel(v),
                  validateurLabel: v.validateurLabel,
                  onTap: () => context.push('/validations/${v.id}'),
                ),
                const SizedBox(height: 12),
              ],
            ]),
    );
  }
}