import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/parametres/domain/settings_models.dart';
import 'package:mlc_mobile/features/parametres/presentation/preferences_screen.dart';
import 'package:mlc_mobile/features/parametres/presentation/settings_providers.dart';

void main() {
  testWidgets('preferences fit a narrow phone screen', (tester) async {
    await tester.binding.setSurfaceSize(const Size(320, 640));
    addTearDown(() => tester.binding.setSurfaceSize(null));

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          preferencesProvider.overrideWith(
            (ref) async => const AppPreferences(),
          ),
        ],
        child: const MaterialApp(home: PreferencesScreen()),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Économie de données'), findsOneWidget);
    expect(find.text('Notifications'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
