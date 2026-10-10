import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_models.dart';
import 'package:mlc_mobile/features/tests/domain/test_numerique_repository.dart';

class ApiTestNumeriqueRepository implements TestNumeriqueRepository {
  const ApiTestNumeriqueRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  List<T> _page<T>(List<T> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    return start >= items.length
        ? const []
        : items.skip(start).take(size).toList();
  }

  Future<List<dynamic>> _fetchList(String path) async {
    final response = await guardDio(() => _dio.get<dynamic>(path));
    return pageItems(response.data);
  }

  @override
  Future<List<TestNumerique>> list({int page = 0, int size = 20}) async {
    final userId = await requireCurrentUserId(_storage);
    final rows = await _fetchList(
      ApiEndpoints.mobileCitizenCompetences(userId),
    );
    final competenceIds = <String>{};
    for (final row in rows) {
      if (row is! Map) continue;
      final nested = row['competence'];
      final id =
          row['competenceId'] ??
          (nested is Map ? nested['id'] : null) ??
          row['id'];
      if (id != null && id.toString().isNotEmpty) {
        competenceIds.add(id.toString());
      }
    }
    final byId = <String, TestNumerique>{};
    for (final competenceId in competenceIds) {
      for (final row in await _fetchList(
        ApiEndpoints.mobileTestsForCompetence(competenceId),
      )) {
        if (row is! Map) continue;
        final test = TestNumerique.fromJson(Map<String, dynamic>.from(row));
        if (test.id.isNotEmpty) byId[test.id] = test;
      }
    }
    final latest = <String, ResultatTest>{};
    for (final row in await _fetchList(
      ApiEndpoints.mobileCitizenTestResults(userId),
    )) {
      if (row is! Map) continue;
      final data = Map<String, dynamic>.from(row);
      final testId = (data['testId'] ?? data['testNumeriqueId'] ?? '')
          .toString();
      if (testId.isEmpty) continue;
      final result = ResultatTest.fromJson({...data, 'testId': testId});
      if (latest[testId] == null || result.date.isAfter(latest[testId]!.date)) {
        latest[testId] = result;
      }
    }
    final items =
        byId.values.map((e) => e.copyWith(lastResult: latest[e.id])).toList()
          ..sort(
            (a, b) => a.title.toLowerCase().compareTo(b.title.toLowerCase()),
          );
    return _page(items, page, size);
  }

  @override
  Future<TestNumerique> get(String id) async {
    final response = await guardDio(
      () => _dio.get<Map<String, dynamic>>(ApiEndpoints.mobileTest(id)),
    );
    final test = TestNumerique.fromJson(response.data!);
    final questions = <TestQuestion>[];
    for (final row in await _fetchList(ApiEndpoints.mobileTestQuestions(id))) {
      if (row is! Map) continue;
      final question = TestQuestion.fromJson(Map<String, dynamic>.from(row));
      final options = await _fetchList(
        ApiEndpoints.mobileQuestionOptions(question.id),
      );
      questions.add(
        TestQuestion(
          id: question.id,
          prompt: question.prompt,
          type: question.type,
          order: question.order,
          options: options
              .whereType<Map>()
              .map((e) => TestOption.fromJson(Map<String, dynamic>.from(e)))
              .toList(),
        ),
      );
    }
    questions.sort((a, b) => a.order.compareTo(b.order));
    return test.copyWith(questions: questions);
  }

  @override
  Future<ResultatTest> submit(String testId, TestAnswers answers) async {
    final userId = await requireCurrentUserId(_storage);
    final response = await guardDio(
      () => _dio.post<Map<String, dynamic>>(
        ApiEndpoints.mobileTestResults(testId),
        data: {
          'citoyenId': userId,
          'reponses': {
            for (final entry in answers.byQuestion.entries)
              if (entry.value.optionIds.isNotEmpty)
                entry.key: entry.value.optionIds,
          },
        },
      ),
    );
    return ResultatTest.fromJson({...response.data!, 'testId': testId});
  }

  @override
  Future<List<ResultatTest>> listResults() async {
    final userId = await requireCurrentUserId(_storage);
    final rows = await _fetchList(
      ApiEndpoints.mobileCitizenTestResults(userId),
    );
    return rows.whereType<Map>().map((row) {
      final data = Map<String, dynamic>.from(row);
      return ResultatTest.fromJson({
        ...data,
        'testId': data['testId'] ?? data['testNumeriqueId'] ?? '',
      });
    }).toList()..sort((a, b) => b.date.compareTo(a.date));
  }
}
