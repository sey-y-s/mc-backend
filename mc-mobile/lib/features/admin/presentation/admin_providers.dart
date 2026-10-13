import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/jwt_utils.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/admin/data/api_admin_repository.dart';
import 'package:mlc_mobile/features/admin/domain/admin_models.dart';

final apiAdminRepositoryProvider = Provider<ApiAdminRepository>(
  (ref) => ApiAdminRepository(ref.watch(dioProvider)),
);

final currentRoleProvider = FutureProvider<String?>((ref) async {
  ref.watch(sessionProvider);
  final token = await ref.watch(tokenStorageProvider).readAccess();
  return JwtUtils.role(token);
});

final currentUserIdProvider = FutureProvider<String?>((ref) async {
  ref.watch(sessionProvider);
  final token = await ref.watch(tokenStorageProvider).readAccess();
  return JwtUtils.subject(token);
});

final isAdminProvider = FutureProvider<bool>((ref) async {
  final role = await ref.watch(currentRoleProvider.future);
  return role == 'ADMIN' || role == 'SUPER_ADMIN';
});

final isSuperAdminProvider = FutureProvider<bool>((ref) async {
  return await ref.watch(currentRoleProvider.future) == 'SUPER_ADMIN';
});

final adminRevisionProvider = StateProvider<int>((ref) => 0);

final adminDashboardProvider = FutureProvider.autoDispose<AdminDashboard>((ref) {
  ref.watch(adminRevisionProvider);
  return ref.watch(apiAdminRepositoryProvider).dashboard();
});

final adminUsersProvider = FutureProvider.autoDispose<List<AdminUser>>((ref) {
  ref.watch(adminRevisionProvider);
  return ref.watch(apiAdminRepositoryProvider).users();
});

final adminValidationsProvider =
    FutureProvider.autoDispose<List<AdminValidation>>((ref) {
      ref.watch(adminRevisionProvider);
      return ref.watch(apiAdminRepositoryProvider).validations();
    });

final adminOrganizationsProvider =
    FutureProvider.autoDispose<List<AdminOrganization>>((ref) {
      ref.watch(adminRevisionProvider);
      return ref.watch(apiAdminRepositoryProvider).organizations();
    });

final adminTestsProvider = FutureProvider.autoDispose<List<AdminQcmTest>>((ref) {
  ref.watch(adminRevisionProvider);
  return ref.watch(apiAdminRepositoryProvider).tests();
});

final adminCompetencesProvider =
    FutureProvider.autoDispose<List<AdminCompetence>>((ref) {
      return ref.watch(apiAdminRepositoryProvider).competences();
    });

final adminOpportunitiesProvider =
    FutureProvider.autoDispose<List<AdminOpportunityDraft>>((ref) {
      ref.watch(adminRevisionProvider);
      return ref.watch(apiAdminRepositoryProvider).opportunities();
    });

final adminOpportunityCategoriesProvider =
    FutureProvider.autoDispose<List<AdminOpportunityCategory>>((ref) {
      return ref.watch(apiAdminRepositoryProvider).opportunityCategories();
    });
