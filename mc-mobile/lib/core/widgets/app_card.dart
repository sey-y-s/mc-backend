import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/app/theme/app_theme.dart';

/// Carte sans ombre : fond blanc + trait fin.
/// [accentColor] ajoute un liseré à gauche qui porte une information (ex. état d'une compétence).
class AppCard extends StatelessWidget {
  const AppCard({
    super.key,
    required this.child,
    this.onTap,
    this.padding = const EdgeInsets.all(16),
    this.accentColor,
  });
  final Widget child;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry padding;
  final Color? accentColor;

  @override
  Widget build(BuildContext context) {
    final radius = BorderRadius.circular(AppRadius.card);
    final content = Padding(padding: padding, child: child);
    return Material(
      color: AppColors.card,
      borderRadius: radius,
      clipBehavior: Clip.antiAlias,
      child: Ink(
        decoration: BoxDecoration(
          borderRadius: radius,
          border: Border.all(color: AppColors.border),
        ),
        child: InkWell(
          onTap: onTap,
          child: accentColor == null
              ? content
              : IntrinsicHeight(
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Container(width: 4, color: accentColor),
                      Expanded(child: content),
                    ],
                  ),
                ),
        ),
      ),
    );
  }
}
