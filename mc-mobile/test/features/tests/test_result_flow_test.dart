import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/preuves/data/mock_preuve_repository.dart';
import 'package:mlc_mobile/features/tests/data/mock_test_numerique_repository.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_repository.dart';
import 'package:mlc_mobile/features/tests/presentation/test_numerique_providers.dart';
import 'package:mlc_mobile/features/tests/presentation/test_result_screen.dart';

class _UnusedTestRepository implements TestNumeriqueRepository {
  @override
  Future<TestNumerique> get(String id) => throw UnimplementedError();

  @override
  Future<List<ResultatTest>> listResults() => throw StateError(
    'The submitted result should be used directly.',
  );

  @override
  Future<List<TestNumerique>> list({int page = 0, int size = 20}) =>
      throw UnimplementedError();

  @override
  Future<ResultatTest> submit(String testId, TestAnswers answers) =>
      throw UnimplementedError();
}

void main() {
  testWidgets('shows the submitted test result without reloading history', (
    tester,
  ) async {
    final result = ResultatTest(
      id: 'result-1',
      testId: 'test-1',
      score: 75,
      passed: true,
      date: DateTime(2026, 10, 9),
      proofId: 'proof-1',
    );

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          testNumeriqueRepositoryProvider.overrideWithValue(
            _UnusedTestRepository(),
          ),
        ],
        child: MaterialApp(
          home: TestResultScreen(id: 'test-1', result: result),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('75%'), findsOneWidget);
    expect(find.text('Résultat favorable'), findsOneWidget);
    expect(find.text('Voir la preuve'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  test('mock test results resolve to a proof detail', () async {
    MockTestNumeriqueRepository.resetForTests();
    MockPreuveRepository.resetForTests();

    final result = await const MockTestNumeriqueRepository().submit(
      'test-solaire-01',
      const TestAnswers({
        'sol-q1': TestAnswer(optionIds: ['sol-q1-b']),
        'sol-q2': TestAnswer(optionIds: ['sol-q2-a', 'sol-q2-b']),
      }),
    );

    final proof = await const MockPreuveRepository().get(result.proofId!);
    expect(proof.id, result.proofId);
    expect(proof.type, PreuveType.testNumerique);
  });
}
