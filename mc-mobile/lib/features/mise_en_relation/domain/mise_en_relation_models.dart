enum RelationStatus {
  enAttente('EN_ATTENTE', 'En attente'),
  acceptee('ACCEPTEE', 'Acceptée'),
  refusee('REFUSEE', 'Refusée');

  const RelationStatus(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static RelationStatus fromApi(String? value) => RelationStatus.values
      .firstWhere((e) => e.apiCode == value?.toUpperCase(),
          orElse: () => RelationStatus.enAttente);
}

class RelationContact {
  const RelationContact({this.telephone, this.email});
  final String? telephone;
  final String? email;

  factory RelationContact.fromJson(Map<String, dynamic> json) => RelationContact(
        telephone: (json['telephone'] ?? json['phone'])?.toString(),
        email: json['email']?.toString(),
      );

  bool get isEmpty => (telephone == null || telephone!.isEmpty) &&
      (email == null || email!.isEmpty);
}

class MiseEnRelation {
  const MiseEnRelation({
    required this.id,
    required this.senderId,
    required this.recipientId,
    required this.status,
    required this.message,
    required this.requestedAt,
    this.respondedAt,
    this.senderName,
    this.recipientName,
    this.contact,
    this.followUpId,
  });

  final String id;
  final String senderId;
  final String recipientId;
  final RelationStatus status;
  final String message;
  final DateTime requestedAt;
  final DateTime? respondedAt;
  final String? senderName;
  final String? recipientName;
  final RelationContact? contact;
  final String? followUpId;

  String interlocutorNameFor(String currentUserId) {
    if (status != RelationStatus.acceptee) return 'Profil anonyme';
    return senderId == currentUserId
        ? (recipientName ?? 'Talent')
        : (senderName ?? 'Demandeur');
  }

  factory MiseEnRelation.fromJson(Map<String, dynamic> json) {
    DateTime date(Object? value) => value is String
        ? DateTime.tryParse(value) ?? DateTime.fromMillisecondsSinceEpoch(0)
        : DateTime.fromMillisecondsSinceEpoch(0);
    final status = RelationStatus.fromApi(json['statut']?.toString());
    final rawContact = json['contact'];
    final contact = status == RelationStatus.acceptee
        ? (rawContact is Map
            ? RelationContact.fromJson(Map<String, dynamic>.from(rawContact))
            : RelationContact.fromJson(json))
        : null;
    return MiseEnRelation(
      id: (json['id'] ?? '').toString(),
      senderId: (json['demandeurId'] ?? '').toString(),
      recipientId: (json['destinataireId'] ?? '').toString(),
      status: status,
      message: (json['message'] ?? '').toString(),
      requestedAt: date(json['dateDemande']),
      respondedAt: json['dateReponse'] == null ? null : date(json['dateReponse']),
      senderName: status == RelationStatus.acceptee
          ? json['demandeurNomComplet']?.toString()
          : null,
      recipientName: status == RelationStatus.acceptee
          ? json['destinataireNomComplet']?.toString()
          : null,
      followUpId: (json['suiviBesoinTalentId'] ?? json['SuiviBesoinTalentId'])?.toString(),
      contact: contact?.isEmpty == true ? null : contact,
    );
  }
}
