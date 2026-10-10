import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/security/jwt_utils.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';

final tokenStorageProvider = Provider<TokenStorage>((ref) => SecureTokenStorage());

enum SessionStatus { unknown, authenticated, unauthenticated }

/// Source de vérité de l'état de connexion. Le routeur la consulte pour protéger les routes.
class SessionNotifier extends Notifier<SessionStatus> {
  @override
  SessionStatus build() {
    Future.microtask(restore);
    return SessionStatus.unknown;
  }

  Future<void> restore() async {
    final token = await ref.read(tokenStorageProvider).readAccess();
    state = (token == null || JwtUtils.isExpired(token))
        ? SessionStatus.unauthenticated
        : SessionStatus.authenticated;
  }

  void setAuthenticated() => state = SessionStatus.authenticated;

  /// Déconnexion volontaire OU 401 : efface les jetons puis repasse en non connecté.
  Future<void> expire() async {
    await ref.read(tokenStorageProvider).clear();
    state = SessionStatus.unauthenticated;
  }
}

final sessionProvider = NotifierProvider<SessionNotifier, SessionStatus>(SessionNotifier.new);
