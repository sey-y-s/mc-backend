import 'package:mlc_mobile/features/parametres/domain/settings_models.dart';

abstract interface class SettingsRepository {
  Future<UserAccount> account();
  Future<UserAccount> updateAccount(AccountChanges changes);
  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  });
  Future<AppPreferences> loadPreferences();
  Future<void> savePreferences(AppPreferences preferences);
}
