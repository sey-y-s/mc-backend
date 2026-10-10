import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_providers.dart';
import 'package:mlc_mobile/shared/widgets/validation_status_badge.dart';

/// Route : /validations/historique. Lecture seule : chaque entrée raconte la demande puis la décision.
class ValidationsHistoryScreen extends ConsumerWidget {
  const ValidationsHistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final revision = ref.watch(validationsRevisionProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Historique')),
      body: PaginatedListView<Validation>(
        key: ValueKey(revision),
        emptyMessage: "Aucun historique pour l'instant.",
        fetchPage: (page, size) => ref.read(validationRepositoryProvider).listMine(page: page, size: size),
        itemBuilder: (_, v) => _HistoryEntry(validation: v),
      ),
    );
  }
}

class _HistoryEntry extends StatelessWidget {
  const _HistoryEntry({required this.validation});
  final Validation validation;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final muted = text.bodySmall?.copyWith(color: AppColors.muted);
    final v = validation;
    return AppCard(
      accentColor: validationAccentColor(v.statut),
      onTap: () => context.push('/validations/${v.id}'),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(v.competenceNom, style: text.titleSmall),
              Text(v.type.label, style: muted),
            ]),
          ),
          ValidationStatusBadge(v.statut),
        ]),
        const SizedBox(height: 10),
        Text('Demande envoyée le ${formatDateFr(v.dateDemande)}', style: text.bodySmall),
        if (v.dateDecision != null)
          Text(
            '${v.statut.label} le ${formatDateFr(v.dateDecision!)}${v.validateurLabel != null ? ' par ${v.validateurLabel}' : ''}',
            style: text.bodySmall,
          ),
      ]),
    );
  }
}