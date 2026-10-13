import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/portfolio/data/api_portfolio_repository.dart';
import 'package:mlc_mobile/features/portfolio/data/mock_portfolio_repository.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_repository.dart';

final portfolioRepositoryProvider = Provider<PortfolioRepository>(
  (ref) => AppConfig.useMocks
      ? const MockPortfolioRepository()
      : ApiPortfolioRepository(
          ref.watch(dioProvider),
          ref.watch(tokenStorageProvider),
        ),
);
final portfolioRevisionProvider = StateProvider<int>((ref) => 0);
final portfolioListProvider =
    FutureProvider.autoDispose<List<PortfolioRealisation>>((ref) {
      ref.watch(portfolioRevisionProvider);
      return ref.watch(portfolioRepositoryProvider).list(size: 20);
    });
final portfolioDetailProvider = FutureProvider.autoDispose
    .family<PortfolioRealisation, String>((ref, id) {
      ref.watch(portfolioRevisionProvider);
      return ref.watch(portfolioRepositoryProvider).get(id);
    });
