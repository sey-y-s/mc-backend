import 'package:mlc_mobile/features/opportunites/domain/opportunite_models.dart';

class AdminDashboard {
  const AdminDashboard({
    required this.citoyens,
    required this.organisations,
    required this.validations,
    required this.tests,
  });

  final int citoyens;
  final int organisations;
  final int validations;
  final int tests;

  factory AdminDashboard.fromJson(Map<String, dynamic> json) => AdminDashboard(
    citoyens: _asInt(json['citoyens']),
    organisations: _asInt(json['organisations']),
    validations: _asInt(json['validations']),
    tests: _asInt(json['tests']),
  );
}

class AdminUser {
  const AdminUser({
    required this.id,
    required this.role,
    required this.actif,
    this.email,
    this.telephone,
  });

  final String id;
  final String role;
  final bool actif;
  final String? email;
  final String? telephone;

  factory AdminUser.fromJson(Map<String, dynamic> json) => AdminUser(
    id: (json['id'] ?? '').toString(),
    role: (json['role'] ?? '').toString(),
    actif: json['actif'] == true,
    email: json['email']?.toString(),
    telephone: json['telephone']?.toString(),
  );
}

class AdminValidation {
  const AdminValidation({
    required this.id,
    required this.type,
    required this.label,
    required this.status,
  });

  final String id;
  final String type;
  final String label;
  final String status;

  factory AdminValidation.fromJson(Map<String, dynamic> json) =>
      AdminValidation(
        id: (json['id'] ?? '').toString(),
        type: (json['type'] ?? '').toString(),
        label: (json['libelle'] ?? '').toString(),
        status: (json['statut'] ?? '').toString(),
      );
}

class AdminOrganization {
  const AdminOrganization({
    required this.id,
    required this.name,
    required this.status,
    this.email,
    this.description,
  });

  final String id;
  final String name;
  final String status;
  final String? email;
  final String? description;

  factory AdminOrganization.fromJson(Map<String, dynamic> json) =>
      AdminOrganization(
        id: (json['id'] ?? '').toString(),
        name: (json['nom'] ?? '').toString(),
        status: (json['statut'] ?? '').toString(),
        email: json['email']?.toString(),
        description: json['description']?.toString(),
      );
}

class AdminQcmAnswer {
  const AdminQcmAnswer({this.id, required this.text, required this.correct});

  final String? id;
  final String text;
  final bool correct;

  factory AdminQcmAnswer.fromJson(Map<String, dynamic> json) => AdminQcmAnswer(
    id: json['id']?.toString(),
    text: (json['libelle'] ?? json['texte'] ?? '').toString(),
    correct: json['estCorrecte'] == true || json['correcte'] == true,
  );
}

class AdminQcmQuestion {
  const AdminQcmQuestion({
    this.id,
    required this.text,
    required this.order,
    required this.answers,
  });

  final String? id;
  final String text;
  final int order;
  final List<AdminQcmAnswer> answers;

  factory AdminQcmQuestion.fromJson(Map<String, dynamic> json) {
    final propositions = json['propositions'];
    return AdminQcmQuestion(
      id: json['id']?.toString(),
      text: (json['libelle'] ?? json['texte'] ?? '').toString(),
      order: _asInt(json['ordre']),
      answers: propositions is List
          ? propositions
                .whereType<Map>()
                .map(
                  (item) => AdminQcmAnswer.fromJson(
                    Map<String, dynamic>.from(item),
                  ),
                )
                .toList()
          : const [],
    );
  }
}

class AdminQcmTest {
  const AdminQcmTest({
    required this.id,
    required this.competenceId,
    required this.title,
    required this.description,
    required this.status,
    required this.questions,
  });

  final String id;
  final String competenceId;
  final String title;
  final String description;
  final String status;
  final List<AdminQcmQuestion> questions;

  factory AdminQcmTest.fromJson(Map<String, dynamic> json) {
    final questions = json['questions'];
    return AdminQcmTest(
      id: (json['id'] ?? '').toString(),
      competenceId: (json['competenceId'] ?? '').toString(),
      title: (json['titre'] ?? '').toString(),
      description: (json['description'] ?? '').toString(),
      status: (json['statut'] ?? '').toString(),
      questions: questions is List
          ? questions
                .whereType<Map>()
                .map(
                  (item) => AdminQcmQuestion.fromJson(
                    Map<String, dynamic>.from(item),
                  ),
                )
                .toList()
          : const [],
    );
  }
}

class AdminCompetence {
  const AdminCompetence({required this.id, required this.name});

  final String id;
  final String name;

  factory AdminCompetence.fromJson(Map<String, dynamic> json) =>
      AdminCompetence(
        id: (json['id'] ?? '').toString(),
        name: (json['nom'] ?? '').toString(),
      );
}

class AdminOpportunityCategory {
  const AdminOpportunityCategory({required this.id, required this.name});

  final String id;
  final String name;

  factory AdminOpportunityCategory.fromJson(Map<String, dynamic> json) =>
      AdminOpportunityCategory(
        id: (json['id'] ?? '').toString(),
        name: (json['nom'] ?? '').toString(),
      );
}

class AdminOpportunityDraft {
  const AdminOpportunityDraft({
    this.id,
    required this.categoryId,
    required this.title,
    required this.description,
    required this.type,
    required this.status,
    this.expirationDate,
  });

  final String? id;
  final String categoryId;
  final String title;
  final String description;
  final OpportuniteType type;
  final OpportuniteStatus status;
  final DateTime? expirationDate;

  factory AdminOpportunityDraft.fromJson(Map<String, dynamic> json) {
    final opportunity = Opportunite.fromJson(json);
    return AdminOpportunityDraft(
      id: opportunity.id,
      categoryId: opportunity.categoryId ?? '',
      title: opportunity.title,
      description: opportunity.description,
      type: opportunity.type,
      status: opportunity.status,
      expirationDate: opportunity.expirationDate,
    );
  }
}

int _asInt(Object? value) => value is num ? value.toInt() : 0;
