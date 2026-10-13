import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';

/// Interrupteur dans une carte : toute la carte est cliquable, avec une phrase d'explication.
class AppSwitchTile extends StatelessWidget {
  const AppSwitchTile({super.key, required this.title, this.subtitle, required this.value, required this.onChanged});
  final String title;
  final String? subtitle;
  final bool value;
  final ValueChanged<bool>? onChanged;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return AppCard(
      onTap: onChanged == null ? null : () => onChanged!(!value),
      child: Row(children: [
        Expanded(
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(title, style: text.titleSmall),
            if (subtitle != null) Text(subtitle!, style: text.bodySmall?.copyWith(color: AppColors.muted)),
          ]),
        ),
        const SizedBox(width: 12),
        Switch(value: value, onChanged: onChanged),
      ]),
    );
  }
}