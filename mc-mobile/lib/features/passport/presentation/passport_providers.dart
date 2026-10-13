import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/passport/data/api_passport_repository.dart';
import 'package:mlc_mobile/features/passport/data/mock_passport_repository.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/domain/passport_repository.dart';
import 'package:mlc_mobile/features/passport/domain/profile_completion.dart';

final passportRepositoryProvider = Provider<PassportRepository>((ref) {
  return AppConfig.useMocks
      ? const MockPassportRepository()
      : ApiPassportRepository(ref.watch(dioProvider));
});

final passportProvider = FutureProvider.autoDispose<Citoyen>(
    (ref) => ref.watch(passportRepositoryProvider).getMine());

/// Régions et communes changent rarement : pas d'autoDispose, elles restent en mémoire (moins d'appels réseau).
final regionsProvider = FutureProvider<List<Region>>(
    (ref) => ref.watch(passportRepositoryProvider).listRegions());

final communesProvider = FutureProvider.family<List<Commune>, String>(
    (ref, regionId) =>
        ref.watch(passportRepositoryProvider).listCommunes(regionId));

/// Complétude = passeport + compétences. Chargement/erreur si l'une des deux sources n'est pas prête.
final profileCompletionProvider =
    Provider.autoDispose<AsyncValue<ProfileCompletion>>((ref) {
  final citoyen = ref.watch(passportProvider);
  final comps = ref.watch(competencesProvider);
  if (citoyen.hasError) {
    return AsyncError(citoyen.error!, citoyen.stackTrace ?? StackTrace.current);
  }
  if (comps.hasError) {
    return AsyncError(comps.error!, comps.stackTrace ?? StackTrace.current);
  }
  if (!citoyen.hasValue || !comps.hasValue) return const AsyncLoading();
  return AsyncData(
      ProfileCompletion.compute(citoyen.requireValue, comps.requireValue));
});

/// Progression (0..1) de l'envoi de la photo ; null = aucun envoi.
final photoProgressProvider = StateProvider.autoDispose<double?>((ref) => null);

/// Actions d'écriture du passeport. Chaque action retourne true si elle a réussi (le passeport est alors rechargé).
class PassportActions extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  Future<bool> _run(Future<void> Function() action) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard<void>(action);
    if (state.hasError) return false;
    ref.invalidate(passportProvider);
    return true;
  }

  PassportRepository get _repo => ref.read(passportRepositoryProvider);

  Future<bool> updateProfile(
          {required String nom, required String prenom, Sexe? sexe}) =>
      _run(() => _repo.updateProfile(nom: nom, prenom: prenom, sexe: sexe));

  Future<bool> updateCommune(String communeId) =>
      _run(() => _repo.updateCommune(communeId));

  Future<bool> updateAvailability(Disponibilite value) =>
      _run(() => _repo.updateAvailability(value));

  Future<bool> updatePhoto(PickedMedia media) => _run(() async {
        ref.read(photoProgressProvider.notifier).state = 0;
        await _repo.updatePhoto(media,
            onProgress: (p) =>
                ref.read(photoProgressProvider.notifier).state = p);
      });
}

final passportActionsProvider =
    AsyncNotifierProvider.autoDispose<PassportActions, void>(
        PassportActions.new);