import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';
import 'package:mlc_mobile/core/widgets/status_badge.dart';
import 'package:mlc_mobile/features/competences/domain/citoyen_competence.dart';
import 'package:mlc_mobile/shared/widgets/etat_badge.dart';

/// Carte de compétence réutilisée : liste, accueil, passeport, détail, profil anonymisé.
/// Le liseré de gauche indique l'état (vert validée, or attestée, gris déclarée).
class CompetenceCard extends StatelessWidget {
  const CompetenceCard({super.key, required this.item, this.onTap});
  final CitoyenCompetence item;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return AppCard(
      onTap: onTap,
      accentColor: etatAccentColor(item.etat),
      child: Row(children: [
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(item.competenceNom, style: text.titleMedium),
            if (item.secteurNom != null)
              Text(item.secteurNom!, style: text.bodySmall?.copyWith(color: AppColors.muted)),
            const SizedBox(height: 10),
            Wrap(spacing: 8, runSpacing: 6, children: [StatusBadge(label: item.niveau.label), EtatBadge(item.etat)]),
          ]),
        ),
        if (onTap != null) const Icon(AppIcons.chevron, color: AppColors.muted),
      ]),
    );
  }
}
