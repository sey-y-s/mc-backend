import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/app/theme/app_theme.dart';

/// Or = confiance/progression ; vert = acquis ; rouge = erreur/rejet uniquement.
enum BadgeTone { neutral, accent, success, error }

class StatusBadge extends StatelessWidget {
  const StatusBadge({super.key, required this.label, this.tone = BadgeTone.neutral});
  final String label;
  final BadgeTone tone;

  @override
  Widget build(BuildContext context) {
    final (bg, fg) = switch (tone) {
      BadgeTone.neutral => (AppColors.border, AppColors.anthracite),
      BadgeTone.accent => (AppColors.goldSoft, AppColors.goldDeep),
      BadgeTone.success => (AppColors.greenSoft, AppColors.green),
      BadgeTone.error => (AppColors.errorSoft, AppColors.error),
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(AppRadius.badge)),
      child: Text(label, style: Theme.of(context).textTheme.labelMedium?.copyWith(color: fg)),
    );
  }
}
