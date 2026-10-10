import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/experiences/data/api_experience_repository.dart';
import 'package:mlc_mobile/features/experiences/data/mock_experience_repository.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_repository.dart';

final experienceRepositoryProvider = Provider<ExperienceRepository>((ref) {
  return AppConfig.useMocks
      ? const MockExperienceRepository()
      : ApiExperienceRepository(
          ref.watch(dioProvider),
          ref.watch(tokenStorageProvider),
        );
});

/// Compteur incrémenté à chaque création, modification ou suppression : listes et blocs se rechargent.
final experiencesRevisionProvider = StateProvider<int>((ref) => 0);

final recentExperiencesProvider = FutureProvider.autoDispose<List<Experience>>((ref) {
  ref.watch(experiencesRevisionProvider);
  return ref.watch(experienceRepositoryProvider).list(size: 3);
});

/// Ne surveille PAS la révision : après une suppression, l'écran de détail ne doit pas se recharger
/// (il afficherait « introuvable » pendant sa fermeture). Après une modification, il est invalidé explicitement.
final experienceDetailProvider =
    FutureProvider.autoDispose.family<Experience, String>((ref, id) => ref.watch(experienceRepositoryProvider).get(id));

class ExperienceActions extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  ExperienceRepository get _repo => ref.read(experienceRepositoryProvider);

  /// Crée (id == null) ou modifie. Retourne true en cas de succès.
  Future<bool> save({String? id, required ExperienceInput input}) => _run(
        () async {
          if (id == null) {
            await _repo.create(input);
          } else {
            await _repo.update(id, input);
          }
        },
        invalidateDetailId: id,
      );

  Future<bool> delete(String id) => _run(() => _repo.delete(id));

  Future<bool> _run(Future<void> Function() action, {String? invalidateDetailId}) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard<void>(action);
    if (state.hasError) return false;
    if (invalidateDetailId != null) ref.invalidate(experienceDetailProvider(invalidateDetailId));
    ref.read(experiencesRevisionProvider.notifier).state++;
    return true;
  }
}

final experienceActionsProvider = AsyncNotifierProvider.autoDispose<ExperienceActions, void>(ExperienceActions.new);