import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/upload.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/domain/passport_repository.dart';

/// Mock avec état en mémoire. Les communes sont un extrait réaliste (pas la liste officielle complète).
class MockPassportRepository implements PassportRepository {
  const MockPassportRepository();

  static const _regions = [
    Region(id: 'r-bko', nom: 'District de Bamako'),
    Region(id: 'r-kay', nom: 'Kayes'),
    Region(id: 'r-kou', nom: 'Koulikoro'),
    Region(id: 'r-sik', nom: 'Sikasso'),
    Region(id: 'r-seg', nom: 'Ségou'),
    Region(id: 'r-mop', nom: 'Mopti'),
    Region(id: 'r-tom', nom: 'Tombouctou'),
    Region(id: 'r-gao', nom: 'Gao'),
    Region(id: 'r-kid', nom: 'Kidal'),
    Region(id: 'r-men', nom: 'Ménaka'),
    Region(id: 'r-tao', nom: 'Taoudénit'),
  ];

  static const _communesByRegion = <String, List<String>>{
    'r-bko': [
      'Commune I',
      'Commune II',
      'Commune III',
      'Commune IV',
      'Commune V',
      'Commune VI'
    ],
    'r-kay': ['Kayes', 'Kita', 'Nioro du Sahel'],
    'r-kou': ['Koulikoro', 'Kati', 'Kalaban Coro'],
    'r-sik': ['Sikasso', 'Koutiala', 'Bougouni'],
    'r-seg': ['Ségou', 'San', 'Markala'],
    'r-mop': ['Mopti', 'Djenné', 'Bandiagara'],
    'r-tom': ['Tombouctou', 'Goundam'],
    'r-gao': ['Gao', 'Ansongo'],
    'r-kid': ['Kidal'],
    'r-men': ['Ménaka'],
    'r-tao': ['Taoudénit'],
  };

  static Citoyen _initial() => const Citoyen(
        id: 'ci-1',
        nom: 'Traoré',
        prenom: 'Aminata',
        sexe: Sexe.femme,
        codePasseport: 'MC-7K4P-29',
        disponibilite: Disponibilite.disponible,
        commune: Commune(
            id: 'r-bko-4',
            nom: 'Commune IV',
            regionId: 'r-bko',
            regionNom: 'District de Bamako'),
      );

  static Citoyen _me = _initial();

  /// Pour les tests : remet l'état initial.
  static void resetForTests() => _me = _initial();

  Future<void> _latency([int ms = 300]) =>
      Future<void>.delayed(Duration(milliseconds: ms));

  Citoyen _with(
          {String? nom,
          String? prenom,
          Sexe? sexe,
          bool setSexe = false,
          String? photoUrl,
          Disponibilite? dispo,
          Commune? commune}) =>
      _me = Citoyen(
        id: _me.id,
        nom: nom ?? _me.nom,
        prenom: prenom ?? _me.prenom,
        sexe: setSexe ? sexe : _me.sexe,
        photoUrl: photoUrl ?? _me.photoUrl,
        codePasseport: _me.codePasseport,
        disponibilite: dispo ?? _me.disponibilite,
        commune: commune ?? _me.commune,
      );

  @override
  Future<Citoyen> getMine() async {
    await _latency(400);
    return _me;
  }

  @override
  Future<Citoyen> updateProfile(
      {required String nom, required String prenom, Sexe? sexe}) async {
    await _latency();
    if (nom.trim().isEmpty || prenom.trim().isEmpty) {
      throw ValidationFailure('Le nom et le prénom sont obligatoires.');
    }
    return _with(
        nom: nom.trim(), prenom: prenom.trim(), sexe: sexe, setSexe: true);
  }

  @override
  Future<Citoyen> updateCommune(String communeId) async {
    await _latency();
    for (final r in _regions) {
      final names = _communesByRegion[r.id]!;
      for (var i = 0; i < names.length; i++) {
        if ('${r.id}-${i + 1}' == communeId) {
          return _with(
              commune: Commune(
                  id: communeId,
                  nom: names[i],
                  regionId: r.id,
                  regionNom: r.nom));
        }
      }
    }
    throw const NotFoundFailure();
  }

  @override
  Future<Citoyen> updateAvailability(Disponibilite value) async {
    await _latency();
    return _with(dispo: value);
  }

  @override
  Future<Citoyen> updatePhoto(PickedMedia media,
      {void Function(double progress)? onProgress}) async {
    await simulateUpload(
        onProgress: onProgress, stepDelay: const Duration(milliseconds: 100));
    // URL factice : l'image ne se charge pas (l'avatar affiche son icône), mais l'étape « Photo » est validée.
    return _with(photoUrl: 'https://example.invalid/mock-photo.jpg');
  }

  @override
  Future<List<Region>> listRegions() async {
    await _latency(200);
    return _regions;
  }

  @override
  Future<List<Commune>> listCommunes(String regionId) async {
    await _latency(200);
    final region = _regions.firstWhere((r) => r.id == regionId,
        orElse: () => throw const NotFoundFailure());
    final names = _communesByRegion[regionId]!;
    return [
      for (var i = 0; i < names.length; i++)
        Commune(
            id: '$regionId-${i + 1}',
            nom: names[i],
            regionId: region.id,
            regionNom: region.nom),
    ];
  }
}
