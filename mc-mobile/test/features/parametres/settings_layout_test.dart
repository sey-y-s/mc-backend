import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/admin/presentation/admin_providers.dart';
import 'package:mlc_mobile/features/parametres/presentation/account_settings_screen.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_models.dart';
import 'package:mlc_mobile/features/parametres/presentation/settings_providers.dart';
import 'package:mlc_mobile/features/parametres/presentation/settings_screen.dart';

void main() {
  testWidgets('settings page is complete and fits a narrow phone screen', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(320, 640));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(
      ProviderScope(
        overrides: [currentRoleProvider.overrideWith((ref) async => 'ADMIN')],
        child: const MaterialApp(home: SettingsScreen()),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('BONJOUR'), findsNothing);
    expect(find.text('Compte actif'), findsNothing);
    expect(find.text('Mon profil'), findsNothing);
    expect(find.text('Votre espace'), findsOneWidget);
    expect(find.text('Application'), findsOneWidget);
    expect(find.text('Administration'), findsOneWidget);
    expect(find.text('Se déconnecter'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('account screen shows only one app bar and back arrow', (
    tester,
  ) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          accountProvider.overrideWith(
            (ref) async =>
                const UserAccount(id: 'account-1', telephone: '+22370000000'),
          ),
        ],
        child: MaterialApp(
          home: Builder(
            builder: (context) => Scaffold(
              body: TextButton(
                onPressed: () => Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => const AccountSettingsScreen(),
                  ),
                ),
                child: const Text('Ouvrir le compte'),
              ),
            ),
          ),
        ),
      ),
    );
    await tester.tap(find.text('Ouvrir le compte'));
    await tester.pumpAndSettle();

    expect(find.byType(AppBar), findsOneWidget);
    expect(find.byIcon(Icons.arrow_back), findsOneWidget);
    expect(find.text('Mon compte'), findsOneWidget);
    expect(find.text('Coordonnées'), findsNothing);
    expect(tester.takeException(), isNull);
  });
}
