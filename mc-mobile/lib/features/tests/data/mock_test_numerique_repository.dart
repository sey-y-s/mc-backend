import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/preuves/data/mock_preuve_repository.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_repository.dart';

class MockTestNumeriqueRepository implements TestNumeriqueRepository {
  const MockTestNumeriqueRepository();

  static bool simulateError = false;
  static final List<ResultatTest> _results = [];

  static final List<TestNumerique> _tests = [
    TestNumerique(
      id: 'test-solaire-01',
      title: 'Bases de l’énergie solaire',
      description: 'Vérifiez vos connaissances sur les installations solaires.',
      competenceId: 'comp-solaire',
      competenceName: 'Installation solaire',
      durationMinutes: 10,
      questions: [
        TestQuestion(
          id: 'sol-q1',
          prompt: 'Quel équipement convertit le courant continu en courant alternatif ?',
          type: QuestionType.choixUnique,
          order: 1,
          options: const [
            TestOption(id: 'sol-q1-a', label: 'Le régulateur'),
            TestOption(id: 'sol-q1-b', label: 'L’onduleur'),
            TestOption(id: 'sol-q1-c', label: 'La batterie'),
          ],
        ),
        TestQuestion(
          id: 'sol-q2',
          prompt: 'Quels gestes contribuent à une installation sûre ?',
          type: QuestionType.choixMultiple,
          order: 2,
          options: const [
            TestOption(
              id: 'sol-q2-a',
              label: 'Isoler l’installation avant intervention',
            ),
            TestOption(
              id: 'sol-q2-b',
              label: 'Vérifier la polarité des connexions',
            ),
            TestOption(id: 'sol-q2-c', label: 'Omettre les protections'),
          ],
        ),
        TestQuestion(
          id: 'sol-q3',
          prompt: 'Décrivez une vérification à faire avant la mise en service.',
          type: QuestionType.texteLibre,
          order: 3,
        ),
      ],
    ),
    TestNumerique(
      id: 'test-couture-02',
      title: 'Couture et finitions',
      description:
          'Un court questionnaire sur les outils et les finitions textiles.',
      competenceId: 'comp-couture',
      competenceName: 'Couture professionnelle',
      durationMinutes: 8,
      questions: [
        TestQuestion(
          id: 'cout-q1',
          prompt: 'Quel outil mesure une longueur de tissu ?',
          type: QuestionType.vraiFaux,
          order: 1,
          options: const [
            TestOption(id: 'cout-q1-a', label: 'Le mètre ruban'),
            TestOption(id: 'cout-q1-b', label: 'Le dé à coudre'),
          ],
        ),
        TestQuestion(
          id: 'cout-q2',
          prompt: 'Citez une finition adaptée à un bord qui s’effiloche.',
          type: QuestionType.texteLibre,
          order: 2,
        ),
      ],
    ),
  ];

  static const Map<String, Set<String>> _correctAnswers = {
    'sol-q1': {'sol-q1-b'},
    'sol-q2': {'sol-q2-a', 'sol-q2-b'},
    'cout-q1': {'cout-q1-a'},
  };

  static void resetForTests() {
    _results.clear();
    simulateError = false;
  }

  Future<void> _wait() async {
    await Future<void>.delayed(const Duration(milliseconds: 390));
    if (simulateError) throw const NetworkFailure();
  }

  List<T> _page<T>(List<T> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  @override
  Future<List<TestNumerique>> list({int page = 0, int size = 20}) async {
    await _wait();
    final withLast = _tests.map((test) {
      final previous = _results.where((r) => r.testId == test.id).toList()
        ..sort((a, b) => b.date.compareTo(a.date));
      return previous.isEmpty
          ? test
          : test.copyWith(lastResult: previous.first);
    }).toList();
    return _page(withLast, page, size);
  }

  @override
  Future<TestNumerique> get(String id) async {
    await _wait();
    if (id == 'network-error') throw const NetworkFailure();
    return _tests.firstWhere(
      (e) => e.id == id,
      orElse: () => throw const NotFoundFailure(),
    );
  }

  @override
  Future<ResultatTest> submit(String testId, TestAnswers answers) async {
    await _wait();
    if (testId == 'network-error') throw const NetworkFailure();
    final test = _tests.firstWhere(
      (e) => e.id == testId,
      orElse: () => throw const NotFoundFailure(),
    );
    var scored = 0;
    var correct = 0;
    for (final question in test.questions) {
      final expected = _correctAnswers[question.id];
      if (expected == null) continue;
      scored++;
      final actual =
          answers.byQuestion[question.id]?.optionIds.toSet() ?? <String>{};
      if (actual.length == expected.length && actual.containsAll(expected)) {
        correct++;
      }
    }
    final score = scored == 0 ? 0.0 : correct * 100 / scored;
    final now = DateTime.now();
    final proofId = 'proof-test-${now.microsecondsSinceEpoch}';
    final result = ResultatTest(
      id: 'result-${now.microsecondsSinceEpoch}',
      testId: testId,
      score: score,
      passed: score >= 60,
      date: now,
      proofId: proofId,
    );
    MockPreuveRepository.addTestResult(
      preuveId: proofId,
      competenceId: test.competenceId,
      competenceName: test.competenceName,
      date: now,
    );
    _results.add(result);
    return result;
  }

  @override
  Future<List<ResultatTest>> listResults() async {
    await _wait();
    return [..._results]..sort((a, b) => b.date.compareTo(a.date));
  }
}
