import 'package:mlc_mobile/features/talents/domain/talent_models.dart';

abstract interface class TalentRepository {
  Future<List<TalentSummary>> search(
    TalentFilters filters, {
    int page = 0,
    int size = 20,
  });
  Future<TalentProfileAnonymized> getAnonymized(String id);
}
