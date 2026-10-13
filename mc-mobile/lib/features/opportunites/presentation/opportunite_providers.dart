import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/features/opportunites/data/api_opportunite_repository.dart';
import 'package:mlc_mobile/features/opportunites/data/mock_opportunite_repository.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_repository.dart';

final opportuniteRepositoryProvider = Provider<OpportuniteRepository>((ref) {
  return AppConfig.useMocks
      ? const MockOpportuniteRepository()
      : ApiOpportuniteRepository(ref.watch(dioProvider));
});

/// Filtre par type sélectionné dans la liste des opportunités
final opportuniteTypeFilterProvider =
    StateProvider.autoDispose<OpportuniteType?>((ref) => null);

/// 3 opportunités récentes pour le bloc de la page d'accueil
final recentOpportunitesProvider =
    FutureProvider.autoDispose<List<Opportunite>>((ref) {
  return ref.watch(opportuniteRepositoryProvider).list(size: 3);
});

/// Détail d'une opportunité
final opportuniteDetailProvider =
    FutureProvider.autoDispose.family<Opportunite, String>((ref, id) {
  return ref.watch(opportuniteRepositoryProvider).get(id);
});
