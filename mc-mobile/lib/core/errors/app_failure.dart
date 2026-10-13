import 'package:dio/dio.dart';

/// Erreur métier/technique normalisée. Les repositories ne laissent pas remonter Dio brut.
sealed class AppFailure implements Exception {
  const AppFailure(this.message);
  final String message;

  factory AppFailure.fromDio(DioException e) {
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.connectionError:
        return const NetworkFailure();
      case DioExceptionType.badResponse:
        final code = e.response?.statusCode ?? 0;
        if (code == 401) return const UnauthorizedFailure();
        if (code == 403) return const ForbiddenFailure();
        if (code == 404) return const NotFoundFailure();
        if (code == 400 || code == 409 || code == 422) {
          return ValidationFailure(_serverMessage(e));
        }
        if (code >= 500) return const ServerFailure();
        return const UnknownFailure();
      default:
        return const UnknownFailure();
    }
  }

  static String? _serverMessage(DioException e) {
    final data = e.response?.data;
    if (data is Map && data['message'] is String) {
      return data['message'] as String;
    }
    return null;
  }
}

final class NetworkFailure extends AppFailure {
  const NetworkFailure()
    : super('Pas de connexion. Vérifiez votre réseau et réessayez.');
}

final class ServerFailure extends AppFailure {
  const ServerFailure()
    : super('Le service est momentanément indisponible. Réessayez plus tard.');
}

final class UnauthorizedFailure extends AppFailure {
  const UnauthorizedFailure()
    : super('Votre session a expiré. Connectez-vous de nouveau.');
}

final class ForbiddenFailure extends AppFailure {
  const ForbiddenFailure()
    : super("Vous n'avez pas accès à cette information.");
}

final class NotFoundFailure extends AppFailure {
  const NotFoundFailure([String? message])
    : super(message ?? 'Élément introuvable.');
}

final class ValidationFailure extends AppFailure {
  ValidationFailure([String? serverMessage])
    : super(serverMessage ?? 'Certaines informations sont invalides.');
}

final class InvalidCredentialsFailure extends AppFailure {
  const InvalidCredentialsFailure()
    : super('Identifiant ou mot de passe incorrect.');
}

final class FileFailure extends AppFailure {
  const FileFailure(super.message);
}

final class FeatureUnavailableFailure extends AppFailure {
  const FeatureUnavailableFailure(super.message);
}

final class UnknownFailure extends AppFailure {
  const UnknownFailure() : super('Une erreur est survenue. Réessayez.');
}

/// Message sûr à afficher à l'utilisateur pour n'importe quelle erreur.
String failureMessage(Object error) =>
    error is AppFailure ? error.message : const UnknownFailure().message;
