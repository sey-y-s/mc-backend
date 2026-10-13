import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';

abstract interface class PortfolioRepository {
  Future<List<PortfolioRealisation>> list({int page = 0, int size = 20});
  Future<PortfolioRealisation> get(String id);
  Future<PortfolioRealisation> create(PortfolioInput input);
  Future<PortfolioRealisation> update(String id, PortfolioInput input);
  Future<void> delete(String id);
  Future<PortfolioMedia> addMedia(
    String portfolioId,
    PickedMedia file, {
    void Function(double progress)? onProgress,
  });
  Future<void> removeMedia(String portfolioId, String mediaId);
}
