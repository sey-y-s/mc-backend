enum OpportuniteType {
  formationGratuite('FORMATION_GRATUITE', 'Formation'),
  bourse('BOURSE', 'Bourse'),
  programme('PROGRAMME', 'Programme'),
  appelCandidature('APPEL_CANDIDATURE', 'Appel à candidatures'),
  concours('CONCOURS', 'Concours'),
  insertion('INSERTION', 'Insertion'),
  accompagnement('ACCOMPAGNEMENT', 'Accompagnement'),
  autre('AUTRE', 'Autre');

  const OpportuniteType(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static OpportuniteType fromApi(String? value) => OpportuniteType.values
      .firstWhere((e) => e.apiCode == value?.toUpperCase(),
          orElse: () => OpportuniteType.autre);
}

enum OpportuniteStatus {
  publiee('PUBLIEE', 'Publiée'),
  brouillon('BROUILLON', 'Brouillon'),
  expiree('EXPIREE', 'Expirée'),
  archivee('ARCHIVEE', 'Archivée'),
  annulee('ANNULEE', 'Annulée');

  const OpportuniteStatus(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static OpportuniteStatus fromApi(String? value) => OpportuniteStatus.values
      .firstWhere((e) => e.apiCode == value?.toUpperCase(),
          orElse: () => OpportuniteStatus.brouillon);
}

class Opportunite {
  const Opportunite({
    required this.id,
    required this.title,
    required this.description,
    required this.type,
    required this.status,
    required this.publishedAt,
    this.categoryId,
    this.categoryName,
    this.expirationDate,
    this.externalUrl,
  });
  final String id;
  final String title;
  final String description;
  final OpportuniteType type;
  final OpportuniteStatus status;
  final DateTime publishedAt;
  final String? categoryId;
  final String? categoryName;
  final DateTime? expirationDate;
  final String? externalUrl;

  bool get isVisible {
    final today = DateTime.now();
    final expiry = expirationDate;
    return status == OpportuniteStatus.publiee &&
        (expiry == null || !DateTime(expiry.year, expiry.month, expiry.day)
            .isBefore(DateTime(today.year, today.month, today.day)));
  }

  factory Opportunite.fromJson(Map<String, dynamic> json) {
    DateTime? date(Object? value) =>
        value is String ? DateTime.tryParse(value) : null;
    final category = json['categorie'];
    return Opportunite(
      id: (json['id'] ?? '').toString(),
      title: (json['titre'] ?? json['title'] ?? '').toString(),
      description: (json['description'] ?? '').toString(),
      type: OpportuniteType.fromApi(json['type']?.toString()),
      status: OpportuniteStatus.fromApi((json['statut'] ?? json['status'])?.toString()),
      publishedAt: date(json['datePublication']) ?? DateTime.fromMillisecondsSinceEpoch(0),
      categoryId: (json['categorieId'] ?? (category is Map ? category['id'] : null))?.toString(),
      categoryName: (json['categorieNom'] ?? (category is Map ? category['nom'] : null))?.toString(),
      expirationDate: date(json['dateExpiration']),
      externalUrl: (json['lienUrl'] ?? json['url'])?.toString(),
    );
  }
}

class OpportuniteFilters {
  const OpportuniteFilters({this.type, this.categoryId});
  final OpportuniteType? type;
  final String? categoryId;

  OpportuniteFilters copyWith({
    OpportuniteType? type,
    String? categoryId,
    bool clearType = false,
    bool clearCategory = false,
  }) => OpportuniteFilters(
        type: clearType ? null : (type ?? this.type),
        categoryId: clearCategory ? null : (categoryId ?? this.categoryId),
      );
}
