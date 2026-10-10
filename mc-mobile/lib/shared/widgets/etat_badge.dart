import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';

/// Couleur d'accent (liseré de carte) d'un état de compétence : seul endroit où elle est décidée.
Color etatAccentColor(EtatCompetence etat) => switch (etat) {
      EtatCompetence.validee => AppColors.green,
      EtatCompetence.attestee => AppColors.gold,
      EtatCompetence.declaree => AppColors.border,
    };

class EtatBadge extends StatelessWidget {
  const EtatBadge(this.etat, {super.key});
  final EtatCompetence etat;

  @override
  Widget build(BuildContext context) => StatusBadge(
        label: etat.label,
        tone: switch (etat) {
          EtatCompetence.validee => BadgeTone.success,
          EtatCompetence.attestee => BadgeTone.accent,
          EtatCompetence.declaree => BadgeTone.neutral,
        },
      );
}
