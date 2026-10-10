import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/network/upload.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_models.dart';
import 'package:mlc_mobile/features/portfolio/domain/portfolio_repository.dart';

class MockPortfolioRepository implements PortfolioRepository {
  const MockPortfolioRepository();

  static bool simulateError = false;
  static final List<PortfolioRealisation> _items = _seed();

  static List<PortfolioRealisation> _seed() {
    final now = DateTime.now();
    return [
      PortfolioRealisation(
        id: 'portfolio-1',
        title: 'Installation solaire à Bamako',
        description:
            'Mise en place de panneaux et raccordement d’un petit atelier.',
        date: now.subtract(const Duration(days: 45)),
        linkUrl: 'https://example.invalid/solaire',
        media: const [
          PortfolioMedia(
            id: 'media-1',
            portfolioId: 'portfolio-1',
            type: PortfolioMediaType.image,
            url: '',
            fileName: 'panneau-solaire.jpg',
          ),
        ],
      ),
      PortfolioRealisation(
        id: 'portfolio-2',
        title: 'Ensemble bogolan',
        description: 'Confection d’un ensemble traditionnel avec finitions cousues main.',
        date: now.subtract(const Duration(days: 120)),
        media: const [
          PortfolioMedia(
            id: 'media-2',
            portfolioId: 'portfolio-2',
            type: PortfolioMediaType.document,
            url: '',
            fileName: 'fiche-bogolan.pdf',
          ),
        ],
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
    await Future<void>.delayed(const Duration(milliseconds: 360));
    if (simulateError) throw const NetworkFailure();
  }

  List<T> _page<T>(List<T> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  int _index(String id) => _items.indexWhere((e) => e.id == id);

  @override
  Future<List<PortfolioRealisation>> list({int page = 0, int size = 20}) async {
    await _wait();
    final sorted = [
      ..._items,
    ]..sort((a, b) => (b.date ?? DateTime(0)).compareTo(a.date ?? DateTime(0)));
    return _page(sorted, page, size);
  }

  @override
  Future<PortfolioRealisation> get(String id) async {
    await _wait();
    if (id == 'network-error') throw const NetworkFailure();
    final index = _index(id);
    if (index < 0) throw const NotFoundFailure();
    return _items[index];
  }

  @override
  Future<PortfolioRealisation> create(PortfolioInput input) async {
    await _wait();
    final id = 'portfolio-${DateTime.now().microsecondsSinceEpoch}';
    final item = PortfolioRealisation(
      id: id,
      title: input.title.trim(),
      description: input.description.trim(),
      date: input.date,
      linkUrl: input.linkUrl,
      media: const [],
    );
    _items.add(item);
    return item;
  }

  @override
  Future<PortfolioRealisation> update(String id, PortfolioInput input) async {
    await _wait();
    final index = _index(id);
    if (index < 0) throw const NotFoundFailure();
    final old = _items[index];
    final item = PortfolioRealisation(
      id: id,
      title: input.title.trim(),
      description: input.description.trim(),
      date: input.date,
      linkUrl: input.linkUrl,
      media: old.media,
    );
    _items[index] = item;
    return item;
  }

  @override
  Future<void> delete(String id) async {
    await _wait();
    final index = _index(id);
    if (index < 0) throw const NotFoundFailure();
    _items.removeAt(index);
  }

  @override
  Future<PortfolioMedia> addMedia(
    String portfolioId,
    PickedMedia file, {
    void Function(double progress)? onProgress,
  }) async {
    await _wait();
    final index = _index(portfolioId);
    if (index < 0) throw const NotFoundFailure();
    await simulateUpload(
      onProgress: onProgress,
      stepDelay: const Duration(milliseconds: 90),
    );
    final type = switch (file.kind) {
      MediaKind.image => PortfolioMediaType.image,
      MediaKind.video => PortfolioMediaType.video,
      MediaKind.document => PortfolioMediaType.document,
    };
    final media = PortfolioMedia(
      id: 'media-${DateTime.now().microsecondsSinceEpoch}',
      portfolioId: portfolioId,
      type: type,
      url: '',
      fileName: file.name,
      localBytes: file.bytes,
    );
    final old = _items[index];
    _items[index] = PortfolioRealisation(
      id: old.id,
      title: old.title,
      description: old.description,
      date: old.date,
      linkUrl: old.linkUrl,
      media: [...old.media, media],
    );
    return media;
  }

  @override
  Future<void> removeMedia(String portfolioId, String mediaId) async {
    await _wait();
    final index = _index(portfolioId);
    if (index < 0) throw const NotFoundFailure();
    final old = _items[index];
    if (!old.media.any((e) => e.id == mediaId)) throw const NotFoundFailure();
    _items[index] = PortfolioRealisation(
      id: old.id,
      title: old.title,
      description: old.description,
      date: old.date,
      linkUrl: old.linkUrl,
      media: old.media.where((e) => e.id != mediaId).toList(),
    );
  }
}
