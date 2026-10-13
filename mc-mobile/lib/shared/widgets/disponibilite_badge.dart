import 'package:flutter/material.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';

/// Seul endroit qui décide de la couleur d'une disponibilité (passeport, résultats de recherche).
class DisponibiliteBadge extends StatelessWidget {
  const DisponibiliteBadge(this.value, {super.key});
  final Disponibilite value;

  @override
  Widget build(BuildContext context) => StatusBadge(
        label: value.label,
        tone: switch (value) {
          Disponibilite.disponible => BadgeTone.success,
          Disponibilite.bientot => BadgeTone.accent,
          Disponibilite.indisponible => BadgeTone.neutral,
        },
      );
}
