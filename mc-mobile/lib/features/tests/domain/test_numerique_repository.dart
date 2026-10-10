import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';

abstract interface class TestNumeriqueRepository {
  Future<List<TestNumerique>> list({int page = 0, int size = 20});
  Future<TestNumerique> get(String id);
  Future<ResultatTest> submit(String testId, TestAnswers answers);
  Future<List<ResultatTest>> listResults();
}
