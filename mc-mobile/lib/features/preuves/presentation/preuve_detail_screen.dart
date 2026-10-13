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
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/preuves/presentation/preuve_providers.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';
import 'package:mlc_mobile/shared/widgets/validation_status_badge.dart';


/// Route : /preuves/:id
/// TODO(PISTE A): ouvrir un document (PDF/Word) avec url_launcher — dépendance à annoncer avant ajout.

class PreuveDetailScreen extends ConsumerWidget {
  const PreuveDetailScreen({super.key, this.id});
  final String? id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    if (id == null) {
      return Scaffold(
          appBar: AppBar(title: const Text('Preuve')),
          body: const ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(preuveDetailProvider(id));
    return Scaffold(
      appBar: AppBar(title: const Text('Preuve')),
      body: AsyncValueView<Preuve>(
        value: value,
        onRetry: () => ref.invalidate(preuveDetailProvider(id)),
        data: (p) => _Content(preuve: p),
      ),
    );
  }
}

class _Content extends StatelessWidget {
  const _Content({required this.preuve});
  final Preuve preuve;

  static String _statutHelp(ValidationStatut s) => switch (s) {
        ValidationStatut.enAttente => 'Un évaluateur examine cette preuve.',
        ValidationStatut.approuvee => 'Cette preuve a été confirmée.',
        ValidationStatut.rejetee =>
          'Cette preuve a été refusée. Vous pouvez en ajouter une autre.',
      };

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final statut = preuve.statut;
    return ListView(padding: const EdgeInsets.all(20), children: [
      Text(preuve.type.label, style: text.headlineSmall),
      const SizedBox(height: 16),
      AppCard(
        onTap: () => context.push('/competences/${preuve.citoyenCompetenceId}'),
        child: Row(children: [
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Compétence concernée',
                  style: text.bodySmall?.copyWith(color: AppColors.muted)),
              Text(preuve.competenceNom, style: text.titleSmall),
            ]),
          ),
          const Icon(AppIcons.chevron, color: AppColors.muted),
        ]),
      ),
      const SizedBox(height: 12),
      AppCard(
        child: Column(children: [
          InfoRow(label: 'Ajoutée le', value: formatDateFr(preuve.date)),
          InfoRow(
              label: 'Fichier', value: preuve.fichierNom ?? 'Non disponible'),
        ]),
      ),
      if (preuve.isImage && preuve.fichierUrl != null) ...[
        const SizedBox(height: 12),
        ClipRRect(
          borderRadius: BorderRadius.circular(14),
          child: Image.network(
            preuve.fichierUrl!,
            height: 220,
            fit: BoxFit.cover,
            cacheWidth: 800, // jamais l'image pleine résolution en mémoire
            errorBuilder: (_, _, _) => const SizedBox(
              height: 120,
              child: Center(
                  child:
                      Icon(AppIcons.gallery, size: 40, color: AppColors.muted)),
            ),
          ),
        ),
      ],
      const SizedBox(height: 28),
      const SectionHeader(title: 'Validation'),
      AppCard(
        child: statut == null
            ? Text('Aucune validation demandée pour cette preuve.',
                style: text.bodyMedium?.copyWith(color: AppColors.muted))
            : Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                ValidationStatusBadge(statut),
                const SizedBox(height: 8),
                Text(_statutHelp(statut), style: text.bodyMedium),
              ]),
      ),

      if (statut == null || statut == ValidationStatut.rejetee) ...[
        const SizedBox(height: 16),
        AppButton(
          label: statut == null ? 'Demander une validation' : 'Demander une nouvelle validation',
          onPressed: () => context.push('/validations/demander?competenceId=${preuve.citoyenCompetenceId}&preuveId=${preuve.id}'),
        ),
      ],
    
    ]);
  }
}
