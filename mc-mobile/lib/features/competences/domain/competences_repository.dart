import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/competences/domain/competence_ref.dart';

abstract interface class CompetencesRepository {
  /// Compétences du citoyen connecté (paginé).
  Future<List<CitoyenCompetence>> list({int page = 0, int size = 20});

  /// Lève NotFoundFailure si la compétence n'existe pas ou n'appartient pas au citoyen.
  Future<CitoyenCompetence> get(String id);

  /// Ajout uniquement depuis le référentiel. État initial : déclarée.
  /// Lève ValidationFailure si la compétence est déjà déclarée.
  Future<CitoyenCompetence> add(
      {required String competenceId, required Niveau niveau});

  /// Recherche dans le référentiel (nom ou secteur), 20 résultats maximum.
  Future<List<CompetenceRef>> searchReferentiel(String query);
}
