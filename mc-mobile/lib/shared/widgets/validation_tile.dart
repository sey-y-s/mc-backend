import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';
import 'package:mlc_mobile/shared/widgets/validation_status_badge.dart';

/// Ligne de validation (liste, détail de compétence). Paramètres simples : indépendante du modèle Validation.
/// Avec [competenceNom] : titre = compétence, sous-titre = type. Sans : titre = type.
class ValidationTile extends StatelessWidget {
  const ValidationTile({
    super.key,
    required this.typeLabel,
    required this.statut,
    required this.dateLabel,
    this.competenceNom,
    this.validateurLabel,
    this.onTap,
  });
  final String typeLabel;
  final ValidationStatut statut;
  final String dateLabel;
  final String? competenceNom;
  final String? validateurLabel;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return AppCard(
      onTap: onTap,
      accentColor: validationAccentColor(statut),
      child: Row(children: [
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(competenceNom ?? typeLabel, style: text.titleSmall),
            if (competenceNom != null) Text(typeLabel, style: text.bodyMedium),
            if (validateurLabel != null) Text(validateurLabel!, style: text.bodySmall?.copyWith(color: AppColors.muted)),
            const SizedBox(height: 2),
            Text(dateLabel, style: text.bodySmall?.copyWith(color: AppColors.muted)),
          ]),
        ),
        const SizedBox(width: 8),
        ValidationStatusBadge(statut),
      ]),
    );
  }
}