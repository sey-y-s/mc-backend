/// Règles de saisie d'une expérience, partagées par le formulaire et les mocks.
class ExperienceValidators {
  const ExperienceValidators._();

  static const titreMaxLength = 120;

  static String? titre(String? v) {
    final t = v?.trim() ?? '';
    if (t.isEmpty) return 'Le titre est obligatoire';
    if (t.length > titreMaxLength) return 'Maximum $titreMaxLength caractères';
    return null;
  }

  static DateTime _day(DateTime d) => DateTime(d.year, d.month, d.day);

  /// Erreurs par champ (`dateDebut`, `dateFin`) ; vide si tout est valide.
  /// Quand l'activité est en cours, la date de fin est ignorée.
  static Map<String, String> dates({required DateTime? debut, required DateTime? fin, required bool enCours, DateTime? today}) {
    final t = _day(today ?? DateTime.now());
    final errors = <String, String>{};
    if (debut == null) {
      errors['dateDebut'] = 'Indiquez la date de début';
    } else if (_day(debut).isAfter(t)) {
      errors['dateDebut'] = 'La date de début ne peut pas être dans le futur';
    }
    if (!enCours) {
      if (fin == null) {
        errors['dateFin'] = 'Indiquez la date de fin ou activez « Poste actuel »';
      } else if (debut != null && _day(fin).isBefore(_day(debut))) {
        errors['dateFin'] = 'La date de fin doit être après la date de début';
      } else if (_day(fin).isAfter(t)) {
        errors['dateFin'] = 'La date de fin ne peut pas être dans le futur';
      }
    }
    return errors;
  }
}