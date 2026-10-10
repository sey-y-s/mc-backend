import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/features/admin/domain/admin_models.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';

class ApiAdminRepository {
  const ApiAdminRepository(this._dio);

  final Dio _dio;

  Future<Map<String, dynamic>> _getMap(String path) async {
    final response = await guardDio(() => _dio.get<Map<String, dynamic>>(path));
    return response.data ?? <String, dynamic>{};
  }

  Future<List<Map<String, dynamic>>> _getRows(String path) async {
    final response = await guardDio(() => _dio.get<dynamic>(path));
    return pageItems(response.data)
        .whereType<Map>()
        .map((row) => Map<String, dynamic>.from(row))
        .toList();
  }

  Future<AdminDashboard> dashboard() async =>
      AdminDashboard.fromJson(await _getMap(ApiEndpoints.adminDashboard()));

  Future<List<AdminUser>> users() async =>
      (await _getRows(ApiEndpoints.adminUsers()))
          .map(AdminUser.fromJson)
          .toList();

  Future<void> updateUserRole(String id, String role) async {
    await guardDio(
      () =>
          _dio.put<void>(ApiEndpoints.adminUserRole(id), data: {'role': role}),
    );
  }

  Future<List<AdminValidation>> validations() async =>
      (await _getRows(ApiEndpoints.adminValidations()))
          .map(AdminValidation.fromJson)
          .toList();

  Future<void> updateValidationStatus(
    String id, {
    required String status,
    String? comment,
  }) async {
    await guardDio(
      () => _dio.patch<void>(
        ApiEndpoints.adminValidationStatus(id),
        data: {'statut': status, 'commentaire': comment},
      ),
    );
  }

  Future<List<AdminOrganization>> organizations() async =>
      (await _getRows('${ApiEndpoints.admin}/organisations'))
          .map(AdminOrganization.fromJson)
          .toList();

  Future<void> updateOrganizationStatus(String id, String status) async {
    await guardDio(
      () => _dio.patch<void>(
        '${ApiEndpoints.admin}/organisations/$id/statut',
        data: {'statut': status},
      ),
    );
  }

  Future<List<AdminQcmTest>> tests() async =>
      (await _getRows(ApiEndpoints.adminTests()))
          .map(AdminQcmTest.fromJson)
          .toList();

  Future<List<AdminCompetence>> competences() async =>
      (await _getRows(ApiEndpoints.adminCompetences))
          .map(AdminCompetence.fromJson)
          .toList();

  Future<void> saveTest({
    String? id,
    required String competenceId,
    required String title,
    required String description,
    required bool active,
    required List<AdminQcmQuestion> questions,
  }) async {
    final data = <String, dynamic>{
      'competenceId': competenceId,
      'titre': title,
      'description': description,
      'actif': active,
      'questions': [
        for (var i = 0; i < questions.length; i++)
          {
            'texte': questions[i].text.trim(),
            'type': 'CHOIX_MULTIPLE',
            'ordre': i + 1,
            'propositions': [
              for (final answer in questions[i].answers)
                {'texte': answer.text.trim(), 'correcte': answer.correct},
            ],
          },
      ],
    };
    if (id == null) {
      await guardDio(
        () => _dio.post<void>(ApiEndpoints.adminTests(), data: data),
      );
    } else {
      await guardDio(
        () => _dio.put<void>(ApiEndpoints.adminTest(id), data: data),
      );
    }
  }

  Future<void> deleteTest(String id) async {
    await guardDio(() => _dio.delete<void>(ApiEndpoints.adminTest(id)));
  }

  Future<List<AdminOpportunityCategory>> opportunityCategories() async =>
      (await _getRows(ApiEndpoints.opportunityCategories))
          .map(AdminOpportunityCategory.fromJson)
          .toList();

  Future<List<AdminOpportunityDraft>> opportunities() async =>
      (await _getRows(ApiEndpoints.adminOpportunities()))
          .map(AdminOpportunityDraft.fromJson)
          .toList();

  Future<void> saveOpportunity(AdminOpportunityDraft opportunity) async {
    final data = <String, dynamic>{
      'categorieId': opportunity.categoryId,
      'titre': opportunity.title.trim(),
      'description': opportunity.description.trim(),
      'statut': _opportunityStatusCode(opportunity.status),
      'type': opportunity.type.apiCode,
      'dateExpiration': opportunity.expirationDate?.toIso8601String().split(
        'T',
      )[0],
    };
    if (opportunity.id == null) {
      await guardDio(
        () => _dio.post<void>(ApiEndpoints.mobileOpportunities, data: data),
      );
    } else {
      await guardDio(
        () => _dio.put<void>(
          ApiEndpoints.adminOpportunity(opportunity.id!),
          data: data,
        ),
      );
    }
  }

  Future<void> deleteOpportunity(String id) async {
    await guardDio(() => _dio.delete<void>(ApiEndpoints.adminOpportunity(id)));
  }

  String _opportunityStatusCode(OpportuniteStatus status) => switch (status) {
    OpportuniteStatus.brouillon => 'BROUILLON',
    OpportuniteStatus.publiee => 'PUBLIEE',
    OpportuniteStatus.expiree => 'EXPIREE',
    OpportuniteStatus.archivee => 'ARCHIVEE',
    OpportuniteStatus.annulee => 'ARCHIVEE',
  };
}
