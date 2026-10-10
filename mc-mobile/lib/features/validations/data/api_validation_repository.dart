import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_repository.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

/// Contrat provisoire :
///  GET  /api/citoyens/me/validations?statut=&page&size
///  GET  /api/citoyen-competences/{id}/validations
///  GET  /api/validations/{id}
///  POST /api/citoyen-competences/{id}/validations {preuveId}  (400/409/422 si la demande est refusée)
class ApiValidationRepository implements ValidationRepository {
  ApiValidationRepository(this._dio);
  final Dio _dio;

  List<Validation> _list(dynamic body) => pageItems(body).map((e) => Validation.fromJson(e as Map<String, dynamic>)).toList();

  @override
  Future<List<Validation>> listMine({ValidationStatut? statut, int page = 0, int size = 20}) async {
    final res = await guardDio(() => _dio.get<dynamic>(
          ApiEndpoints.citoyenValidations(ApiEndpoints.me),
          queryParameters: {'page': page, 'size': size, if (statut != null) 'statut': statut.apiCode},
        ));
    return _list(res.data);
  }

  @override
  Future<List<Validation>> listForCompetence(String citoyenCompetenceId) async {
    final res = await guardDio(() => _dio.get<dynamic>(ApiEndpoints.validations(citoyenCompetenceId)));
    return _list(res.data);
  }

  @override
  Future<Validation> get(String id) async {
    final res = await guardDio(() => _dio.get<Map<String, dynamic>>(ApiEndpoints.validation(id)));
    return Validation.fromJson(res.data!);
  }

  @override
  Future<Validation> request({required String citoyenCompetenceId, required String preuveId}) async {
    final res = await guardDio(() => _dio.post<Map<String, dynamic>>(
          ApiEndpoints.validations(citoyenCompetenceId),
          data: {'preuveId': preuveId},
        ));
    return Validation.fromJson(res.data!);
  }
}