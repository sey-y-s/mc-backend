import 'package:flutter/material.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/app/theme/app_theme.dart';
import 'package:mlc_mobile/core/utils/date_format.dart';
import 'package:mlc_mobile/core/widgets/app_icons.dart';

/// Champ « choisir une date » (sélecteur système, en français dans l'app). Même aspect que ChoiceSheetField.
/// Par défaut : de 1960 à aujourd'hui (dates passées ; passer [lastDate] pour accepter le futur).
class DateField extends StatelessWidget {
  const DateField({
    super.key,
    required this.label,
    required this.value,
    required this.onChanged,
    this.firstDate,
    this.lastDate,
    this.errorText,
    this.enabled = true,
    this.placeholder = 'Choisir la date',
  });

  final String label;
  final DateTime? value;
  final ValueChanged<DateTime> onChanged;
  final DateTime? firstDate;
  final DateTime? lastDate;
  final String? errorText;
  final bool enabled;
  final String placeholder;

  Future<void> _pick(BuildContext context) async {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final first = firstDate ?? DateTime(1960);
    final last = lastDate ?? today;
    var initial = value ?? last;
    if (initial.isBefore(first)) initial = first;
    if (initial.isAfter(last)) initial = last;
    final picked = await showDatePicker(context: context, initialDate: initial, firstDate: first, lastDate: last);
    if (picked != null) onChanged(picked);
  }

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Opacity(
        opacity: enabled ? 1 : 0.5,
        child: Semantics(
          button: true,
          label: '$label : ${value == null ? placeholder : formatDateFr(value!)}',
          excludeSemantics: true,
          child: InkWell(
            onTap: enabled ? () => _pick(context) : null,
            borderRadius: BorderRadius.circular(AppRadius.field),
            child: Container(
              constraints: const BoxConstraints(minHeight: 60),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: AppColors.card,
                borderRadius: BorderRadius.circular(AppRadius.field),
                border: Border.all(color: errorText != null ? AppColors.error : AppColors.border, width: errorText != null ? 1.6 : 1),
              ),
              child: Row(children: [
                Expanded(
                  child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisSize: MainAxisSize.min, children: [
                    Text(label, style: text.bodySmall?.copyWith(color: AppColors.muted)),
                    Text(
                      value == null ? placeholder : formatDateFr(value!),
                      style: text.titleSmall?.copyWith(color: value == null ? AppColors.muted : AppColors.anthracite),
                    ),
                  ]),
                ),
                const Icon(AppIcons.calendar, color: AppColors.muted),
              ]),
            ),
          ),
        ),
      ),
      if (errorText != null) ...[
        const SizedBox(height: 6),
        Text(errorText!, style: text.bodySmall?.copyWith(color: AppColors.error)),
      ],
    ]);
  }
}