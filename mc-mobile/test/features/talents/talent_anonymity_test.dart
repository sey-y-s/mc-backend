import 'package:flutter_test/flutter_test.dart';
import 'package:mlc_mobile/features/talents/domain/talent_models.dart';

void main() {
  test('talent parser keeps profile data anonymous', () {
    final talent = TalentSummary.fromJson({
      'id': 'opaque-profile-1',
      'nom': 'Nom à ignorer',
      'telephone': '+223 70 00 00 00',
      'email': 'person@example.invalid',
      'photoUrl': 'https://example.invalid/person.jpg',
      'regionNom': 'Bamako',
      'disponibilite': 'DISPONIBLE',
      'competences': [
        {'competenceNom': 'Soudure', 'niveau': 'EXPERT'},
      ],
    });

    expect(talent.id, 'opaque-profile-1');
    expect(talent.regionName, 'Bamako');
    expect(talent.skills.single.name, 'Soudure');
    expect(talent.availability.name, 'disponible');
  });
}
