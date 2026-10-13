import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/network/page_utils.dart';
import 'package:mlc_mobile/core/network/upload.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_models.dart';
import 'package:mlc_mobile/features/preuves/domain/preuve_repository.dart';

class ApiPreuveRepository implements PreuveRepository {
  ApiPreuveRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  Future<List<Map<String, dynamic>>> _citizenCompetences() async {
    final citoyenId = await requireCurrentUserId(_storage);
    final response = await guardDio(
      () => _dio.get<dynamic>(ApiEndpoints.citoyenCompetences(citoyenId)),
    );
    return pageItems(response.data)
        .whereType<Map>()
        .map((row) => Map<String, dynamic>.from(row))
        .toList();
  }

  Future<String> _competenceName(String citoyenCompetenceId) async {
    final competences = await _citizenCompetences();
    for (final competence in competences) {
      if (competence['id']?.toString() == citoyenCompetenceId) {
        return (competence['competenceNom'] ?? 'Compétence').toString();
      }
    }
    throw const NotFoundFailure();
  }

  Future<List<Preuve>> _listForCompetence(
    String citoyenCompetenceId,
    String competenceNom,
  ) async {
    final response = await guardDio(
      () => _dio.get<dynamic>(ApiEndpoints.preuves(citoyenCompetenceId)),
    );
    return pageItems(response.data)
        .whereType<Map>()
        .map(
          (row) =>
              _proofFromJson(Map<String, dynamic>.from(row), competenceNom),
        )
        .toList();
  }

  Preuve _proofFromJson(Map<String, dynamic> row, String competenceNom) {
    final json = {
      ...row,
      'competenceNom': row['competenceNom'] ?? competenceNom,
      'date': row['date'] ?? row['dateDepot'],
      'fichierNom': row['fichierNom'] ?? row['titre'],
    };
    return Preuve.fromJson(json);
  }

  List<Preuve> _page(List<Preuve> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    return start >= items.length
        ? const []
        : items.skip(start).take(size).toList();
  }

  @override
  Future<List<Preuve>> listMine({int page = 0, int size = 20}) async {
    final competences = await _citizenCompetences();
    final preuves = await Future.wait(
      competences.map((competence) {
        final id = (competence['id'] ?? '').toString();
        final name = (competence['competenceNom'] ?? 'Compétence').toString();
        if (id.isEmpty) return Future.value(const <Preuve>[]);
        return _listForCompetence(id, name);
      }),
    );
    final items = preuves.expand((list) => list).toList()
      ..sort((a, b) => b.date.compareTo(a.date));
    return _page(items, page, size);
  }

  @override
  Future<List<Preuve>> listForCompetence(String citoyenCompetenceId) async {
    final competences = await _citizenCompetences();
    final matches = competences.where(
      (row) => row['id']?.toString() == citoyenCompetenceId,
    );
    if (matches.isEmpty) throw const NotFoundFailure();
    final competence = matches.first;
    return _listForCompetence(
      citoyenCompetenceId,
      (competence['competenceNom'] ?? 'Compétence').toString(),
    );
  }

  @override
  Future<Preuve> get(String id) async {
    final competences = await _citizenCompetences();
    for (final competence in competences) {
      final competenceId = (competence['id'] ?? '').toString();
      if (competenceId.isEmpty) continue;
      final proofs = await _listForCompetence(
        competenceId,
        (competence['competenceNom'] ?? 'Compétence').toString(),
      );
      for (final proof in proofs) {
        if (proof.id == id) return proof;
      }
    }
    throw const NotFoundFailure();
  }

  @override
  Future<Preuve> add({
    required String citoyenCompetenceId,
    required PreuveType type,
    required PickedMedia fichier,
    void Function(double progress)? onProgress,
  }) async {
    final res = await guardDio(
      () => uploadMultipart<Map<String, dynamic>>(
        _dio,
        ApiEndpoints.preuves(citoyenCompetenceId),
        file: fichier,
        fields: {'type': type.apiCode},
        onProgress: onProgress,
      ),
    );
    return _proofFromJson(
      res.data!,
      await _competenceName(citoyenCompetenceId),
    );
  }
}
