import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/app_button.dart';
import 'package:mlc_mobile/core/widgets/selectable_option_card.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/presentation/passport_providers.dart';

/// Route : /passeport/disponibilite
class AvailabilityScreen extends ConsumerWidget {
  const AvailabilityScreen({super.key});

  Widget _shell(Widget body) =>
      Scaffold(appBar: AppBar(title: const Text('Disponibilité')), body: body);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ref.watch(passportProvider).when(
          skipLoadingOnReload: true,
          loading: () => _shell(const LoadingView()),
          error: (e, _) => _shell(ErrorView(
              error: e, onRetry: () => ref.invalidate(passportProvider))),
          data: (c) => _AvailabilityForm(initial: c.disponibilite),
        );
  }
}

class _AvailabilityForm extends ConsumerStatefulWidget {
  const _AvailabilityForm({required this.initial});
  final Disponibilite initial;

  @override
  ConsumerState<_AvailabilityForm> createState() => _AvailabilityFormState();
}

class _AvailabilityFormState extends ConsumerState<_AvailabilityForm> {
  late Disponibilite _value = widget.initial;

  static String _description(Disponibilite d) => switch (d) {
        Disponibilite.disponible =>
          'Les organisations peuvent vous contacter dès maintenant.',
        Disponibilite.bientot => 'Vous serez disponible prochainement.',
        Disponibilite.indisponible =>
          'Vous ne souhaitez pas être contacté pour le moment.',
      };

  Future<void> _save() async {
    final ok = await ref
        .read(passportActionsProvider.notifier)
        .updateAvailability(_value);
    if (!ok || !mounted) return;
    showSuccess(context, 'Disponibilité enregistrée');
    Navigator.of(context).maybePop();
  }

  @override
  Widget build(BuildContext context) {
    final actions = ref.watch(passportActionsProvider);
    final busy = actions.isLoading;
    return Scaffold(
      appBar: AppBar(title: const Text('Disponibilité')),
      body: SafeArea(
        child: ListView(padding: const EdgeInsets.all(20), children: [
          Text('Êtes-vous disponible pour de nouvelles opportunités ?',
              style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 16),
          for (final d in Disponibilite.values) ...[
            SelectableOptionCard(
              title: d.label,
              description: _description(d),
              selected: _value == d,
              onTap: busy ? null : () => setState(() => _value = d),
            ),
            const SizedBox(height: 12),
          ],
          if (actions.hasError) ...[
            Text(failureMessage(actions.error!),
                style: const TextStyle(color: AppColors.error)),
            const SizedBox(height: 12),
          ],
          const SizedBox(height: 8),
          AppButton(
              label: 'Enregistrer',
              onPressed: _value == widget.initial ? null : _save,
              isLoading: busy),
        ]),
      ),
    );
  }
}
