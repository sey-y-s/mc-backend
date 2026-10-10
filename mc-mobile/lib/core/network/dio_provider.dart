import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/config/app_config.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';

final dioProvider = Provider<Dio>((ref) {
  return buildDio(
    storage: ref.watch(tokenStorageProvider),
    onUnauthorized: () => ref.read(sessionProvider.notifier).expire(),
  );
});

Dio buildDio({
  required TokenStorage storage,
  required Future<void> Function() onUnauthorized,
}) {
  final dio = Dio(
    BaseOptions(
      baseUrl: AppConfig.apiBaseUrl,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 20),
      headers: {'Accept': 'application/json'},
    ),
  );
  // Volontairement aucun LogInterceptor : pas de token ni de données perso dans les logs.
  dio.interceptors.add(AuthInterceptor(storage, onUnauthorized));
  return dio;
}

class AuthInterceptor extends Interceptor {
  AuthInterceptor(this._storage, this._onUnauthorized);
  final TokenStorage _storage;
  final Future<void> Function() _onUnauthorized;

  bool _isAuthCall(RequestOptions o) => o.path.startsWith(ApiEndpoints.auth);

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    if (!_isAuthCall(options)) {
      final token = await _storage.readAccess();
      if (token != null && token.isNotEmpty) {
        options.headers['Authorization'] = 'Bearer $token';
      }
    }
    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    // TODO: si le backend expose le renouvellement de session, utiliser ApiEndpoints.refresh.
    // (verrou pour éviter les refresh concurrents), rejouer la requête une fois, et n'appeler
    // _onUnauthorized() qu'en cas d'échec. Tant que le backend ne le propose pas : logout direct.
    if (err.response?.statusCode == 401 && !_isAuthCall(err.requestOptions)) {
      await _onUnauthorized();
    }
    handler.next(err);
  }
}

/// Exécute un appel Dio et convertit toute erreur en [AppFailure].
Future<T> guardDio<T>(Future<T> Function() call) async {
  try {
    return await call();
  } on DioException catch (e) {
    throw AppFailure.fromDio(e);
  }
}
