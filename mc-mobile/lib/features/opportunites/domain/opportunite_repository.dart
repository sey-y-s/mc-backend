import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';

abstract interface class OpportuniteRepository {
  Future<List<Opportunite>> list({
    OpportuniteType? type,
    String? categoryId,
    int page = 0,
    int size = 20,
  });
  Future<Opportunite> get(String id);
}
