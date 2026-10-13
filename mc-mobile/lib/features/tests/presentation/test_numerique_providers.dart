import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/tests/data/api_test_numerique_repository.dart';
import 'package:mlc_mobile/features/tests/data/mock_test_numerique_repository.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_repository.dart';

final testNumeriqueRepositoryProvider = Provider<TestNumeriqueRepository>(
  (ref) => AppConfig.useMocks
      ? const MockTestNumeriqueRepository()
      : ApiTestNumeriqueRepository(
          ref.watch(dioProvider),
          ref.watch(tokenStorageProvider),
        ),
);
final testsRevisionProvider = StateProvider<int>((ref) => 0);
final testsProvider = FutureProvider.autoDispose<List<TestNumerique>>((ref) {
  ref.watch(testsRevisionProvider);
  return ref.watch(testNumeriqueRepositoryProvider).list(size: 20);
});
final testDetailProvider = FutureProvider.autoDispose
    .family<TestNumerique, String>(
      (ref, id) => ref.watch(testNumeriqueRepositoryProvider).get(id),
    );
final testResultsProvider = FutureProvider.autoDispose<List<ResultatTest>>((
  ref,
) {
  ref.watch(testsRevisionProvider);
  return ref.watch(testNumeriqueRepositoryProvider).listResults();
});

class TestRun {
  const TestRun({
    required this.testId,
    this.index = 0,
    this.answers = const {},
  });
  final String testId;
  final int index;
  final Map<String, TestAnswer> answers;
  TestRun copyWith({int? index, Map<String, TestAnswer>? answers}) => TestRun(
    testId: testId,
    index: index ?? this.index,
    answers: answers ?? this.answers,
  );
}

final testRunProvider = StateProvider.autoDispose.family<TestRun?, String>(
  (ref, id) => null,
);
