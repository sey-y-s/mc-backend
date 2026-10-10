import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/mise_en_relation/presentation/mise_en_relation_providers.dart';
import 'package:mlc_mobile/features/talents/domain/talent_models.dart';
import 'package:mlc_mobile/features/talents/presentation/talent_providers.dart';

class TalentProfileScreen extends ConsumerWidget {
  const TalentProfileScreen({super.key, this.id});
  final String? id;
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final talentId = id;
    if (talentId == null || talentId.isEmpty) {
      return const Scaffold(body: ErrorView(error: NotFoundFailure()));
    }
    final value = ref.watch(talentProfileProvider(talentId));
    return Scaffold(
      appBar: AppBar(title: const Text('Profil anonymisé')),
      body: AsyncValueView<TalentProfileAnonymized>(
        value: value,
        onRetry: () => ref.invalidate(talentProfileProvider(talentId)),
        data: (profile) {
          final talent = profile.summary;
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              AppCard(
                child: Row(
                  children: [
                    const CircleAvatar(
                      radius: 28,
                      backgroundColor: AppColors.greenSoft,
                      child: Icon(
                        AppIcons.person,
                        color: AppColors.green,
                        size: 30,
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Profil anonymisé',
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                          Text(
                            '${talent.communeName}, ${talent.regionName}',
                            style: const TextStyle(color: AppColors.muted),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              Text(
                'Compétences',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 8),
              for (final skill in talent.skills)
                AppCard(
                  padding: const EdgeInsets.all(14),
                  child: Row(
                    children: [
                      const Icon(AppIcons.competences, color: AppColors.green),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              skill.name,
                              style: const TextStyle(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            Text(
                              'Niveau : ${skill.level.label}',
                              style: const TextStyle(
                                color: AppColors.muted,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),
                      if (skill.validated)
                        const Icon(
                          AppIcons.validations,
                          color: AppColors.green,
                        ),
                    ],
                  ),
                ),
              const SizedBox(height: 12),
              AppCard(
                child: Column(
                  children: [
                    _CountRow(
                      label: 'Disponibilité',
                      value: talent.availability.label,
                    ),
                    _CountRow(
                      label: 'Validations',
                      value: talent.validationCount.toString(),
                    ),
                    _CountRow(
                      label: 'Preuves',
                      value: talent.evidenceCount.toString(),
                    ),
                    _CountRow(
                      label: 'Portfolio',
                      value: talent.hasPortfolio ? 'Présent' : 'Non renseigné',
                    ),
                  ],
                ),
              ),
              if (profile.portfolioTitles.isNotEmpty) ...[
                const SizedBox(height: 16),
                Text(
                  'Réalisations présentées',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                for (final title in profile.portfolioTitles)
                  ListTile(
                    leading: const Icon(AppIcons.portfolio),
                    title: Text(title),
                  ),
              ],
              const SizedBox(height: 20),
              AppButton(
                label: 'Demander une mise en relation',
                icon: AppIcons.relations,
                onPressed: () async {
                  final message = await showDialog<String>(
                    context: context,
                    builder: (ctx) {
                      final controller = TextEditingController();
                      return AlertDialog(
                        title: const Text('Demande de mise en relation'),
                        content: TextField(
                          controller: controller,
                          maxLines: 3,
                          decoration: const InputDecoration(
                            labelText: 'Message (facultatif)',
                          ),
                        ),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.pop(ctx),
                            child: const Text('Annuler'),
                          ),
                          FilledButton(
                            onPressed: () =>
                                Navigator.pop(ctx, controller.text),
                            child: const Text('Envoyer'),
                          ),
                        ],
                      );
                    },
                  );
                  if (message == null || !context.mounted) return;
                  try {
                    await ref
                        .read(miseEnRelationRepositoryProvider)
                        .send(talentId, message: message);
                    ref.read(relationsRevisionProvider.notifier).state++;
                    if (context.mounted) {
                      showSuccess(context, 'Demande envoyée.');
                    }
                  } catch (e) {
                    if (context.mounted) showError(context, failureMessage(e));
                  }
                },
              ),
            ],
          );
        },
      ),
    );
  }
}

class _CountRow extends StatelessWidget {
  const _CountRow({required this.label, required this.value});
  final String label, value;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 7),
    child: Row(
      children: [
        Expanded(child: Text(label)),
        Text(value, style: const TextStyle(fontWeight: FontWeight.w600)),
      ],
    ),
  );
}
