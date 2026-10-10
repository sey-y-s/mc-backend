import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/widgets/app_card.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

/// Option à choix unique (niveau, disponibilité...).
class SelectableOptionCard extends StatelessWidget {
  const SelectableOptionCard(
      {super.key,
      required this.title,
      this.description,
      required this.selected,
      required this.onTap});
  final String title;
  final String? description;
  final bool selected;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return AppCard(
      onTap: onTap,
      accentColor: selected ? AppColors.green : null,
      child: Row(children: [
        Icon(selected ? AppIcons.selected : AppIcons.unselected,
            color: selected ? AppColors.green : AppColors.muted),
        const SizedBox(width: 14),
        Expanded(
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(title, style: text.titleSmall),
            if (description != null)
              Text(description!,
                  style: text.bodySmall?.copyWith(color: AppColors.muted)),
          ]),
        ),
      ]),
    );
  }
}
