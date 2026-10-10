import 'dart:typed_data';

enum PortfolioMediaType {
  image('IMAGE', 'Photo'),
  video('VIDEO', 'Vidéo'),
  document('DOCUMENT', 'Document'),
  autre('AUTRE', 'Média');

  const PortfolioMediaType(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static PortfolioMediaType fromApi(String? value) =>
      PortfolioMediaType.values.firstWhere(
        (e) => e.apiCode == value?.toUpperCase(),
        orElse: () => PortfolioMediaType.autre,
      );
}

class PortfolioMedia {
  const PortfolioMedia({
    required this.id,
    required this.portfolioId,
    required this.type,
    required this.url,
    this.fileName,
    this.caption,
    this.localBytes,
  });

  final String id;
  final String portfolioId;
  final PortfolioMediaType type;
  final String url;
  final String? fileName;
  final String? caption;

  /// Octets gardés en mémoire uniquement par le dépôt mock pour l'aperçu local.
  final Uint8List? localBytes;

  factory PortfolioMedia.fromJson(
    Map<String, dynamic> json, {
    String portfolioId = '',
  }) => PortfolioMedia(
    id: (json['id'] ?? '').toString(),
    portfolioId: portfolioId,
    type: PortfolioMediaType.fromApi(json['type']?.toString()),
    url: (json['urlMedia'] ?? json['url'] ?? '').toString(),
    fileName: (json['nomFichier'] ?? json['fileName'])?.toString(),
    caption: (json['legende'] ?? json['caption'])?.toString(),
  );
}

class PortfolioRealisation {
  const PortfolioRealisation({
    required this.id,
    required this.title,
    required this.description,
    required this.media,
    this.date,
    this.linkUrl,
  });

  final String id;
  final String title;
  final String description;
  final DateTime? date;
  final String? linkUrl;
  final List<PortfolioMedia> media;

  factory PortfolioRealisation.fromJson(Map<String, dynamic> json) {
    final id = (json['id'] ?? '').toString();
    final rawMedia = json['medias'] ?? json['media'];
    return PortfolioRealisation(
      id: id,
      title: (json['titre'] ?? json['title'] ?? '').toString(),
      description: (json['description'] ?? '').toString(),
      date: json['dateRealisation'] is String
          ? DateTime.tryParse(json['dateRealisation'] as String)
          : null,
      linkUrl: (json['lienUrl'] ?? json['linkUrl'])?.toString(),
      media: rawMedia is List
          ? rawMedia
                .whereType<Map>()
                .map(
                  (e) => PortfolioMedia.fromJson(
                    Map<String, dynamic>.from(e),
                    portfolioId: id,
                  ),
                )
                .toList()
          : const [],
    );
  }
}

class PortfolioInput {
  const PortfolioInput({
    required this.title,
    this.description = '',
    this.date,
    this.linkUrl,
  });

  final String title;
  final String description;
  final DateTime? date;
  final String? linkUrl;
}
