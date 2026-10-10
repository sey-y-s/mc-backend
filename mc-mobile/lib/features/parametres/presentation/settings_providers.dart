import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/parametres/data/api_settings_repository.dart';
import 'package:mlc_mobile/features/parametres/data/mock_settings_repository.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_repository.dart';

final settingsRepositoryProvider = Provider<SettingsRepository>(
  (ref) => AppConfig.useMocks
      ? const MockSettingsRepository()
      : ApiSettingsRepository(
          ref.watch(dioProvider),
          ref.watch(tokenStorageProvider),
        ),
);
final accountRevisionProvider = StateProvider<int>((ref) => 0);
final accountProvider = FutureProvider.autoDispose((ref) {
  ref.watch(accountRevisionProvider);
  return ref.watch(settingsRepositoryProvider).account();
});
final preferencesProvider = FutureProvider.autoDispose(
  (ref) => ref.watch(settingsRepositoryProvider).loadPreferences(),
);
