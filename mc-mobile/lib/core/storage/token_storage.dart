import 'package:flutter_secure_storage/flutter_secure_storage.dart';

abstract interface class TokenStorage {
  Future<String?> readAccess();
  Future<String?> readRefresh();
  Future<void> save({required String access, String? refresh});
  Future<void> clear();
}

/// Keychain (iOS) / Keystore (Android). Jamais SharedPreferences pour un JWT.
class SecureTokenStorage implements TokenStorage {
  SecureTokenStorage([FlutterSecureStorage? storage]) : _s = storage ?? const FlutterSecureStorage();
  final FlutterSecureStorage _s;
  static const _kAccess = 'access_token';
  static const _kRefresh = 'refresh_token';

  @override
  Future<String?> readAccess() => _s.read(key: _kAccess);
  @override
  Future<String?> readRefresh() => _s.read(key: _kRefresh);
  @override
  Future<void> save({required String access, String? refresh}) async {
    await _s.write(key: _kAccess, value: access);
    if (refresh != null) await _s.write(key: _kRefresh, value: refresh);
  }

  @override
  Future<void> clear() async {
    await _s.delete(key: _kAccess);
    await _s.delete(key: _kRefresh);
  }
}

/// Pour les tests et les mocks uniquement.
class InMemoryTokenStorage implements TokenStorage {
  String? _access;
  String? _refresh;
  @override
  Future<String?> readAccess() async => _access;
  @override
  Future<String?> readRefresh() async => _refresh;
  @override
  Future<void> save({required String access, String? refresh}) async {
    _access = access;
    if (refresh != null) _refresh = refresh;
  }

  @override
  Future<void> clear() async {
    _access = null;
    _refresh = null;
  }
}
