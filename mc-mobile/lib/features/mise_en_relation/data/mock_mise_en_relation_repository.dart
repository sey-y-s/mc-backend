import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_models.dart';
import 'package:mlc_mobile/features/mise_en_relation/domain/mise_en_relation_repository.dart';

class MockMiseEnRelationRepository implements MiseEnRelationRepository {
  const MockMiseEnRelationRepository();

  static const _me = 'citoyen-demo';
  static bool simulateError = false;
  static final List<MiseEnRelation> _items = _seed();

  static List<MiseEnRelation> _seed() {
    final now = DateTime.now();
    return [
      MiseEnRelation(
        id: 'relation-in-1',
        senderId: 'org-1',
        recipientId: _me,
        status: RelationStatus.enAttente,
        message:
            'Nous souhaitons échanger au sujet de votre expérience en soudure.',
        requestedAt: now.subtract(const Duration(hours: 3)),
      ),
      MiseEnRelation(
        id: 'relation-out-1',
        senderId: _me,
        recipientId: 'talent-bko-41',
        status: RelationStatus.enAttente,
        message: 'Votre expérience en soudure nous intéresse.',
        requestedAt: now.subtract(const Duration(days: 1)),
      ),
      MiseEnRelation(
        id: 'relation-in-2',
        senderId: 'org-2',
        recipientId: _me,
        status: RelationStatus.acceptee,
        senderName: 'Aminata Traoré',
        recipientName: 'Mamadou Diallo',
        contact: const RelationContact(
          telephone: '+223 70 00 00 00',
          email: 'aminata@example.invalid',
        ),
        message: 'Échange au sujet d’une formation à Sikasso.',
        requestedAt: now.subtract(const Duration(days: 5)),
        respondedAt: now.subtract(const Duration(days: 4)),
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
    await Future<void>.delayed(const Duration(milliseconds: 380));
    if (simulateError) throw const NetworkFailure();
  }

  List<MiseEnRelation> _page(List<MiseEnRelation> items, int page, int size) {
    if (page < 0 || size <= 0) throw ValidationFailure();
    final start = page * size;
    if (start >= items.length) return const [];
    return items.skip(start).take(size).toList();
  }

  @override
  Future<List<MiseEnRelation>> listReceived({
    int page = 0,
    int size = 20,
  }) async {
    await _wait();
    final items = _items.where((e) => e.recipientId == _me).toList()
      ..sort((a, b) => b.requestedAt.compareTo(a.requestedAt));
    return _page(items, page, size);
  }

  @override
  Future<List<MiseEnRelation>> listSent({int page = 0, int size = 20}) async {
    await _wait();
    final items = _items.where((e) => e.senderId == _me).toList()
      ..sort((a, b) => b.requestedAt.compareTo(a.requestedAt));
    return _page(items, page, size);
  }

  @override
  Future<MiseEnRelation> get(String id) async {
    await _wait();
    if (id == 'network-error') throw const NetworkFailure();
    return _items.firstWhere(
      (e) => e.id == id,
      orElse: () => throw const NotFoundFailure(),
    );
  }

  @override
  Future<MiseEnRelation> send(String talentId, {String? message}) async {
    await _wait();
    final exists = _items.any(
      (e) =>
          e.senderId == _me &&
          e.recipientId == talentId &&
          e.status != RelationStatus.refusee,
    );
    if (exists) {
      throw ValidationFailure('Une demande existe déjà pour ce profil.');
    }
    final created = MiseEnRelation(
      id: 'relation-${DateTime.now().microsecondsSinceEpoch}',
      senderId: _me,
      recipientId: talentId,
      status: RelationStatus.enAttente,
      message: message?.trim().isNotEmpty == true
          ? message!.trim()
          : 'Je souhaite échanger au sujet de vos compétences.',
      requestedAt: DateTime.now(),
    );
    _items.add(created);
    return created;
  }

  @override
  Future<MiseEnRelation> respond(String id, {required bool accept}) async {
    await _wait();
    final index = _items.indexWhere((e) => e.id == id);
    if (index < 0) throw const NotFoundFailure();
    final existing = _items[index];
    if (existing.recipientId != _me) throw const ForbiddenFailure();
    if (existing.status != RelationStatus.enAttente) {
      throw ValidationFailure('Cette demande a déjà reçu une réponse.');
    }
    final updated = MiseEnRelation(
      id: existing.id,
      senderId: existing.senderId,
      recipientId: existing.recipientId,
      status: accept ? RelationStatus.acceptee : RelationStatus.refusee,
      message: existing.message,
      requestedAt: existing.requestedAt,
      respondedAt: DateTime.now(),
      senderName: accept ? 'Aminata Traoré' : null,
      recipientName: accept ? 'Mamadou Diallo' : null,
      contact: accept
          ? const RelationContact(
              telephone: '+223 70 00 00 00',
              email: 'contact@example.invalid',
            )
          : null,
      followUpId: existing.followUpId,
    );
    _items[index] = updated;
    return updated;
  }
}
