import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_labels.dart';
import 'package:mlc_mobile/features/experiences/presentation/experience_providers.dart';
import 'package:mlc_mobile/shared/widgets/experience_tile.dart';

/// Route : /experiences
class ExperiencesListScreen extends ConsumerWidget {
  const ExperiencesListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final revision = ref.watch(experiencesRevisionProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Mes expériences')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/experiences/nouvelle'),
        icon: const Icon(AppIcons.add),
        label: const Text('Ajouter'),
      ),
      body: PaginatedListView<Experience>(
        key: ValueKey(revision), // nouvelle clé après une modification : la liste se recharge
        emptyMessage: "Aucune expérience pour l'instant.\nAjoutez votre parcours : emplois, apprentissages, reconversions.",
        fetchPage: (page, size) => ref.read(experienceRepositoryProvider).list(page: page, size: size),
        itemBuilder: (_, e) => ExperienceTile(
          titre: e.titre,
          entreprise: e.entreprise,
          periodLabel: experiencePeriodLabel(e),
          enCours: e.enCours,
          reconversion: e.reconversion,
          onTap: () => context.push('/experiences/${e.id}'),
        ),
      ),
    );
  }
}