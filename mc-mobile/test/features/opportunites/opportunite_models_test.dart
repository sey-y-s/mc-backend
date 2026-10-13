import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';

void main() {
  group('OpportuniteType', () {
    test('fromApi parses known types properly', () {
      expect(
        OpportuniteType.fromApi('FORMATION_GRATUITE'),
        OpportuniteType.formationGratuite,
      );
      expect(OpportuniteType.fromApi('BOURSE'), OpportuniteType.bourse);
      expect(OpportuniteType.fromApi('PROGRAMME'), OpportuniteType.programme);
      expect(
        OpportuniteType.fromApi('APPEL_CANDIDATURE'),
        OpportuniteType.appelCandidature,
      );
      expect(OpportuniteType.fromApi('CONCOURS'), OpportuniteType.concours);
    });

    test('fromApi falls back to autre on unknown or null', () {
      expect(OpportuniteType.fromApi('INCONNU'), OpportuniteType.autre);
      expect(OpportuniteType.fromApi(null), OpportuniteType.autre);
    });
  });

  group('OpportuniteStatus', () {
    test('fromApi parses status correctly', () {
      expect(OpportuniteStatus.fromApi('PUBLIEE'), OpportuniteStatus.publiee);
      expect(OpportuniteStatus.fromApi('EXPIREE'), OpportuniteStatus.expiree);
    });

    test('fromApi falls back to brouillon on unknown', () {
      expect(OpportuniteStatus.fromApi('UNKNOWN'), OpportuniteStatus.brouillon);
    });
  });

  group('Opportunite', () {
    test('fromJson parses correctly', () {
      final json = {
        'id': 'opp-99',
        'titre': 'Formation en maraîchage',
        'description': 'Formation certifiante',
        'type': 'FORMATION_GRATUITE',
        'statut': 'PUBLIEE',
        'datePublication': '2026-06-01T10:00:00.000Z',
        'categorieNom': 'Agriculture',
        'dateExpiration': '2026-08-01T10:00:00.000Z',
        'lienUrl': 'https://example.com/apply',
      };

      final opp = Opportunite.fromJson(json);

      expect(opp.id, 'opp-99');
      expect(opp.title, 'Formation en maraîchage');
      expect(opp.type, OpportuniteType.formationGratuite);
      expect(opp.status, OpportuniteStatus.publiee);
      expect(opp.categoryName, 'Agriculture');
      expect(opp.externalUrl, 'https://example.com/apply');
    });

    test('isVisible returns false for expired opportunity', () {
      final opp = Opportunite(
        id: 'opp-old',
        title: 'Expirée',
        description: 'Desc',
        type: OpportuniteType.bourse,
        status: OpportuniteStatus.publiee,
        publishedAt: DateTime(2025, 1, 1),
        expirationDate: DateTime(2025, 2, 1),
      );

      expect(opp.isVisible, isFalse);
    });
  });
}
