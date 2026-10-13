import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/app_progress_bar.dart';
import 'package:mlc_mobile/core/widgets/info_row.dart';
import 'package:mlc_mobile/core/widgets/section_header.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/domain/profile_completion.dart';
import 'package:mlc_mobile/features/passport/presentation/passport_providers.dart';
import 'package:mlc_mobile/shared/widgets/competence_card.dart';
import 'package:mlc_mobile/shared/widgets/disponibilite_badge.dart';
import 'package:mlc_mobile/shared/widgets/profile_header.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/features/preuves/presentation/preuve_providers.dart';
import 'package:mlc_mobile/shared/widgets/preuve_tile.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_labels.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_providers.dart';
import 'package:mlc_mobile/shared/widgets/experience_tile.dart';

/// Route : /passeport (onglet). Vue d'ensemble du passeport du citoyen.
/// 
/// SectionHeader + liste de 3 PreuveTile (preuves récentes) puis 3 expériences récentes, chacun avec « Voir tout ».
class PassportScreen extends ConsumerWidget {
  const PassportScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(passportProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Passeport'),
        actions: [
          IconButton(
              tooltip: 'Modifier mon profil',
              icon: const Icon(AppIcons.edit),
              onPressed: () => context.push('/passeport/profil')),
        ],
      ),
      body: AsyncValueView<Citoyen>(
        value: value,
        onRetry: () => ref.invalidate(passportProvider),
        data: (c) => RefreshIndicator(
          onRefresh: () async {
            ref.invalidate(competencesProvider);
            ref.invalidate(passportProvider);
            await ref.read(passportProvider.future);
          },
          child: _Content(citoyen: c),
        ),
      ),
    );
  }
}

class _Content extends ConsumerWidget {
  const _Content({required this.citoyen});
  final Citoyen citoyen;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final text = Theme.of(context).textTheme;
    final comps = ref.watch(competencesProvider);
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.all(20),
      children: [
        ProfileHeader(
          title: citoyen.nomComplet,
          subtitle: citoyen.commune?.fullLabel ?? 'Localisation à renseigner',
          codePasseport: citoyen.codePasseport,
          avatarUrl: citoyen.photoUrl,
        ),
        const SizedBox(height: 16),
        AppCard(
          onTap: () => context.push('/passeport/disponibilite'),
          child: Row(children: [
            Expanded(child: Text('Disponibilité', style: text.titleSmall)),
            DisponibiliteBadge(citoyen.disponibilite),
            const SizedBox(width: 8),
            const Icon(AppIcons.chevron, color: AppColors.muted),
          ]),
        ),
        const SizedBox(height: 16),
        const _CompletionCard(),
        const SizedBox(height: 16),
        AppButton(
          label: 'Générer mon CV PDF',
          icon: Icons.download_outlined,
          variant: AppButtonVariant.secondary,
          onPressed: () => context.push('/passeport/cv'),
        ),
        const SizedBox(height: 28),
        SectionHeader(
            title: 'Compétences',
            actionLabel: 'Voir tout',
            onAction: () => context.push('/competences')),
        comps.when(
          loading: () => const SizedBox(height: 80, child: LoadingView()),
          error: (e, _) => SizedBox(
              height: 160,
              child: ErrorView(
                  error: e,
                  onRetry: () => ref.invalidate(competencesProvider))),
          data: (items) => items.isEmpty
              ? Column(children: [
                  Text('Vous n\'avez pas encore de compétence.',
                      style: text.bodyMedium?.copyWith(color: AppColors.muted)),
                  const SizedBox(height: 12),
                  AppButton(
                      label: 'Ajouter une compétence',
                      variant: AppButtonVariant.secondary,
                      onPressed: () => context.push('/competences/ajouter')),
                ])
              : Column(children: [
                  for (final c in items.take(3)) ...[
                    CompetenceCard(
                        item: c,
                        onTap: () => context.push('/competences/${c.id}')),
                    const SizedBox(height: 12),
                  ],
                ]),
        ),
        const SizedBox(height: 16),
        SectionHeader(
            title: 'Preuves récentes',
            actionLabel: 'Voir tout',
            onAction: () => context.push('/preuves')),
        const _RecentProofs(),
        const SizedBox(height: 16),

        SectionHeader(title: 'Expériences', actionLabel: 'Voir tout', onAction: () => context.push('/experiences')),
        const _RecentExperiences(),
        const SizedBox(height: 16),

        SectionHeader(
            title: 'Localisation',
            actionLabel: 'Modifier',
            onAction: () => context.push('/passeport/localisation')),
        AppCard(
          child: Column(children: [
            InfoRow(
                label: 'Commune',
                value: citoyen.commune?.nom ?? 'Non renseignée',
                icon: AppIcons.location),
            InfoRow(
                label: 'Région',
                value: citoyen.commune?.regionNom ?? 'Non renseignée'),
          ]),
        ),
      ],
    );
  }
}

