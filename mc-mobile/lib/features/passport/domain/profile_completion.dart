import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';

class CompletionStep {
  const CompletionStep(
      {required this.title,
      required this.hint,
      required this.done,
      required this.route});
  final String title; // « Photo »
  final String
      hint; // affiché tant que l'étape n'est pas faite : « Ajoutez une photo de vous »
  final bool done;
  final String route;
}

/// Complétude du passeport (KPI « taux de profils complétés »). Calcul côté mobile, sans appel réseau.
class ProfileCompletion {
  const ProfileCompletion(this.steps);
  final List<CompletionStep> steps;

  double get ratio =>
      steps.isEmpty ? 0 : steps.where((s) => s.done).length / steps.length;
  bool get isComplete => steps.every((s) => s.done);
  CompletionStep? get next {
    for (final s in steps) {
      if (!s.done) return s;
    }
    return null;
  }

  factory ProfileCompletion.compute(
          Citoyen c, List<CitoyenCompetence> competences) =>
      ProfileCompletion([
        CompletionStep(
            title: 'Identité',
            hint: 'Précisez votre sexe',
            done: c.sexe != null,
            route: '/passeport/profil'),
        CompletionStep(
            title: 'Photo',
            hint: 'Ajoutez une photo de vous',
            done: c.photoUrl != null,
            route: '/passeport/profil'),
        CompletionStep(
            title: 'Localisation',
            hint: 'Indiquez votre commune',
            done: c.commune != null,
            route: '/passeport/localisation'),
        CompletionStep(
            title: 'Première compétence',
            hint: 'Déclarez une compétence',
            done: competences.isNotEmpty,
            route: '/competences/ajouter'),
        CompletionStep(
          title: 'Première preuve',
          hint: 'Ajoutez une preuve à une compétence',
          done: competences.any((x) => x.etat != EtatCompetence.declaree),
          route: '/preuves/ajouter',
        ),
      ]);
}
