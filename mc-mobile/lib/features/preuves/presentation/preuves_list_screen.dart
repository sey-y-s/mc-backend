import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/preuves/presentation/preuve_providers.dart';
import 'package:mlc_mobile/shared/widgets/preuve_tile.dart';

/// Route : /preuves
class PreuvesListScreen extends ConsumerWidget {
  const PreuvesListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final revision = ref.watch(preuvesRevisionProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Mes preuves')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/preuves/ajouter'),
        icon: const Icon(AppIcons.add),
        label: const Text('Ajouter'),
      ),
      body: PaginatedListView<Preuve>(
        key: ValueKey(
            revision), // nouvelle clé après un ajout : la liste se recharge
        emptyMessage:
            "Aucune preuve pour l'instant.\nAjoutez un diplôme, un certificat ou une attestation.",
        fetchPage: (page, size) =>
            ref.read(preuveRepositoryProvider).listMine(page: page, size: size),
        itemBuilder: (_, p) => PreuveTile(
          typeLabel: p.type.label,
          competenceNom: p.competenceNom,
          dateLabel: formatDateFr(p.date),
          statut: p.statut,
          onTap: () => context.push('/preuves/${p.id}'),
        ),
      ),
    );
  }
}
