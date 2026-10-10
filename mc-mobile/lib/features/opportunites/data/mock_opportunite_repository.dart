import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';
import 'package:mlc_mobile/features/opportunites/domain/opportunite_repository.dart';

class MockOpportuniteRepository implements OpportuniteRepository {
  const MockOpportuniteRepository();

  static final List<Opportunite> _items = _seed();
  static bool simulateError = false;

  static List<Opportunite> _seed() {
    final now = DateTime.now();
    return [
      Opportunite(
        id: 'opp-1',
        title: 'Formation en énergie solaire à Bamako',
        description: 'Parcours pratique de six semaines pour installer et entretenir des équipements solaires.',
        type: OpportuniteType.formationGratuite,
        status: OpportuniteStatus.publiee,
        publishedAt: now.subtract(const Duration(days: 1)),
        expirationDate: now.add(const Duration(days: 20)),
        categoryId: 'cat-formation',
        categoryName: 'Formation',
      ),
      Opportunite(
        id: 'opp-2',
        title: 'Bourse d’études professionnelles',
        description: 'Aide à la formation professionnelle ouverte aux candidats du Mali.',
        type: OpportuniteType.bourse,
        status: OpportuniteStatus.publiee,
        publishedAt: now.subtract(const Duration(days: 2)),
        expirationDate: now.add(const Duration(days: 30)),
        categoryId: 'cat-bourse',
        categoryName: 'Bourse',
      ),
      Opportunite(
        id: 'opp-3',
        title: 'Programme d’accompagnement des artisans de Ségou',
        description: 'Accompagnement individuel pour développer une activité artisanale.',
        type: OpportuniteType.accompagnement,
        status: OpportuniteStatus.publiee,
        publishedAt: now.subtract(const Duration(days: 4)),
        expirationDate: now.add(const Duration(days: 15)),
        categoryId: 'cat-programme',
        categoryName: 'Accompagnement',
      ),
      Opportunite(
        id: 'opp-4',
        title: 'Concours des métiers du numérique',
        description:
            'Présentez une réalisation et échangez avec des professionnels.',
        type: OpportuniteType.concours,
        status: OpportuniteStatus.publiee,
        publishedAt: now.subtract(const Duration(days: 6)),
        expirationDate: now.add(const Duration(days: 10)),
        categoryId: 'cat-concours',
        categoryName: 'Concours',
      ),
      Opportunite(
        id: 'opp-5',
        title: 'Annonce archivée',
        description: 'Cette annonce n’est plus ouverte.',
        type: OpportuniteType.programme,
        status: OpportuniteStatus.expiree,
        publishedAt: now.subtract(const Duration(days: 50)),
        expirationDate: now.subtract(const Duration(days: 4)),
      ),
    ];
  }

  static void resetForTests() {
    _items
      ..clear()
      ..addAll(_seed());
    simulateError = false;
  }

  Future<void> _wait() async {
    await Future<void>.delayed(const Duration(milliseconds: 420));
    if (simulateError) throw const NetworkFailure();
  }

  List<Opportunite> _page(List<Opportunite> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  @override
  Future<List<Opportunite>> list({
    OpportuniteType? type,
    String? categoryId,
    int page = 0,
    int size = 20,
  }) async {
    await _wait();
    final visible =
        _items
            .where(
              (e) =>
                  e.isVisible &&
                  e.type != OpportuniteType.insertion &&
                  (type == null || e.type == type) &&
                  (categoryId == null || e.categoryId == categoryId),
            )
            .toList()
          ..sort((a, b) => b.publishedAt.compareTo(a.publishedAt));
    return _page(visible, page, size);
  }

  @override
  Future<Opportunite> get(String id) async {
    await _wait();
    if (id == 'network-error') throw const NetworkFailure();
    return _items.firstWhere(
      (e) => e.id == id && e.isVisible && e.type != OpportuniteType.insertion,
      orElse: () => throw const NotFoundFailure(),
    );
  }
}
