import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';

/// Ligne d'expérience (liste, passeport). Liseré vert quand l'activité est en cours.
/// Paramètres simples : indépendante du modèle Experience.
class ExperienceTile extends StatelessWidget {
  const ExperienceTile({
    super.key,
    required this.titre,
    required this.periodLabel,
    this.entreprise,
    this.enCours = false,
    this.reconversion = false,
    this.onTap,
  });
  final String titre;
  final String periodLabel;
  final String? entreprise;
  final bool enCours;
  final bool reconversion;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return AppCard(
      onTap: onTap,
      accentColor: enCours ? AppColors.green : null,
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(titre, style: text.titleSmall),
        if (entreprise != null) Text(entreprise!, style: text.bodyMedium),
        const SizedBox(height: 2),
        Text(periodLabel, style: text.bodySmall?.copyWith(color: AppColors.muted)),
        if (enCours || reconversion) ...[
          const SizedBox(height: 10),
          Wrap(spacing: 8, runSpacing: 6, children: [
            if (enCours) const StatusBadge(label: 'En cours', tone: BadgeTone.success),
            if (reconversion) const StatusBadge(label: 'Reconversion', tone: BadgeTone.accent),
          ]),
        ],
      ]),
    );
  }
}