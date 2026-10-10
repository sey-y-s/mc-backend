import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';
import 'package:mlc_mobile/features/auth/domain/auth_repository.dart';
import 'package:mlc_mobile/features/auth/presentation/auth_providers.dart';
import 'package:mocktail/mocktail.dart';

class _MockAuthRepository extends Mock implements AuthRepository {}

void main() {
  test(
    'logout expires the local session without waiting for the server',
    () async {
      final storage = InMemoryTokenStorage();
      await storage.save(access: 'e30.eyJleHAiOjQ3NDAwMDAwMDB9.signature');
      final repository = _MockAuthRepository();
      final logoutCompleter = Completer<void>();
      when(() => repository.logout()).thenAnswer((_) => logoutCompleter.future);
      final container = ProviderContainer(
        overrides: [
          tokenStorageProvider.overrideWith((ref) => storage),
          authRepositoryProvider.overrideWithValue(repository),
        ],
      );
      addTearDown(container.dispose);

      container.read(sessionProvider.notifier).setAuthenticated();
      final logout = container.read(authControllerProvider.notifier).logout();
      await Future<void>.delayed(Duration.zero);

      expect(container.read(sessionProvider), SessionStatus.unauthenticated);
      expect(await storage.readAccess(), isNull);

      logoutCompleter.complete();
      await logout;
    },
  );
}
