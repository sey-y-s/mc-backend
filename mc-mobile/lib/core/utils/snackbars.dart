import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';

/// Retour immédiat après une action (état « success » / « error » léger).
void showSuccess(BuildContext context, String message) => _show(context, message, AppColors.green);
void showError(BuildContext context, String message) => _show(context, message, AppColors.error);

void _show(BuildContext context, String message, Color color) {
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(content: Text(message), backgroundColor: color));
}
