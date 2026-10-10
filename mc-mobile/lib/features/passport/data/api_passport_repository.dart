import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/network/upload.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/domain/passport_repository.dart';

/// Contrat provisoire (voir docs/CONTRAT_API_PROVISOIRE.md, section Passeport). Toutes les écritures renvoient le Citoyen à jour.
class ApiPassportRepository implements PassportRepository {
  ApiPassportRepository(this._dio);
  final Dio _dio;

  String get _me => ApiEndpoints.citoyen(ApiEndpoints.me);

  Future<Citoyen> _citoyen(Response<Map<String, dynamic>> res) async {
    final data = res.data!;
    final communeId = (data['communeId'] ?? '').toString();
    if (communeId.isEmpty || data['commune'] is Map) {
      return Citoyen.fromJson(data);
    }
    final communeResponse = await guardDio(
      () => _dio.get<Map<String, dynamic>>(ApiEndpoints.commune(communeId)),
    );
    return Citoyen.fromJson({...data, 'commune': communeResponse.data});
  }

  @override
  Future<Citoyen> getMine() async => _citoyen(
    await guardDio(() => _dio.get<Map<String, dynamic>>(_me)),
  );

  @override
  Future<Citoyen> updateProfile({
    required String nom,
    required String prenom,
    Sexe? sexe,
  }) async => _updateMine(nom: nom, prenom: prenom, sexe: sexe);

  @override
  Future<Citoyen> updateCommune(String communeId) =>
      _updateMine(communeId: communeId);

  @override
  Future<Citoyen> updateAvailability(Disponibilite value) =>
      _updateMine(disponibilite: value);

  Future<Citoyen> _updateMine({
    String? nom,
    String? prenom,
    Sexe? sexe,
    String? communeId,
    Disponibilite? disponibilite,
  }) async {
    final current = await getMine();
    final res = await guardDio(
      () => _dio.put<Map<String, dynamic>>(
        _me,
        data: {
          'nom': nom ?? current.nom,
          'prenom': prenom ?? current.prenom,
          'sexe': (sexe ?? current.sexe)?.apiCode,
          'photoUrl': current.photoUrl,
          'disponibilite': (disponibilite ?? current.disponibilite).apiCode,
          'communeId': communeId ?? current.commune?.id,
        },
      ),
    );
    return _citoyen(res);
  }

  @override
  Future<Citoyen> updatePhoto(
    PickedMedia media, {
    void Function(double progress)? onProgress,
  }) async => _citoyen(
    await guardDio(
      () => uploadMultipart<Map<String, dynamic>>(
        _dio,
        ApiEndpoints.citoyenPhoto(ApiEndpoints.me),
        file: media,
        onProgress: onProgress,
      ),
    ),
  );

  @override
  Future<List<Region>> listRegions() async {
    final res = await guardDio(() => _dio.get<dynamic>(ApiEndpoints.regions));
    return pageItems(res.data)
        .map((e) => Region.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  @override
  Future<List<Commune>> listCommunes(String regionId) async {
    final res = await guardDio(
      () => _dio.get<dynamic>(ApiEndpoints.citoyenRegionCommunes(regionId)),
    );
    return pageItems(res.data)
        .map((e) => Commune.fromJson(e as Map<String, dynamic>))
        .toList();
  }
}
