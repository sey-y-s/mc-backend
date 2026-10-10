import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/opportunites/data/mock_opportunite_repository.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';

void main() {
  late MockOpportuniteRepository repository;

  setUp(() {
    MockOpportuniteRepository.resetForTests();
    repository = const MockOpportuniteRepository();
  });

  group('MockOpportuniteRepository', () {
    test('list returns visible opportunities', () async {
      final items = await repository.list();
      expect(items, isNotEmpty);
      for (final item in items) {
        expect(item.isVisible, isTrue);
      }
    });

    test('list filters by type correctly', () async {
      final formations = await repository.list(
        type: OpportuniteType.formationGratuite,
      );
      expect(formations, isNotEmpty);
      for (final f in formations) {
        expect(f.type, OpportuniteType.formationGratuite);
      }
    });

    test('get returns single item or throws NotFoundFailure', () async {
      final opp = await repository.get('opp-1');
      expect(opp.id, 'opp-1');

      expect(
        () => repository.get('opp-unknown-xyz'),
        throwsA(isA<NotFoundFailure>()),
      );
    });

    test('get throws NetworkFailure on special error id', () async {
      expect(
        () => repository.get('network-error'),
        throwsA(isA<NetworkFailure>()),
      );
    });
  });
}
