import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/features/validations/domain/validation_models.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

/// « Demandée le 2 avril 2026 » tant qu'il n'y a pas de décision, sinon « Décision le 5 février 2026 ».
String validationDateLabel(Validation v) =>
    v.dateDecision == null ? 'Demandée le ${formatDateFr(v.dateDemande)}' : 'Décision le ${formatDateFr(v.dateDecision!)}';

/// Phrase d'explication d'un statut, en langage simple.
String validationStatutHelp(ValidationStatut s) => switch (s) {
      ValidationStatut.enAttente => 'Un évaluateur ou un centre examine votre demande.',
      ValidationStatut.approuvee => 'Votre compétence a été confirmée.',
      ValidationStatut.rejetee => 'La demande a été refusée. Lisez le commentaire, puis ajoutez une meilleure preuve.',
    };