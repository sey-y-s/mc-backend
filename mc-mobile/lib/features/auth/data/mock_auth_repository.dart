import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/auth/domain/auth_repository.dart';

/// Mock : tout couple identifiant + mot de passe est accepté,
/// sauf le mot de passe "mauvaismdp" (pour tester l'erreur).
class MockAuthRepository implements AuthRepository {
  MockAuthRepository(this._storage);
  static const testResetCode = '123456';
  final TokenStorage _storage;

  Future<void> _latency() => Future<void>.delayed(const Duration(milliseconds: 300));

  @override
  Future<void> login({required String identifiant, required String password}) async {
    await _latency();
    if (password == 'mauvaismdp') throw const InvalidCredentialsFailure();
    await _storage.save(access: 'mock-access-token');
  }

  @override
  Future<void> register(RegisterData data) async {
    await _latency();
    await _storage.save(access: 'mock-access-token');
  }

  @override
  Future<void> logout() => _storage.clear();

  @override
  Future<void> requestPasswordReset(String identifiant) => _latency();

  @override
  Future<void> resetPassword({
    required String identifiant,
    required String code,
    required String newPassword,
  }) async {
    await _latency();
    if (code != testResetCode) throw ValidationFailure('Code incorrect ou expiré.');
  }
}
