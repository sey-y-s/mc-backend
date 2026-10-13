import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/mise_en_relation/data/api_mise_en_relation_repository.dart';
import 'package:mlc_mobile/features/mise_en_relation/data/mock_mise_en_relation_repository.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_models.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_repository.dart';

final miseEnRelationRepositoryProvider = Provider<MiseEnRelationRepository>(
  (ref) => AppConfig.useMocks
      ? const MockMiseEnRelationRepository()
      : ApiMiseEnRelationRepository(
          ref.watch(dioProvider),
          ref.watch(tokenStorageProvider),
        ),
);
final relationsRevisionProvider = StateProvider<int>((ref) => 0);
final relationsReceivedProvider =
    FutureProvider.autoDispose<List<MiseEnRelation>>((ref) {
      ref.watch(relationsRevisionProvider);
      return ref.watch(miseEnRelationRepositoryProvider).listReceived(size: 20);
    });
final relationsSentProvider = FutureProvider.autoDispose<List<MiseEnRelation>>((
  ref,
) {
  ref.watch(relationsRevisionProvider);
  return ref.watch(miseEnRelationRepositoryProvider).listSent(size: 20);
});
final relationDetailProvider = FutureProvider.autoDispose
    .family<MiseEnRelation, String>((ref, id) {
      ref.watch(relationsRevisionProvider);
      return ref.watch(miseEnRelationRepositoryProvider).get(id);
    });
