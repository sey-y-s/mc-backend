import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/info_row.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_labels.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_providers.dart';
import 'package:mlc_mobile/shared/widgets/validation_status_badge.dart';

/// Route : /validations/:id. Lecture seule, avec un raccourci pour refaire une demande après un refus.
class ValidationDetailScreen extends ConsumerWidget {
  const ValidationDetailScreen({super.key, this.id});
  final String? id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    if (id == null) {
      return Scaffold(appBar: AppBar(title: const Text('Validation')), body: const ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(validationDetailProvider(id));
    return Scaffold(
      appBar: AppBar(title: const Text('Validation')),
      body: AsyncValueView<Validation>(
        value: value,
        onRetry: () => ref.invalidate(validationDetailProvider(id)),
        data: (v) => _Content(validation: v),
      ),
    );
  }
}

class _Content extends StatelessWidget {
  const _Content({required this.validation});
  final Validation validation;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final v = validation;
    return ListView(padding: const EdgeInsets.all(20), children: [
      Text(v.competenceNom, style: text.headlineSmall),
      Text(v.type.label, style: text.bodyMedium?.copyWith(color: AppColors.muted)),
      const SizedBox(height: 16),
      AppCard(
        accentColor: validationAccentColor(v.statut),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          ValidationStatusBadge(v.statut),
          const SizedBox(height: 8),
          Text(validationStatutHelp(v.statut), style: text.bodyMedium),
        ]),
      ),
      const SizedBox(height: 12),
      AppCard(
        child: Column(children: [
          InfoRow(label: 'Demandée le', value: formatDateFr(v.dateDemande)),
          if (v.dateDecision != null) InfoRow(label: 'Décision le', value: formatDateFr(v.dateDecision!)),
          if (v.validateurLabel != null) InfoRow(label: 'Validateur', value: v.validateurLabel!),
        ]),
      ),
      if (v.commentaire != null) ...[
        const SizedBox(height: 24),
        const SectionHeader(title: 'Commentaire'),
        AppCard(child: Text(v.commentaire!, style: text.bodyMedium)),
      ],
      const SizedBox(height: 12),
      if (v.preuveId != null) ...[
        AppCard(
          onTap: () => context.push('/preuves/${v.preuveId}'),
          child: Row(children: [
            Expanded(child: Text('Voir la preuve associée', style: text.titleSmall)),
            const Icon(AppIcons.chevron, color: AppColors.muted),
          ]),
        ),
        const SizedBox(height: 12),
      ],
      AppCard(
        onTap: () => context.push('/competences/${v.citoyenCompetenceId}'),
        child: Row(children: [
          Expanded(child: Text('Voir la compétence', style: text.titleSmall)),
          const Icon(AppIcons.chevron, color: AppColors.muted),
        ]),
      ),
      if (v.statut == ValidationStatut.rejetee) ...[
        const SizedBox(height: 24),
        AppButton(
          label: 'Demander une nouvelle validation',
          onPressed: () => context.push(
            '/validations/demander?competenceId=${v.citoyenCompetenceId}${v.preuveId != null ? '&preuveId=${v.preuveId}' : ''}',
          ),
        ),
      ],
    ]);
  }
}