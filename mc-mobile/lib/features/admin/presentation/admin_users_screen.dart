import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';

class AdminUsersScreen extends ConsumerWidget {
  const AdminUsersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final allowed = ref.watch(isSuperAdminProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Comptes et rôles')),
      body: allowed.when(
        loading: () => const LoadingView(),
        error: (error, _) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(isSuperAdminProvider),
        ),
        data: (isAllowed) => isAllowed
            ? _usersContent(context, ref)
            : const ErrorView(error: ForbiddenFailure()),
      ),
    );
  }

  Widget _usersContent(BuildContext context, WidgetRef ref) {
    final users = ref.watch(adminUsersProvider);
    return users.when(
      loading: () => const LoadingView(),
      error: (error, _) => ErrorView(
        error: error,
        onRetry: () => ref.invalidate(adminUsersProvider),
      ),
      data: (items) => RefreshIndicator(
        onRefresh: () => ref.refresh(adminUsersProvider.future),
        child: items.isEmpty
            ? const EmptyView(message: 'Aucun compte à afficher.')
            : ListView.separated(
                padding: const EdgeInsets.all(16),
                itemCount: items.length,
                separatorBuilder: (_, _) => const SizedBox(height: 10),
                itemBuilder: (context, index) {
                  final user = items[index];
                  return AppCard(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          user.email?.isNotEmpty == true
                              ? user.email!
                              : (user.telephone ?? 'Compte'),
                          style: const TextStyle(fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          user.actif ? 'Compte actif' : 'Compte désactivé',
                          style: const TextStyle(color: AppColors.muted),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Rôle : ${_roleLabel(user.role)}',
                          style: const TextStyle(color: AppColors.muted),
                        ),
                      ],
                    ),
                  );
                },
              ),
      ),
    );
  }

  static String _roleLabel(String role) => switch (role) {
    'CITOYEN' => 'Citoyen',
    'ORGANISATION' => 'Organisation',
    'EVALUATEUR' => 'Évaluateur',
    'ADMIN' => 'Administrateur',
    'SUPER_ADMIN' => 'Super-administrateur',
    _ => role,
  };
}
