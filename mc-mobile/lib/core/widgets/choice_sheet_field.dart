import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/app/theme/app_theme.dart';
import 'package:mlc_mobile/core/widgets/app_dialogs.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

/// Champ « choisir dans une liste » : ouvre une feuille modale (région, commune, type...).
/// Gère : désactivé (liste dépendante), chargement, erreur. Plus agréable et plus léger qu'un menu déroulant.
class ChoiceSheetField<T> extends StatelessWidget {
  const ChoiceSheetField({
    super.key,
    required this.label,
    required this.options,
    required this.labelOf,
    required this.selected,
    required this.onChanged,
    this.sheetTitle,
    this.placeholder = 'Choisir',
    this.enabled = true,
    this.isLoading = false,
    this.errorText,
  });

  final String label;
  final List<T> options;
  final String Function(T option) labelOf;
  final T? selected;
  final ValueChanged<T> onChanged;
  final String? sheetTitle;
  final String placeholder;
  final bool enabled;
  final bool isLoading;
  final String? errorText;

  Future<void> _open(BuildContext context) async {
    final picked = await showAppBottomSheet<T>(
      context,
      builder: (ctx) => _OptionsSheet<T>(
          title: sheetTitle ?? label,
          options: options,
          labelOf: labelOf,
          selected: selected),
    );
    if (picked != null) onChanged(picked);
  }

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final active = enabled && !isLoading;
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Opacity(
        opacity: enabled ? 1 : 0.5,
        child: Semantics(
          button: true,
          label:
              '$label : ${selected == null ? placeholder : labelOf(selected as T)}',
          excludeSemantics: true,
          child: InkWell(
            onTap: active ? () => _open(context) : null,
            borderRadius: BorderRadius.circular(AppRadius.field),
            child: Container(
              constraints: const BoxConstraints(minHeight: 60),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: AppColors.card,
                borderRadius: BorderRadius.circular(AppRadius.field),
                border: Border.all(
                    color:
                        errorText != null ? AppColors.error : AppColors.border,
                    width: errorText != null ? 1.6 : 1),
              ),
              child: Row(children: [
                Expanded(
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(label,
                            style: text.bodySmall
                                ?.copyWith(color: AppColors.muted)),
                        Text(
                          selected == null
                              ? placeholder
                              : labelOf(selected as T),
                          style: text.titleSmall?.copyWith(
                              color: selected == null
                                  ? AppColors.muted
                                  : AppColors.anthracite),
                        ),
                      ]),
                ),
                if (isLoading)
                  const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2))
                else
                  const Icon(AppIcons.expand, color: AppColors.muted),
              ]),
            ),
          ),
        ),
      ),
      if (errorText != null) ...[
        const SizedBox(height: 6),
        Text(errorText!,
            style: text.bodySmall?.copyWith(color: AppColors.error)),
      ],
    ]);
  }
}

class _OptionsSheet<T> extends StatelessWidget {
  const _OptionsSheet(
      {required this.title,
      required this.options,
      required this.labelOf,
      required this.selected});
  final String title;
  final List<T> options;
  final String Function(T) labelOf;
  final T? selected;

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Text(title, style: text.titleLarge)),
          if (options.isEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 24),
              child: Text('Aucun choix disponible.',
                  style: text.bodyMedium?.copyWith(color: AppColors.muted)),
            )
          else
            ConstrainedBox(
              constraints: BoxConstraints(
                  maxHeight: MediaQuery.of(context).size.height * 0.55),
              child: ListView(shrinkWrap: true, children: [
                for (final o in options)
                  ListTile(
                    contentPadding: EdgeInsets.zero,
                    title: Text(labelOf(o)),
                    trailing: o == selected
                        ? const Icon(AppIcons.selected, color: AppColors.green)
                        : null,
                    onTap: () => Navigator.of(context).pop(o),
                  ),
              ]),
            ),
        ]);
  }
}
