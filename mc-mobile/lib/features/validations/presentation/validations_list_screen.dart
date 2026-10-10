import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/paginated_list_view.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_labels.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_providers.dart';
import 'package:mlc_mobile/shared/widgets/validation_tile.dart';

/// Route : /validations
class ValidationsListScreen extends ConsumerStatefulWidget {
  const ValidationsListScreen({super.key});

  @override
  ConsumerState<ValidationsListScreen> createState() => _ValidationsListScreenState();
}

class _ValidationsListScreenState extends ConsumerState<ValidationsListScreen> {
  ValidationStatut? _statut;

  @override
  Widget build(BuildContext context) {
    final revision = ref.watch(validationsRevisionProvider);
    final selectedStatus = _StatusOption.forStatus(_statut);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mes validations'),
        actions: [
          IconButton(tooltip: 'Historique', icon: const Icon(AppIcons.history), onPressed: () => context.push('/validations/historique')),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/validations/demander'),
        icon: const Icon(AppIcons.add),
        label: const Text('Demander'),
      ),
      body: Column(children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 10),
          child: Material(
            color: AppColors.card,
            borderRadius: BorderRadius.circular(16),
            child: InkWell(
              borderRadius: BorderRadius.circular(16),
              onTap: () => _chooseStatus(context, selectedStatus),
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 13,
                ),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppColors.border),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 42,
                      height: 42,
                      decoration: BoxDecoration(
                        color: AppColors.greenSoft,
                        borderRadius: BorderRadius.circular(13),
                      ),
                      child: Icon(
                        selectedStatus.icon,
                        color: AppColors.green,
                        size: 22,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Explorer par statut',
                            style: TextStyle(
                              color: AppColors.muted,
                              fontSize: 11,
                            ),
                          ),
                          const SizedBox(height: 3),
                          Text(
                            selectedStatus.label,
                            style: const TextStyle(
                              color: AppColors.anthracite,
                              fontWeight: FontWeight.w700,
                              fontSize: 15,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const Icon(AppIcons.expand, color: AppColors.green),
                  ],
                ),
              ),
            ),
          ),
        ),
        Expanded(
          child: PaginatedListView<Validation>(
            key: ValueKey('$revision-${_statut?.apiCode}'),
            emptyMessage: _statut == null
                ? "Aucune validation pour l'instant.\nAjoutez une preuve, puis demandez une validation."
                : 'Aucune validation « ${_statut!.label.toLowerCase()} ».',
            fetchPage: (page, size) => ref.read(validationRepositoryProvider).listMine(statut: _statut, page: page, size: size),
            itemBuilder: (_, v) => ValidationTile(
              competenceNom: v.competenceNom,
              typeLabel: v.type.label,
              statut: v.statut,
              dateLabel: validationDateLabel(v),
              validateurLabel: v.validateurLabel,
              onTap: () => context.push('/validations/${v.id}'),
            ),
          ),
        ),
      ]),
    );
  }

  Future<void> _chooseStatus(
    BuildContext context,
    _StatusOption selected,
  ) async {
    final choice = await showModalBottomSheet<_StatusOption>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) =>
          _ValidationStatusSheet(selected: selected, options: _StatusOption.all),
    );
    if (choice != null) setState(() => _statut = choice.statut);
  }
}

class _StatusOption {
  const _StatusOption({
    required this.statut,
    required this.label,
    required this.icon,
  });

  static const all = [
    _StatusOption(
      statut: null,
      label: 'Toutes',
      icon: AppIcons.validations,
    ),
    _StatusOption(
      statut: ValidationStatut.approuvee,
      label: 'Approuvées',
      icon: AppIcons.check,
    ),
    _StatusOption(
      statut: ValidationStatut.enAttente,
      label: 'En attente',
      icon: AppIcons.clock,
    ),
    _StatusOption(
      statut: ValidationStatut.rejetee,
      label: 'Rejetées',
      icon: AppIcons.close,
    ),
  ];

  static _StatusOption forStatus(ValidationStatut? statut) =>
      all.firstWhere((option) => option.statut == statut);

  final ValidationStatut? statut;
  final String label;
  final IconData icon;
}

class _ValidationStatusSheet extends StatelessWidget {
  const _ValidationStatusSheet({
    required this.selected,
    required this.options,
  });

  final _StatusOption selected;
  final List<_StatusOption> options;

  @override
  Widget build(BuildContext context) => SafeArea(
    top: false,
    child: Padding(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Center(
            child: Container(
              width: 38,
              height: 4,
              decoration: BoxDecoration(
                color: AppColors.border,
                borderRadius: BorderRadius.circular(4),
              ),
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            'Choisir un statut',
            style: TextStyle(
              color: AppColors.anthracite,
              fontSize: 19,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 5),
          const Text(
            'Affinez la liste selon l’avancement de vos demandes.',
            style: TextStyle(color: AppColors.muted, fontSize: 13),
          ),
          const SizedBox(height: 18),
          GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 10,
            crossAxisSpacing: 10,
            childAspectRatio: 2.5,
            children: [
              for (final option in options)
                _ValidationStatusTile(
                  option: option,
                  selected: option.statut == selected.statut,
                  onTap: () => Navigator.of(context).pop(option),
                ),
            ],
          ),
        ],
      ),
    ),
  );
}

class _ValidationStatusTile extends StatelessWidget {
  const _ValidationStatusTile({
    required this.option,
    required this.selected,
    required this.onTap,
  });

  final _StatusOption option;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final foreground = selected ? AppColors.greenDark : AppColors.anthracite;
    return Material(
      color: selected ? AppColors.greenSoft : AppColors.surface,
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(
              color: selected ? AppColors.green : AppColors.border,
              width: selected ? 1.5 : 1,
            ),
          ),
          child: Row(
            children: [
              Icon(option.icon, size: 20, color: AppColors.green),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  option.label,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    color: foreground,
                    fontSize: 12,
                    fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                  ),
                ),
              ),
              if (selected)
                const Icon(AppIcons.check, size: 16, color: AppColors.green),
            ],
          ),
        ),
      ),
    );
  }
}