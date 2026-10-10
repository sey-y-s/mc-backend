import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/features/talents/data/api_talent_repository.dart';
import 'package:mlc_mobile/features/talents/data/mock_talent_repository.dart';
import 'package:mlc_mobile/features/talents/domain/talent_models.dart';
import 'package:mlc_mobile/features/talents/domain/talent_repository.dart';

class TalentRegion {
  const TalentRegion(this.id, this.label);
  final String id, label;
}

final talentRepositoryProvider = Provider<TalentRepository>(
  (ref) => AppConfig.useMocks
      ? const MockTalentRepository()
      : ApiTalentRepository(ref.watch(dioProvider)),
);

final talentFiltersProvider = StateProvider.autoDispose<TalentFilters>(
  (ref) => const TalentFilters(),
);
final talentSearchRequestedProvider = StateProvider.autoDispose<bool>(
  (ref) => false,
);

final talentRegionsProvider = FutureProvider.autoDispose<List<TalentRegion>>((
  ref,
) async {
  if (AppConfig.useMocks) {
    return const [
      TalentRegion('region-bamako', 'Bamako'),
      TalentRegion('region-kayes', 'Kayes'),
      TalentRegion('region-sikasso', 'Sikasso'),
      TalentRegion('region-segou', 'Ségou'),
      TalentRegion('region-mopti', 'Mopti'),
      TalentRegion('region-gao', 'Gao'),
      TalentRegion('region-tombouctou', 'Tombouctou'),
      TalentRegion('region-kidal', 'Kidal'),
    ];
  }
  final response = await guardDio(
    () => ref.watch(dioProvider).get<dynamic>(ApiEndpoints.regions),
  );
  return pageItems(response.data)
      .whereType<Map>()
      .map(
        (row) => TalentRegion(
          (row['id'] ?? '').toString(),
          (row['nom'] ?? row['name'] ?? 'Région').toString(),
        ),
      )
      .toList();
});

final talentSearchProvider = FutureProvider.autoDispose
    .family<List<TalentSummary>, TalentFilters>((ref, filters) async {
      if (!ref.watch(talentSearchRequestedProvider)) return const [];
      var cancelled = false;
      ref.onDispose(() => cancelled = true);
      await Future<void>.delayed(const Duration(milliseconds: 400));
      if (cancelled) return const [];
      return ref.watch(talentRepositoryProvider).search(filters, size: 20);
    });
final talentProfileProvider = FutureProvider.autoDispose
    .family<TalentProfileAnonymized, String>(
      (ref, id) => ref.watch(talentRepositoryProvider).getAnonymized(id),
    );
