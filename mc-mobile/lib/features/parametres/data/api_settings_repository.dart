import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/current_user_id.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_models.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_repository.dart';

class ApiSettingsRepository implements SettingsRepository {
  const ApiSettingsRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;
  Future<String> get _userId => requireCurrentUserId(_storage);

  @override
  Future<UserAccount> account() async {
    final uid = await _userId;
    final res = await guardDio(
      () => _dio.get<Map<String, dynamic>>(ApiEndpoints.mobileUser(uid)),
    );
    return UserAccount.fromJson(res.data!);
  }

  @override
  Future<UserAccount> updateAccount(AccountChanges changes) async {
    final uid = await _userId;
    final res = await guardDio(
      () => _dio.put<Map<String, dynamic>>(
        ApiEndpoints.mobileUserContact(uid),
        data: {
          'telephone': changes.telephone.trim(),
          'email': changes.email?.trim(),
        },
      ),
    );
    return UserAccount.fromJson(res.data!);
  }

  @override
  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    final uid = await _userId;
    await guardDio(
      () => _dio.put<void>(
        ApiEndpoints.mobilePasswordChange,
        data: {
          'utilisateurId': uid,
          'ancienMotDePasse': currentPassword,
          'nouveauMotDePasse': newPassword,
        },
      ),
    );
  }

  @override
  Future<AppPreferences> loadPreferences() async {
    final response = await guardDio(
      () => _dio.get<Map<String, dynamic>>(ApiEndpoints.mobileUserPreferences),
    );
    return AppPreferences(
      lowDataMode: response.data?['lowDataMode'] == true,
      notificationsEnabled: response.data?['notificationsEnabled'] != false,
    );
  }

  @override
  Future<void> savePreferences(AppPreferences preferences) async {
    await guardDio(
      () => _dio.put<void>(
        ApiEndpoints.mobileUserPreferences,
        data: {
          'lowDataMode': preferences.lowDataMode,
          'notificationsEnabled': preferences.notificationsEnabled,
        },
      ),
    );
  }
}
