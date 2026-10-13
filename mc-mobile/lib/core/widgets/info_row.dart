import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';

/// Ligne « libellé : valeur » pour les écrans de détail.
class InfoRow extends StatelessWidget {
  const InfoRow({super.key, required this.label, required this.value, this.icon});
  final String label;
  final String value;
  final IconData? icon;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          if (icon != null) ...[Icon(icon, size: 18, color: AppColors.muted), const SizedBox(width: 8)],
          Expanded(flex: 2, child: Text(label, style: const TextStyle(color: AppColors.muted))),
          Expanded(flex: 3, child: Text(value, textAlign: TextAlign.end, style: const TextStyle(fontWeight: FontWeight.w600))),
        ]),
      );
}
