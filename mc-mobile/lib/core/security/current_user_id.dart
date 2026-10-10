import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/security/jwt_utils.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';

Future<String> requireCurrentUserId(TokenStorage storage) async {
  final id = JwtUtils.subject(await storage.readAccess());
  if (id == null) throw const UnauthorizedFailure();
  return id;
}
