import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';

class AdminRouteGuard extends ConsumerWidget {
  const AdminRouteGuard({
    super.key,
    required this.child,
    this.superAdminOnly = false,
  });

  final Widget child;
  final bool superAdminOnly;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final role = ref.watch(currentRoleProvider);
    return role.when(
      loading: () => const LoadingView(),
      error: (error, _) => ErrorView(
        error: error,
        onRetry: () => ref.invalidate(currentRoleProvider),
      ),
      data: (value) {
        final isSuperAdmin = value == 'SUPER_ADMIN';
        final allowed = isSuperAdmin ||
            (!superAdminOnly && value == 'ADMIN');
        return allowed
            ? child
            : const ErrorView(error: ForbiddenFailure());
      },
    );
  }
}