class _CompletionCard extends ConsumerWidget {
  const _CompletionCard();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final text = Theme.of(context).textTheme;
    final completion = ref.watch(profileCompletionProvider);
    return AppCard(
      child: completion.when(
        loading: () => const SizedBox(height: 56, child: LoadingView()),
        error: (e, _) => Text('Progression indisponible pour le moment.',
            style: text.bodyMedium),
        data: (c) =>
            Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          AppProgressBar(label: 'Passeport complété', value: c.ratio),
          const SizedBox(height: 12),
          if (c.isComplete)
            Text(
                'Votre passeport est complet. Continuez à l\'enrichir avec de nouvelles compétences.',
                style: text.bodySmall?.copyWith(color: AppColors.muted))
          else
            for (final s in c.steps) _StepRow(step: s),
        ]),
      ),
    );
  }
}

class _StepRow extends StatelessWidget {
  const _StepRow({required this.step});
  final CompletionStep step;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return InkWell(
      onTap: step.done ? null : () => context.push(step.route),
      borderRadius: BorderRadius.circular(8),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(children: [
          Icon(step.done ? AppIcons.selected : AppIcons.unselected,
              size: 22, color: step.done ? AppColors.green : AppColors.muted),
          const SizedBox(width: 12),
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(step.title, style: text.titleSmall),
              if (!step.done)
                Text(step.hint,
                    style: text.bodySmall?.copyWith(color: AppColors.muted)),
            ]),
          ),
          if (!step.done) const Icon(AppIcons.chevron, color: AppColors.muted),
        ]),
      ),
    );
  }
}

class _RecentProofs extends ConsumerWidget {
  const _RecentProofs();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(recentPreuvesProvider);
    return value.when(
      loading: () => const SizedBox(height: 80, child: LoadingView()),
      error: (e, _) => SizedBox(
          height: 160,
          child: ErrorView(
              error: e, onRetry: () => ref.invalidate(recentPreuvesProvider))),
      data: (items) => items.isEmpty
          ? Column(children: [
              Text("Aucune preuve pour l'instant.",
                  style: Theme.of(context)
                      .textTheme
                      .bodyMedium
                      ?.copyWith(color: AppColors.muted)),
              const SizedBox(height: 12),
              AppButton(
                  label: 'Ajouter une preuve',
                  variant: AppButtonVariant.secondary,
                  onPressed: () => context.push('/preuves/ajouter')),
            ])
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

class _RecentExperiences extends ConsumerWidget {
  const _RecentExperiences();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(recentExperiencesProvider);
    return value.when(
      loading: () => const SizedBox(height: 80, child: LoadingView()),
      error: (e, _) => SizedBox(height: 160, child: ErrorView(error: e, onRetry: () => ref.invalidate(recentExperiencesProvider))),
      data: (items) => items.isEmpty
          ? Column(children: [
              Text("Aucune expérience pour l'instant.", style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: AppColors.muted)),
              const SizedBox(height: 12),
              AppButton(label: 'Ajouter une expérience', variant: AppButtonVariant.secondary, onPressed: () => context.push('/experiences/nouvelle')),
            ])
          : Column(children: [
              for (final e in items) ...[
                ExperienceTile(
                  titre: e.titre,
                  entreprise: e.entreprise,
                  periodLabel: experiencePeriodLabel(e),
                  enCours: e.enCours,
                  reconversion: e.reconversion,
                  onTap: () => context.push('/experiences/${e.id}'),
                ),
                const SizedBox(height: 12),
              ],
            ]),
    );
  }
}