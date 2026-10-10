import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';

class AdminHomeScreen extends ConsumerWidget {
  const AdminHomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final access = ref.watch(isAdminProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Administration')),
      body: access.when(
        loading: () => const LoadingView(),
        error: (error, _) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(isAdminProvider),
        ),
        data: (allowed) =>
            allowed ? const _AdminDashboardBody() : const _ForbiddenAdminView(),
      ),
    );
  }
}

class _AdminDashboardBody extends ConsumerWidget {
  const _AdminDashboardBody();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dashboard = ref.watch(adminDashboardProvider);
    final superAdmin = ref.watch(isSuperAdminProvider).value ?? false;
    return dashboard.when(
      loading: () => const LoadingView(),
      error: (error, _) => ErrorView(
        error: error,
        onRetry: () => ref.invalidate(adminDashboardProvider),
      ),
      data: (summary) => RefreshIndicator(
        onRefresh: () => ref.refresh(adminDashboardProvider.future),
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [AppColors.greenDark, AppColors.green],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(20),
              ),
              child: const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'ESPACE ADMINISTRATEUR',
                    style: TextStyle(
                      color: AppColors.goldSoft,
                      fontSize: 11,
                      fontWeight: FontWeight.w700,
                      letterSpacing: 1.3,
                    ),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Pilotez la plateforme.',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 22,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  SizedBox(height: 6),
                  Text(
                    'Consultez les informations et l’état de la plateforme.',
                    style: TextStyle(color: Color(0xD1FFFFFF)),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                _Metric(label: 'Citoyens', value: summary.citoyens),
                _Metric(label: 'Organisations', value: summary.organisations),
                _Metric(label: 'Validations', value: summary.validations),
                _Metric(label: 'Tests', value: summary.tests),
              ],
            ),
            const SizedBox(height: 20),
            Text('Gestion', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 10),
            _AdminAction(
              title: 'Opportunités',
              detail: 'Consulter les opportunités publiées et leurs statuts',
              onTap: () => context.push('/admin/opportunites'),
            ),
            _AdminAction(
              title: 'Tests numériques',
              detail: 'Consulter les tests QCM et leurs statuts',
              onTap: () => context.push('/admin/tests'),
            ),
            _AdminAction(
              title: 'Organisations',
              detail: 'Consulter les organisations et leur statut',
              onTap: () => context.push('/admin/organisations'),
            ),
            _AdminAction(
              title: 'Validations',
              detail: 'Consulter les demandes de validation',
              onTap: () => context.push('/admin/validations'),
            ),
            if (superAdmin)
              _AdminAction(
                title: 'Comptes et rôles',
                detail: 'Consulter les comptes et leurs rôles',
                onTap: () => context.push('/admin/utilisateurs'),
              ),
          ],
        ),
      ),
    );
  }
}

class _Metric extends StatelessWidget {
  const _Metric({required this.label, required this.value});

  final String label;
  final int value;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: (MediaQuery.sizeOf(context).width - 42) / 2,
    child: AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            value.toString(),
            style: Theme.of(context).textTheme.headlineSmall
                ?.copyWith(color: AppColors.green),
          ),
          Text(label, style: const TextStyle(color: AppColors.muted)),
        ],
      ),
    ),
  );
}

class _AdminAction extends StatelessWidget {
  const _AdminAction({
    required this.title,
    required this.detail,
    required this.onTap,
  });

  final String title;
  final String detail;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => AppCard(
    onTap: onTap,
    child: ListTile(
      contentPadding: EdgeInsets.zero,
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
      subtitle: Text(detail),
      trailing: const Icon(Icons.chevron_right),
    ),
  );
}

class _ForbiddenAdminView extends StatelessWidget {
  const _ForbiddenAdminView();

  @override
  Widget build(BuildContext context) =>
      const ErrorView(error: ForbiddenFailure());
}
