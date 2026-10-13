import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/features/auth/presentation/auth_providers.dart';

/// Actions du mot de passe oublié. Contrairement à AuthController, aucune session n'est ouverte :
/// après la réinitialisation, l'utilisateur se reconnecte.
class PasswordResetController extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  /// Demande l'envoi d'un code. Retourne true si la demande est partie (réponse volontairement neutre côté serveur).
  Future<bool> requestCode(String identifiant) =>
      _run(() => ref.read(authRepositoryProvider).requestPasswordReset(identifiant));

  /// Retourne true si le mot de passe a été changé.
  Future<bool> reset({required String identifiant, required String code, required String newPassword}) =>
      _run(() => ref
          .read(authRepositoryProvider)
          .resetPassword(identifiant: identifiant, code: code, newPassword: newPassword));

  Future<bool> _run(Future<void> Function() action) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard<void>(action);
    return !state.hasError;
  }
}

final passwordResetControllerProvider =
    AsyncNotifierProvider.autoDispose<PasswordResetController, void>(PasswordResetController.new);