import 'dart:convert';

class JwtUtils {
  const JwtUtils._();

  /// Lecture côté client uniquement. Le serveur reste l'autorité sur le jeton.
  static String? subject(String? token) {
    try {
      final parts = token?.split('.');
      if (parts == null || parts.length != 3) return null;
      final payload = utf8.decode(
        base64Url.decode(base64Url.normalize(parts[1])),
      );
      final subject = (jsonDecode(payload) as Map<String, dynamic>)['sub'];
      return subject is String && subject.isNotEmpty ? subject : null;
    } catch (_) {
      return null;
    }
  }

  static String? role(String? token) {
    try {
      final parts = token?.split('.');
      if (parts == null || parts.length != 3) return null;
      final payload = utf8.decode(
        base64Url.decode(base64Url.normalize(parts[1])),
      );
      final role = (jsonDecode(payload) as Map<String, dynamic>)['role'];
      return role is String ? role : null;
    } catch (_) {
      return null;
    }
  }

  /// Lecture de `exp` côté client uniquement pour éviter un appel inutile.
  /// Un jeton illisible n'est pas considéré expiré : le serveur décidera avec un 401.
  static bool isExpired(String token, {DateTime? now}) {
    try {
      final parts = token.split('.');
      if (parts.length != 3) return false;
      final payload = utf8.decode(
        base64Url.decode(base64Url.normalize(parts[1])),
      );
      final exp = (jsonDecode(payload) as Map<String, dynamic>)['exp'];
      if (exp is! int) return false;
      final expiry = DateTime.fromMillisecondsSinceEpoch(
        exp * 1000,
        isUtc: true,
      );
      return (now ?? DateTime.now().toUtc()).isAfter(expiry);
    } catch (_) {
      return false;
    }
  }
}
