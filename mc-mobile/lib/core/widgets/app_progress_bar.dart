import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';

/// Barre de progression (or = progression, selon la charte). [value] entre 0 et 1.
/// [onDark] : version pour fond vert (libellé blanc, piste translucide).
class AppProgressBar extends StatelessWidget {
  const AppProgressBar({super.key, required this.value, this.label, this.onDark = false});
  final double value;
  final String? label;
  final bool onDark;

  @override
  Widget build(BuildContext context) {
    final v = value.clamp(0.0, 1.0);
    final textColor = onDark ? Colors.white : AppColors.anthracite;
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      if (label != null)
        Padding(
          padding: const EdgeInsets.only(bottom: 6),
          child: Row(children: [
            Expanded(child: Text(label!, style: Theme.of(context).textTheme.bodySmall?.copyWith(color: textColor))),
            Text('${(v * 100).round()} %', style: Theme.of(context).textTheme.labelMedium?.copyWith(color: textColor)),
          ]),
        ),
      ClipRRect(
        borderRadius: BorderRadius.circular(8),
        child: LinearProgressIndicator(
          value: v,
          minHeight: 8,
          color: AppColors.gold,
          backgroundColor: onDark ? Colors.white24 : AppColors.border,
        ),
      ),
    ]);
  }
}
