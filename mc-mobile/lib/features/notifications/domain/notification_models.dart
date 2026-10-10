enum NotificationType {
  miseEnRelation('MISE_EN_RELATION', 'Mise en relation'),
  validation('VALIDATION', 'Validation'),
  opportunite('OPPORTUNITE', 'Opportunité'),
  besoinCompetence('BESOIN_COMPETENCE', 'Besoin de compétences'),
  systeme('SYSTEME', 'Information');

  const NotificationType(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static NotificationType fromApi(String? value) =>
      NotificationType.values.firstWhere(
        (e) => e.apiCode == value?.toUpperCase(),
        orElse: () => NotificationType.systeme,
      );
}

class AppNotification {
  const AppNotification({
    required this.id,
    required this.notificationId,
    required this.title,
    required this.message,
    required this.type,
    required this.receivedAt,
    required this.createdAt,
    required this.isRead,
    this.readAt,
    this.referenceId,
  });

  /// Identifiant de la liaison destinataire, utilisé par l'API pour marquer lu.
  final String id;
  final String notificationId;
  final String title;
  final String message;
  final NotificationType type;
  final DateTime receivedAt;
  final DateTime createdAt;
  final bool isRead;
  final DateTime? readAt;
  final String? referenceId;

  AppNotification copyWith({bool? isRead, DateTime? readAt}) => AppNotification(
    id: id,
    notificationId: notificationId,
    title: title,
    message: message,
    type: type,
    receivedAt: receivedAt,
    createdAt: createdAt,
    isRead: isRead ?? this.isRead,
    readAt: readAt ?? this.readAt,
    referenceId: referenceId,
  );

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    final fallback = DateTime.fromMillisecondsSinceEpoch(0);
    DateTime date(Object? value, DateTime defaultValue) {
      if (value is DateTime) return value;
      if (value is String) return DateTime.tryParse(value) ?? defaultValue;
      return defaultValue;
    }

    return AppNotification(
      id: (json['destinataireId'] ?? json['id'] ?? '').toString(),
      notificationId: (json['id'] ?? '').toString(),
      title: (json['titre'] ?? json['title'] ?? '').toString(),
      message: (json['message'] ?? '').toString(),
      type: NotificationType.fromApi(json['type']?.toString()),
      receivedAt: date(
        json['dateReception'],
        date(json['dateCreation'], fallback),
      ),
      createdAt: date(json['dateCreation'], fallback),
      isRead: json['lu'] == true || json['isRead'] == true,
      readAt: json['dateLecture'] == null
          ? null
          : date(json['dateLecture'], fallback),
      referenceId: (json['referenceId'] ?? json['objetId'] ?? json['reference'])
          ?.toString(),
    );
  }
}
