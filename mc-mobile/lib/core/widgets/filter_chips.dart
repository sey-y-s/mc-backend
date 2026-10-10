import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';

/// Filtre à choix unique avec option « Tous » (selected == null).
/// Défile horizontalement : adapté aux petits écrans.
class FilterChips<T> extends StatelessWidget {
  const FilterChips({
    super.key,
    required this.options,
    required this.labelOf,
    required this.selected,
    required this.onChanged,
    this.allLabel = 'Tous',
  });

  final List<T> options;
  final String Function(T option) labelOf;
  final T? selected;
  final ValueChanged<T?> onChanged;
  final String allLabel;

  @override
  Widget build(BuildContext context) {
    Widget chip(String label, bool isSelected, VoidCallback onTap) => Padding(
          padding: const EdgeInsets.only(right: 8),
          child: ChoiceChip(
            label: Text(label),
            selected: isSelected,
            showCheckmark: false,
            selectedColor: AppColors.greenSoft,
            side: BorderSide(color: isSelected ? AppColors.green : AppColors.border),
            labelStyle: TextStyle(
              fontSize: 13,
              fontWeight: isSelected ? FontWeight.w500 : FontWeight.w400,
              color: isSelected ? AppColors.green : AppColors.anthracite,
            ),
            onSelected: (_) => onTap(),
          ),
        );
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(children: [
        chip(allLabel, selected == null, () => onChanged(null)),
        for (final o in options) chip(labelOf(o), o == selected, () => onChanged(o)),
      ]),
    );
  }
}
