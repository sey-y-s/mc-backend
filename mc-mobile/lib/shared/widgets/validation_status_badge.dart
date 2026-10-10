import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';

/// Couleur d'accent (liseré de carte) d'un statut de validation : seul endroit où elle est décidée.
Color validationAccentColor(ValidationStatut statut) => switch (statut) {
      ValidationStatut.approuvee => AppColors.green,
      ValidationStatut.enAttente => AppColors.gold,
      ValidationStatut.rejetee => AppColors.error,
    };

/// Seul endroit qui décide de la couleur d'un statut de validation (rouge = rejet uniquement).
class ValidationStatusBadge extends StatelessWidget {
  const ValidationStatusBadge(this.statut, {super.key});
  final ValidationStatut statut;

  @override
  Widget build(BuildContext context) => StatusBadge(
        label: statut.label,
        tone: switch (statut) {
          ValidationStatut.approuvee => BadgeTone.success,
          ValidationStatut.enAttente => BadgeTone.accent,
          ValidationStatut.rejetee => BadgeTone.error,
        },
      );
}