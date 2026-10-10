import 'dart:async';

import 'package:dio/dio.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/network/api_endpoints.dart';
import 'package:mlc_mobile/core/network/dio_provider.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/auth/domain/auth_repository.dart';

/// Authentification et inscription suivent les DTO de l'API Spring.
class ApiAuthRepository implements AuthRepository {
  ApiAuthRepository(this._dio, this._storage);
  final Dio _dio;
  final TokenStorage _storage;

  @override
  Future<void> login({
    required String identifiant,
    required String password,
  }) async {
    try {
      final res = await guardDio(
        () => _dio.post<Map<String, dynamic>>(
          ApiEndpoints.login,
          data: {'identifiant': identifiant, 'password': password},
        ),
      );
      await _saveTokens(res.data);
    } on UnauthorizedFailure {
      throw const InvalidCredentialsFailure();
    }
  }

  @override
  Future<void> register(RegisterData d) async {
    await guardDio(
      () => _dio.post<void>(
        ApiEndpoints.register,
        data: {
          'nom': d.nom,
          'prenom': d.prenom,
          'telephone': d.telephone,
          if (d.email != null) 'email': d.email,
          'password': d.password,
          if (d.communeId != null) 'communeId': d.communeId,
        },
      ),
    );
    await login(identifiant: d.telephone, password: d.password);
  }

  @override
  Future<void> logout() async {
    // La déconnexion locale ne doit pas dépendre de la disponibilité du serveur.
    try {
      await _dio
          .post<void>(ApiEndpoints.logout)
          .timeout(const Duration(seconds: 3));
    } on DioException {
      // Ignoré : les jetons locaux sont supprimés même si le serveur est injoignable.
    } on TimeoutException {
      // Le délai réseau est borné pour permettre à l'application de fermer la session.
    } finally {
      await _storage.clear();
    }
  }

  @override
  Future<void> requestPasswordReset(String identifiant) => guardDio(
    () => _dio.post<void>(
      ApiEndpoints.forgotPassword,
      data: {'identifiant': identifiant},
    ),
  );

  @override
  Future<void> resetPassword({
    required String identifiant,
    required String code,
    required String newPassword,
  }) => guardDio(
    () => _dio.post<void>(
      ApiEndpoints.resetPassword,
      data: {'identifiant': identifiant, 'code': code, 'motDePasse': newPassword},
    ),
  );

  Future<void> _saveTokens(Map<String, dynamic>? data) async {
    final access = data?['token'] ?? data?['accessToken'];
    if (access is! String) throw const UnknownFailure();
    await _storage.save(
      access: access,
      refresh: data?['refreshToken'] as String?,
    );
  }
}
