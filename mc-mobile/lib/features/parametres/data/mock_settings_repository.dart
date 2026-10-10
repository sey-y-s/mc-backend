import 'package:shared_preferences/shared_preferences.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_models.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_repository.dart';

class MockSettingsRepository implements SettingsRepository {
  const MockSettingsRepository();
  static bool simulateError = false;
  static UserAccount _account = const UserAccount(
    id: 'compte-demo',
    telephone: '+223 70 00 00 00',
    email: 'mamadou@example.invalid',
  );
  static String _password = 'MaliDemo123';

  static void resetForTests() {
    simulateError = false;
    _account = const UserAccount(
      id: 'compte-demo',
      telephone: '+223 70 00 00 00',
      email: 'mamadou@example.invalid',
    );
    _password = 'MaliDemo123';
  }

  Future<void> _wait() async {
    await Future<void>.delayed(const Duration(milliseconds: 330));
    if (simulateError) throw const NetworkFailure();
  }

  @override
  Future<UserAccount> account() async {
    await _wait();
    return _account;
  }

  @override
  Future<UserAccount> updateAccount(AccountChanges changes) async {
    await _wait();
    if (changes.telephone.trim().isEmpty) {
      throw ValidationFailure('Le téléphone est obligatoire.');
    }
    _account = UserAccount(
      id: _account.id,
      telephone: changes.telephone.trim(),
      email: changes.email?.trim(),
    );
    return _account;
  }

  @override
  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    await _wait();
    if (currentPassword != _password) {
      throw ValidationFailure('Le mot de passe actuel est incorrect.');
    }
    if (newPassword.length < 8) {
      throw ValidationFailure(
        'Le nouveau mot de passe doit contenir au moins 8 caractères.',
      );
    }
    _password = newPassword;
  }

  @override
  Future<AppPreferences> loadPreferences() async {
    await _wait();
    final prefs = await SharedPreferences.getInstance();
    return AppPreferences(
      lowDataMode: prefs.getBool('settings.low_data') ?? false,
      notificationsEnabled: prefs.getBool('settings.notifications') ?? true,
    );
  }

  @override
  Future<void> savePreferences(AppPreferences preferences) async {
    await _wait();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('settings.low_data', preferences.lowDataMode);
    await prefs.setBool(
      'settings.notifications',
      preferences.notificationsEnabled,
    );
  }
}
