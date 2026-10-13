import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_models.dart';

void main() {
  test('pending relation hides names and coordinates', () {
    final relation = MiseEnRelation.fromJson({
      'id': 'request-1',
      'demandeurId': 'user-1',
      'demandeurNomComplet': 'Nom demandeur',
      'destinataireId': 'user-2',
      'destinataireNomComplet': 'Nom destinataire',
      'statut': 'EN_ATTENTE',
      'telephone': '+223 70 00 00 00',
      'email': 'person@example.invalid',
      'message': 'Échange souhaité',
      'dateDemande': '2026-05-10T10:00:00Z',
    });

    expect(relation.senderName, isNull);
    expect(relation.recipientName, isNull);
    expect(relation.contact, isNull);
  });

  test(
    'accepted relation exposes only coordinates present in server response',
    () {
      final relation = MiseEnRelation.fromJson({
        'id': 'request-2',
        'demandeurId': 'user-1',
        'demandeurNomComplet': 'Nom demandeur',
        'destinataireId': 'user-2',
        'destinataireNomComplet': 'Nom destinataire',
        'statut': 'ACCEPTEE',
        'telephone': '+223 70 00 00 00',
        'message': 'Échange souhaité',
        'dateDemande': '2026-05-10T10:00:00Z',
      });

      expect(relation.senderName, 'Nom demandeur');
      expect(relation.contact?.telephone, '+223 70 00 00 00');
      expect(relation.contact?.email, isNull);
    },
  );
}
