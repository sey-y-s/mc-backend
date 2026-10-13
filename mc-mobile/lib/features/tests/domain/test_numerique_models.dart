enum QuestionType {
  choixUnique('CHOIX_UNIQUE', 'Choix unique'),
  choixMultiple('CHOIX_MULTIPLE', 'Choix multiples'),
  vraiFaux('VRAI_FAUX', 'Vrai ou faux'),
  texteLibre('TEXTE_LIBRE', 'Réponse libre');

  const QuestionType(this.apiCode, this.label);
  final String apiCode;
  final String label;

  static QuestionType fromApi(String? value) => QuestionType.values
      .firstWhere((e) => e.apiCode == value?.toUpperCase(),
          orElse: () => QuestionType.texteLibre);
}

class TestOption {
  const TestOption({required this.id, required this.label, this.order = 0});
  final String id;
  final String label;
  final int order;

  /// Le parseur ignore volontairement toute propriété `correcte` renvoyée par le serveur.
  factory TestOption.fromJson(Map<String, dynamic> json) => TestOption(
        id: (json['id'] ?? '').toString(),
        label: (json['texte'] ?? json['libelle'] ?? '').toString(),
        order: json['ordre'] is num ? (json['ordre'] as num).toInt() : 0,
      );
}

class TestQuestion {
  const TestQuestion({
    required this.id,
    required this.prompt,
    required this.type,
    required this.order,
    this.options = const [],
  });
  final String id;
  final String prompt;
  final QuestionType type;
  final int order;
  final List<TestOption> options;

  factory TestQuestion.fromJson(Map<String, dynamic> json) {
    final rawOptions = json['propositions'] ?? json['options'];
    return TestQuestion(
      id: (json['id'] ?? '').toString(),
      prompt: (json['texte'] ?? json['enonce'] ?? '').toString(),
      type: QuestionType.fromApi((json['type'] ?? '').toString()),
      order: json['ordre'] is num ? (json['ordre'] as num).toInt() : 0,
      options: rawOptions is List
          ? rawOptions.whereType<Map>().map((e) =>
              TestOption.fromJson(Map<String, dynamic>.from(e))).toList()
          : const [],
    );
  }
}

class ResultatTest {
  const ResultatTest({
    required this.id,
    required this.testId,
    required this.score,
    required this.passed,
    required this.date,
    this.proofId,
  });
  final String id;
  final String testId;
  final double score;
  final bool passed;
  final DateTime date;
  final String? proofId;

  factory ResultatTest.fromJson(Map<String, dynamic> json) => ResultatTest(
        id: (json['id'] ?? '').toString(),
        testId: (json['testId'] ?? '').toString(),
        score: json['score'] is num ? (json['score'] as num).toDouble() : 0,
        passed: json['reussi'] == true || json['passed'] == true,
        date: json['datePassage'] is String
            ? DateTime.tryParse(json['datePassage'] as String) ??
                DateTime.fromMillisecondsSinceEpoch(0)
            : DateTime.fromMillisecondsSinceEpoch(0),
        proofId: (json['preuveId'] ?? json['proofId'])?.toString(),
      );
}

class TestNumerique {
  const TestNumerique({
    required this.id,
    required this.title,
    required this.description,
    required this.competenceId,
    required this.competenceName,
    this.durationMinutes,
    this.questions = const [],
    this.lastResult,
  });
  final String id;
  final String title;
  final String description;
  final String competenceId;
  final String competenceName;
  final int? durationMinutes;
  final List<TestQuestion> questions;
  final ResultatTest? lastResult;

  int get questionCount => questions.length;

  TestNumerique copyWith({
    List<TestQuestion>? questions,
    ResultatTest? lastResult,
  }) =>
      TestNumerique(
        id: id,
        title: title,
        description: description,
        competenceId: competenceId,
        competenceName: competenceName,
        durationMinutes: durationMinutes,
        questions: questions ?? this.questions,
        lastResult: lastResult ?? this.lastResult,
      );

  factory TestNumerique.fromJson(Map<String, dynamic> json) {
    final competence = json['competence'];
    final rawQuestions = json['questions'];
    return TestNumerique(
      id: (json['id'] ?? '').toString(),
      title: (json['titre'] ?? json['title'] ?? '').toString(),
      description: (json['description'] ?? '').toString(),
      competenceId: (json['competenceId'] ??
              (competence is Map ? competence['id'] : null) ??
              '')
          .toString(),
      competenceName: (json['competenceNom'] ??
              (competence is Map ? competence['nom'] : null) ??
              'Compétence')
          .toString(),
      durationMinutes: json['dureeMinutes'] is num
          ? (json['dureeMinutes'] as num).toInt()
          : null,
      questions: rawQuestions is List
          ? rawQuestions.whereType<Map>().map((e) =>
              TestQuestion.fromJson(Map<String, dynamic>.from(e))).toList()
          : const [],
    );
  }
}

class TestAnswer {
  const TestAnswer({this.optionIds = const [], this.text = ''});
  final List<String> optionIds;
  final String text;
}

class TestAnswers {
  const TestAnswers(this.byQuestion);
  final Map<String, TestAnswer> byQuestion;
}
