import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/talents/domain/talent_models.dart';
import 'package:mlc_mobile/features/talents/domain/talent_repository.dart';

class MockTalentRepository implements TalentRepository {
  const MockTalentRepository();

  static bool simulateError = false;
  static final List<TalentProfileAnonymized> _profiles = _seed();

  static List<TalentProfileAnonymized> _seed() {
    TalentCompetence skill(
      String id,
      String name,
      Niveau level, {
      bool validated = false,
      bool proof = false,
    }) => TalentCompetence(
      name: name,
      competenceId: id,
      level: level,
      validated: validated,
      hasEvidence: proof,
    );
    TalentSummary summary(
      String id,
      List<TalentCompetence> skills,
      String regionId,
      String region,
      String communeId,
      String commune,
      TalentAvailability availability,
      int validations,
      int proofs,
      bool portfolio,
      List<String> metiers,
    ) => TalentSummary(
      id: id,
      skills: skills,
      regionId: regionId,
      regionName: region,
      communeId: communeId,
      communeName: commune,
      availability: availability,
      validationCount: validations,
      evidenceCount: proofs,
      hasPortfolio: portfolio,
      metierIds: metiers,
    );
    return [
      TalentProfileAnonymized(
        summary: summary(
          'talent-bko-41',
          [
            skill(
              'comp-soudure',
              'Soudure à l’arc',
              Niveau.expert,
              validated: true,
              proof: true,
            ),
            skill(
              'comp-metallurgie',
              'Travail des métaux',
              Niveau.intermediaire,
              proof: true,
            ),
          ],
          'region-bamako',
          'Bamako',
          'commune-bamako-2',
          'Commune IV',
          TalentAvailability.disponible,
          3,
          4,
          true,
          ['metier-soudeur'],
        ),
        portfolioTitles: const ['Portail métallique', 'Support solaire'],
      ),
      TalentProfileAnonymized(
        summary: summary(
          'talent-kys-28',
          [
            skill(
              'comp-couture',
              'Couture professionnelle',
              Niveau.intermediaire,
              validated: true,
              proof: true,
            ),
            skill('comp-gestion', 'Gestion de stock', Niveau.debutant),
          ],
          'region-kayes',
          'Kayes',
          'commune-kayes',
          'Kayes',
          TalentAvailability.bientotDisponible,
          1,
          2,
          true,
          ['metier-couturier'],
        ),
        portfolioTitles: const ['Collection bogolan'],
      ),
      TalentProfileAnonymized(
        summary: summary(
          'talent-sik-73',
          [
            skill(
              'comp-solaire',
              'Installation solaire',
              Niveau.expert,
              validated: true,
              proof: true,
            ),
            skill(
              'comp-electricite',
              'Électricité bâtiment',
              Niveau.intermediaire,
              validated: true,
            ),
          ],
          'region-sikasso',
          'Sikasso',
          'commune-sikasso',
          'Sikasso',
          TalentAvailability.disponible,
          2,
          3,
          false,
          ['metier-electricien'],
        ),
      ),
      TalentProfileAnonymized(
        summary: summary(
          'talent-seg-16',
          [
            skill(
              'comp-menuiserie',
              'Menuiserie bois',
              Niveau.debutant,
              proof: true,
            ),
          ],
          'region-segou',
          'Ségou',
          'commune-segou',
          'Ségou',
          TalentAvailability.indisponible,
          0,
          1,
          false,
          ['metier-menuisier'],
        ),
      ),
    ];
  }

  static void resetForTests() => simulateError = false;

  Future<void> _wait() async {
    await Future<void>.delayed(const Duration(milliseconds: 400));
    if (simulateError) throw const NetworkFailure();
  }

  bool _matches(TalentSummary talent, TalentFilters filters) {
    final q = filters.query.trim().toLowerCase();
    return (q.isEmpty ||
            talent.skills.any((s) => s.name.toLowerCase().contains(q)) ||
            talent.metierIds.any((m) => m.toLowerCase().contains(q))) &&
        (filters.competenceId == null ||
            talent.skills.any((s) => s.competenceId == filters.competenceId)) &&
        (filters.metierId == null ||
            talent.metierIds.contains(filters.metierId)) &&
        (filters.regionId == null || talent.regionId == filters.regionId) &&
        (filters.communeId == null || talent.communeId == filters.communeId) &&
        (filters.availability == null ||
            talent.availability == filters.availability) &&
        (filters.minimumLevel == null ||
            talent.skills.any(
              (s) => s.level.index >= filters.minimumLevel!.index,
            ));
  }

  @override
  Future<List<TalentSummary>> search(
    TalentFilters filters, {
    int page = 0,
    int size = 20,
  }) async {
    await _wait();
    if (filters.query == 'network-error') throw const NetworkFailure();
    if (page < 0 || size <= 0) throw ValidationFailure();
    final matches = _profiles
        .map((e) => e.summary)
        .where((e) => _matches(e, filters))
        .toList();
    return matches.skip(page * size).take(size).toList();
  }

  @override
  Future<TalentProfileAnonymized> getAnonymized(String id) async {
    await _wait();
    if (id == 'network-error') throw const NetworkFailure();
    return _profiles.firstWhere(
      (e) => e.summary.id == id,
      orElse: () => throw const NotFoundFailure(),
    );
  }
}
