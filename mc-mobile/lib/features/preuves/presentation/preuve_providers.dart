import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/competences/presentation/competences_providers.dart';
import 'package:mlc_mobile/features/preuves/data/api_preuve_repository.dart';
import 'package:mlc_mobile/features/preuves/data/mock_preuve_repository.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_repository.dart';

final preuveRepositoryProvider = Provider<PreuveRepository>((ref) {
  return AppConfig.useMocks
      ? const MockPreuveRepository()
      : ApiPreuveRepository(
          ref.watch(dioProvider),
          ref.watch(tokenStorageProvider),
        );
});

/// Compteur incrémenté à chaque ajout : tout ce qui l'observe se recharge tout seul
/// (liste, blocs du passeport et du détail de compétence). Un seul signal, pas d'invalidations dispersées.
final preuvesRevisionProvider = StateProvider<int>((ref) => 0);

final recentPreuvesProvider = FutureProvider.autoDispose<List<Preuve>>((ref) {
  ref.watch(preuvesRevisionProvider);
  return ref.watch(preuveRepositoryProvider).listMine(size: 3);
});

final preuvesForCompetenceProvider = FutureProvider.autoDispose
    .family<List<Preuve>, String>((ref, citoyenCompetenceId) {
  ref.watch(preuvesRevisionProvider);
  return ref
      .watch(preuveRepositoryProvider)
      .listForCompetence(citoyenCompetenceId);
});

final preuveDetailProvider = FutureProvider.autoDispose.family<Preuve, String>(
    (ref, id) => ref.watch(preuveRepositoryProvider).get(id));

/// Progression (0..1) de l'envoi en cours ; null = aucun envoi.
final preuveProgressProvider =
    StateProvider.autoDispose<double?>((ref) => null);

class AddPreuveController extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  /// Retourne true si l'ajout a réussi. La compétence peut passer à « attestée » (décidé par le serveur) :
  /// on recharge donc aussi les compétences.
  Future<bool> submit(
      {required String citoyenCompetenceId,
      required PreuveType type,
      required PickedMedia media}) async {
    state = const AsyncLoading();
    final progress = ref.read(preuveProgressProvider.notifier);
    progress.state = 0;
    state = await AsyncValue.guard<void>(() async {
      await ref.read(preuveRepositoryProvider).add(
            citoyenCompetenceId: citoyenCompetenceId,
            type: type,
            fichier: media,
            onProgress: (p) => progress.state = p,
          );
    });
    if (state.hasError) {
      progress.state = null;
      return false;
    }
    ref.read(preuvesRevisionProvider.notifier).state++;
    ref.invalidate(competencesProvider);
    ref.invalidate(competenceDetailProvider(citoyenCompetenceId));
    return true;
  }
}

final addPreuveControllerProvider =
    AsyncNotifierProvider.autoDispose<AddPreuveController, void>(
        AddPreuveController.new);
