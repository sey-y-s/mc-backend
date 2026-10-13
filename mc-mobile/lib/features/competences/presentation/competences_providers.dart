import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/competences/data/api_competences_repository.dart';
import 'package:mlc_mobile/features/competences/data/mock_competences_repository.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/domain/competence_ref.dart';
import 'package:mlc_mobile/features/competences/domain/competences_repository.dart';

final competencesRepositoryProvider = Provider<CompetencesRepository>((ref) {
  return AppConfig.useMocks
      ? const MockCompetencesRepository()
      : ApiCompetencesRepository(
          ref.watch(dioProvider),
          ref.watch(tokenStorageProvider),
        );
});

// Première page seulement pour l'instant. TODO(PISTE A): AsyncNotifier paginé (scroll) quand les listes dépassent 20.
final competencesProvider = FutureProvider.autoDispose<List<CitoyenCompetence>>(
  (ref) => ref.watch(competencesRepositoryProvider).list(),
);

final competenceDetailProvider =
    FutureProvider.autoDispose.family<CitoyenCompetence, String>(
  (ref, id) => ref.watch(competencesRepositoryProvider).get(id),
);

/// Recherche dans le référentiel avec anti-rebond (400 ms)
final referentielSearchProvider = FutureProvider.autoDispose
    .family<List<CompetenceRef>, String>((ref, query) async {
  var disposed = false;
  ref.onDispose(() => disposed = true);
  await Future<void>.delayed(const Duration(milliseconds: 400));
  if (disposed) return const <CompetenceRef>[];
  return ref.read(competencesRepositoryProvider).searchReferentiel(query);
});

class AddCompetenceController extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  /// Retourne true si l'ajout a réussi (la liste est alors rechargée).
  Future<bool> submit(
      {required CompetenceRef competence, required Niveau niveau}) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard<void>(() async {
      await ref
          .read(competencesRepositoryProvider)
          .add(competenceId: competence.id, niveau: niveau);
    });
    if (state.hasError) return false;
    ref.invalidate(competencesProvider);
    return true;
  }
}

final addCompetenceControllerProvider =
    AsyncNotifierProvider.autoDispose<AddCompetenceController, void>(
        AddCompetenceController.new);