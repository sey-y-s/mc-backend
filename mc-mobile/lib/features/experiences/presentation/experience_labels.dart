import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/features/experiences/domain/experience_models.dart';

/// « mars 2019 à juin 2023 », « septembre 2023 à aujourd'hui ».
String experiencePeriodLabel(Experience e) {
  final start = formatMonthYearFr(e.dateDebut);
  if (e.enCours) return "$start à aujourd'hui";
  if (e.dateFin == null) return 'Depuis $start';
  return '$start à ${formatMonthYearFr(e.dateFin!)}';
}