import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/features/validations/domain/validation_statut.dart';
import 'package:mlc_mobile/shared/widgets/validation_status_badge.dart';

/// Ligne de preuve. Paramètres simples (pas de dépendance au futur modèle Preuve) :
/// l'écran fournit le libellé du type (ex. « Certificat »), la compétence liée et la date formatée.
class PreuveTile extends StatelessWidget {
  const PreuveTile({super.key, required this.typeLabel, required this.competenceNom, required this.dateLabel, this.statut, this.onTap});
  final String typeLabel;
  final String competenceNom;
  final String dateLabel;
  final ValidationStatut? statut;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) => AppCard(
        onTap: onTap,
        child: Row(children: [
          const Icon(AppIcons.preuves, color: AppColors.green),
          const SizedBox(width: 12),
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(typeLabel, style: Theme.of(context).textTheme.titleSmall),
              Text(competenceNom),
              Text(dateLabel, style: const TextStyle(color: AppColors.muted, fontSize: 12)),
            ]),
          ),
          if (statut != null) ValidationStatusBadge(statut!),
        ]),
      );
}
