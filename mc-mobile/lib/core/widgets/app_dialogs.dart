import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/app/theme/app_theme.dart';

/// Dialogue de confirmation. Retourne true uniquement si l'utilisateur confirme.
/// [destructive] : bouton de confirmation rouge (suppression, refus, déconnexion).
Future<bool> confirmDialog(
  BuildContext context, {
  required String title,
  required String message,
  String confirmLabel = 'Confirmer',
  String cancelLabel = 'Annuler',
  bool destructive = false,
}) async {
  final result = await showDialog<bool>(
    context: context,
    builder: (ctx) => AlertDialog(
      backgroundColor: AppColors.card,
      surfaceTintColor: Colors.transparent,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.card + 4)),
      title: Text(title, style: Theme.of(ctx).textTheme.titleLarge),
      content: Text(message),
      actions: [
        TextButton(onPressed: () => Navigator.of(ctx).pop(false), child: Text(cancelLabel)),
        FilledButton(
          style: FilledButton.styleFrom(
            minimumSize: const Size(0, 44), // le thème impose une largeur infinie : inadapté aux actions
            backgroundColor: destructive ? AppColors.error : null,
          ),
          onPressed: () => Navigator.of(ctx).pop(true),
          child: Text(confirmLabel),
        ),
      ],
    ),
  );
  return result ?? false;
}

/// Bottom sheet standard (filtres, envoi d'une demande...). Gère clavier et zones sûres.
Future<T?> showAppBottomSheet<T>(BuildContext context, {required WidgetBuilder builder}) {
  return showModalBottomSheet<T>(
    context: context,
    isScrollControlled: true,
    showDragHandle: true,
    builder: (ctx) => SafeArea(
      child: Padding(
        padding: EdgeInsets.fromLTRB(16, 0, 16, MediaQuery.of(ctx).viewInsets.bottom + 16),
        child: builder(ctx),
      ),
    ),
  );
}
