import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/features/auth/data/api_auth_repository.dart';
import 'package:mlc_mobile/features/auth/data/mock_auth_repository.dart';
import 'package:mlc_mobile/features/auth/domain/auth_repository.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final storage = ref.watch(tokenStorageProvider);
  return AppConfig.useMocks
      ? MockAuthRepository(storage)
      : ApiAuthRepository(ref.watch(dioProvider), storage);
});

/// État d'une action d'authentification (login/register/logout) : loading / error / data.
class AuthController extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  Future<bool> login(String identifiant, String password) => _run(
        () => ref.read(authRepositoryProvider).login(identifiant: identifiant, password: password),
      );

  Future<bool> register(RegisterData data) => _run(() => ref.read(authRepositoryProvider).register(data));

  Future<void> logout() async {
    final repositoryLogout = ref.read(authRepositoryProvider).logout();
    final expireSession = ref.read(sessionProvider.notifier).expire();
    await Future.wait([repositoryLogout, expireSession]);
  }

  Future<bool> _run(Future<void> Function() action) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(action);
    if (state.hasError) return false;
    ref.read(sessionProvider.notifier).setAuthenticated();
    return true;
  }
}

final authControllerProvider =
    AsyncNotifierProvider.autoDispose<AuthController, void>(AuthController.new);