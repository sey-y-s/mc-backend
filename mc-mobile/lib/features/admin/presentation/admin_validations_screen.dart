import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/admin/domain/admin_models.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';

class AdminValidationsScreen extends ConsumerStatefulWidget {
  const AdminValidationsScreen({super.key});

  @override
  ConsumerState<AdminValidationsScreen> createState() =>
      _AdminValidationsScreenState();
}

class _AdminValidationsScreenState
    extends ConsumerState<AdminValidationsScreen> {
  String? _status;
  ValidationType? _type;

  static const _statuses = <String, String>{
    'EN_ATTENTE': 'En attente',
    'APPROUVEE': 'Approuvées',
    'REJETEE': 'Rejetées',
  };
  static const _types = <ValidationType>[
    ValidationType.diplomeVerifie,
    ValidationType.partenaireAtteste,
    ValidationType.validationPratique,
    ValidationType.testNumerique,
    ValidationType.vae,
  ];

  @override
  Widget build(BuildContext context) {
    final validations = ref.watch(adminValidationsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Demandes de validation')),
      body: Column(
        children: [
          _filterRow(
            label: 'Statut',
            chips: [
              _statusChip(null, 'Tous'),
              for (final entry in _statuses.entries)
                _statusChip(entry.key, entry.value),
            ],
          ),
          _filterRow(
            label: 'Type de validation',
            chips: [
              _typeChip(null, 'Tous'),
              for (final type in _types) _typeChip(type, type.label),
            ],
          ),
          Expanded(
            child: validations.when(
              loading: () => const LoadingView(),
              error: (error, _) => ErrorView(
                error: error,
                onRetry: () => ref.invalidate(adminValidationsProvider),
              ),
              data: (items) {
                final filtered = items
                    .where(
                      (item) =>
                          (_status == null || item.status == _status) &&
                          (_type == null ||
                              item.type == _type!.apiCode),
                    )
                    .toList();
                return RefreshIndicator(
                  onRefresh: () => ref.refresh(adminValidationsProvider.future),
                  child: filtered.isEmpty
                      ? const EmptyView(
                          message: 'Aucune demande pour ce statut.',
                        )
                      : ListView.separated(
                          padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                          itemCount: filtered.length,
                          separatorBuilder: (_, _) =>
                              const SizedBox(height: 10),
                          itemBuilder: (context, index) =>
                              _validationCard(context, filtered[index]),
                        ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _filterRow({required String label, required List<Widget> chips}) =>
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 4, 16, 2),
            child: Text(
              label,
              style: const TextStyle(
                color: AppColors.muted,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(children: chips),
          ),
        ],
      );

  Widget _statusChip(String? value, String label) => Padding(
    padding: const EdgeInsets.only(right: 8),
    child: ChoiceChip(
      label: Text(label),
      selected: _status == value,
      onSelected: (_) => setState(() => _status = value),
    ),
  );

  Widget _typeChip(ValidationType? value, String label) => Padding(
    padding: const EdgeInsets.only(right: 8),
    child: ChoiceChip(
      label: Text(label),
      selected: _type == value,
      onSelected: (_) => setState(() => _type = value),
    ),
  );

  Widget _validationCard(BuildContext context, AdminValidation item) {
    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  item.label.isEmpty ? 'Validation ${item.id}' : item.label,
                  style: const TextStyle(fontWeight: FontWeight.w700),
                ),
              ),
            ],
          ),
          Text(
            '${_typeLabel(item.type)} · ${_statuses[item.status] ?? item.status}',
            style: const TextStyle(color: AppColors.muted),
          ),
        ],
      ),
    );
  }

  static String _typeLabel(String type) => switch (type) {
    'DIPLOME_VERIFIE' => 'Diplôme vérifié',
    'PARTENAIRE_ATTESTE' => 'Attestation de partenaire',
    'VALIDATION_PRATIQUE' => 'Évaluation pratique',
    'TEST_NUMERIQUE' => 'Test numérique',
    'VAE' => 'Validation des acquis',
    _ => type,
  };
}
