import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_repository.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';
import 'package:mlc_mobile/features/validations/presentation/validation_providers.dart';
import 'package:mlc_mobile/features/validations/presentation/validations_list_screen.dart';

class _RecordingValidationRepository implements ValidationRepository {
  final requestedStatuses = <ValidationStatut?>[];

  @override
  Future<List<Validation>> listMine({
    ValidationStatut? statut,
    int page = 0,
    int size = 20,
  }) async {
    requestedStatuses.add(statut);
    return [];
  }

  @override
  Future<Validation> get(String id) => throw UnimplementedError();

  @override
  Future<List<Validation>> listForCompetence(String citoyenCompetenceId) =>
      throw UnimplementedError();

  @override
  Future<Validation> request({
    required String citoyenCompetenceId,
    required String preuveId,
  }) => throw UnimplementedError();
}

void main() {
  testWidgets('status selector reloads validations with the chosen filter', (
    tester,
  ) async {
    final repository = _RecordingValidationRepository();
    await tester.pumpWidget(
      ProviderScope(
        overrides: [validationRepositoryProvider.overrideWithValue(repository)],
        child: const MaterialApp(home: ValidationsListScreen()),
      ),
    );
    await tester.pumpAndSettle();

    expect(repository.requestedStatuses, [null]);
    await tester.tap(find.text('Explorer par statut'));
    await tester.pumpAndSettle();
    expect(find.text('Toutes'), findsNWidgets(2));
    expect(find.text('Approuvées'), findsOneWidget);
    expect(find.text('En attente'), findsOneWidget);
    expect(find.text('Rejetées'), findsOneWidget);

    await tester.tap(find.text('Approuvées'));
    await tester.pumpAndSettle();

    expect(find.text('Approuvées'), findsOneWidget);
    expect(repository.requestedStatuses, [null, ValidationStatut.approuvee]);
    expect(tester.takeException(), isNull);
  });
}
