import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/app/app.dart';
import 'package:mlc_mobile/core/security/session.dart';
import 'package:mlc_mobile/core/storage/token_storage.dart';

void main() {
  testWidgets('Une session vide ouvre la page de connexion', (tester) async {
    final storage = InMemoryTokenStorage();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [tokenStorageProvider.overrideWith((ref) => storage)],
        child: const MaliCompetencesApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Bienvenue'), findsOneWidget);
  });
}
