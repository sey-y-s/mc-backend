import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/admin/domain/admin_models.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';

class AdminOrganizationsScreen extends ConsumerWidget {
  const AdminOrganizationsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final organizations = ref.watch(adminOrganizationsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Organisations')),
      body: organizations.when(
        loading: () => const LoadingView(),
        error: (error, _) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(adminOrganizationsProvider),
        ),
        data: (items) => RefreshIndicator(
          onRefresh: () => ref.refresh(adminOrganizationsProvider.future),
          child: items.isEmpty
              ? const EmptyView(message: 'Aucune organisation enregistrée.')
              : ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: items.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 10),
                  itemBuilder: (context, index) =>
                      _OrganizationCard(item: items[index]),
                ),
        ),
      ),
    );
  }
}

class _OrganizationCard extends StatelessWidget {
  const _OrganizationCard({required this.item});

  final AdminOrganization item;

  @override
  Widget build(BuildContext context) => AppCard(
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(item.name, style: const TextStyle(fontWeight: FontWeight.w700)),
        if (item.email?.isNotEmpty == true) ...[
          const SizedBox(height: 4),
          Text(item.email!, style: const TextStyle(color: AppColors.muted)),
        ],
        if (item.description?.isNotEmpty == true) ...[
          const SizedBox(height: 8),
          Text(item.description!),
        ],
        const SizedBox(height: 8),
        Text(
          'Statut : ${_OrganizationStatus.labels[item.status] ?? item.status}',
          style: const TextStyle(color: AppColors.muted),
        ),
      ],
    ),
  );
}

class _OrganizationStatus {
  static const labels = {
    'EN_ATTENTE': 'En attente',
    'APPROUVEE': 'Approuvée',
    'REJETEE': 'Rejetée',
    'SUSPENDUE': 'Suspendue',
  };
}
