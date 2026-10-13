import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/shared/widgets/competence_card.dart';

class CompetencesListScreen extends ConsumerWidget {
  const CompetencesListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(competencesProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Mes compétences')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/competences/ajouter'),
        icon: const Icon(AppIcons.add),
        label: const Text('Ajouter'),
      ),
      // TODO: ajouter une barre de recherche locale (filtre par nom) au-dessus de la liste.
      body: AsyncValueView<List<CitoyenCompetence>>(
        value: value,
        onRetry: () => ref.invalidate(competencesProvider),
        isEmpty: (d) => d.isEmpty,
        emptyMessage:
            'Vous n\'avez pas encore de compétence.\nAjoutez la première !',
        data: (items) => RefreshIndicator(
          onRefresh: () async => ref.refresh(competencesProvider.future),
          child: ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: items.length,
            separatorBuilder: (_, _) => const SizedBox(height: 12),
            itemBuilder: (_, i) => CompetenceCard(
              item: items[i],
              onTap: () => context.push('/competences/${items[i].id}'),
            ),
          ),
        ),
      ),
    );
  }
}
