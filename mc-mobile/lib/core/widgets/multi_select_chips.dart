import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';

/// Choix multiple par pastilles (compétences utilisées, secteurs...). La sélection est suivie par identifiant.
class MultiSelectChips<T> extends StatelessWidget {
  const MultiSelectChips({
    super.key,
    required this.options,
    required this.idOf,
    required this.labelOf,
    required this.selectedIds,
    required this.onChanged,
    this.enabled = true,
  });

  final List<T> options;
  final String Function(T option) idOf;
  final String Function(T option) labelOf;
  final Set<String> selectedIds;
  final ValueChanged<Set<String>> onChanged;
  final bool enabled;

  @override
  Widget build(BuildContext context) => Wrap(
        spacing: 8,
        runSpacing: 8,
        children: [
          for (final o in options)
            Builder(builder: (_) {
              final id = idOf(o);
              final selected = selectedIds.contains(id);
              return FilterChip(
                label: Text(labelOf(o)),
                selected: selected,
                showCheckmark: false,
                selectedColor: AppColors.greenSoft,
                side: BorderSide(color: selected ? AppColors.green : AppColors.border),
                labelStyle: TextStyle(
                  fontSize: 13,
                  fontWeight: selected ? FontWeight.w500 : FontWeight.w400,
                  color: selected ? AppColors.green : AppColors.anthracite,
                ),
                onSelected: enabled
                    ? (on) => onChanged(on ? {...selectedIds, id} : ({...selectedIds}..remove(id)))
                    : null,
              );
            }),
        ],
      );
}