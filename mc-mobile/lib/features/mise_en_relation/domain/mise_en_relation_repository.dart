import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_models.dart';

abstract interface class MiseEnRelationRepository {
  Future<List<MiseEnRelation>> listReceived({int page = 0, int size = 20});
  Future<List<MiseEnRelation>> listSent({int page = 0, int size = 20});
  Future<MiseEnRelation> get(String id);
  Future<MiseEnRelation> send(String talentId, {String? message});
  Future<MiseEnRelation> respond(String id, {required bool accept});
}
