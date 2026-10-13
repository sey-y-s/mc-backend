import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/preuves/presentation/preuve_providers.dart';
import 'package:mlc_mobile/features/validations/data/api_validation_repository.dart';
import 'package:mlc_mobile/features/validations/data/mock_validation_repository.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_repository.dart';

final validationRepositoryProvider = Provider<ValidationRepository>((ref) {
  return AppConfig.useMocks ? const MockValidationRepository() : ApiValidationRepository(ref.watch(dioProvider));
});

/// Compteur incrémenté à chaque demande : tout ce qui l'observe se recharge (même principe que les preuves).
final validationsRevisionProvider = StateProvider<int>((ref) => 0);

final validationsForCompetenceProvider = FutureProvider.autoDispose.family<List<Validation>, String>((ref, citoyenCompetenceId) {
  ref.watch(validationsRevisionProvider);
  return ref.watch(validationRepositoryProvider).listForCompetence(citoyenCompetenceId);
});

final validationDetailProvider = FutureProvider.autoDispose.family<Validation, String>((ref, id) {
  ref.watch(validationsRevisionProvider);
  return ref.watch(validationRepositoryProvider).get(id);
});

class RequestValidationController extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  /// Retourne true si la demande est envoyée. Le statut de la preuve change : on recharge aussi les preuves.
  Future<bool> submit({required String citoyenCompetenceId, required String preuveId}) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard<void>(() async {
      await ref.read(validationRepositoryProvider).request(citoyenCompetenceId: citoyenCompetenceId, preuveId: preuveId);
    });
    if (state.hasError) return false;
    ref.read(validationsRevisionProvider.notifier).state++;
    ref.read(preuvesRevisionProvider.notifier).state++;
    ref.invalidate(competenceDetailProvider(citoyenCompetenceId));
    return true;
  }
}

final requestValidationControllerProvider =
    AsyncNotifierProvider.autoDispose<RequestValidationController, void>(RequestValidationController.new);