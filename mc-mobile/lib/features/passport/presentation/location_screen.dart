import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/widgets/choice_sheet_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/presentation/passport_providers.dart';

/// Route : /passeport/localisation (région puis commune ; règle : un citoyen a une commune).
class LocationScreen extends ConsumerWidget {
  const LocationScreen({super.key});

  Widget _shell(Widget body) =>
      Scaffold(appBar: AppBar(title: const Text('Localisation')), body: body);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ref.watch(passportProvider).when(
          skipLoadingOnReload: true,
          loading: () => _shell(const LoadingView()),
          error: (e, _) => _shell(ErrorView(
              error: e, onRetry: () => ref.invalidate(passportProvider))),
          data: (c) => _LocationForm(initial: c.commune),
        );
  }
}

class _LocationForm extends ConsumerStatefulWidget {
  const _LocationForm({required this.initial});
  final Commune? initial;

  @override
  ConsumerState<_LocationForm> createState() => _LocationFormState();
}

class _LocationFormState extends ConsumerState<_LocationForm> {
  final _formKey = GlobalKey<FormState>();
  late String? _regionId = widget.initial?.regionId;
  late String? _communeId = widget.initial?.id;

  Future<void> _submit() async {
    if (_communeId == null) {
      showError(context, 'Choisissez votre commune.');
      return;
    }
    final ok = await ref
        .read(passportActionsProvider.notifier)
        .updateCommune(_communeId!);
    if (!ok || !mounted) return;
    showSuccess(context, 'Localisation enregistrée');
    Navigator.of(context).maybePop();
  }

  @override
  Widget build(BuildContext context) {
    final regions = ref.watch(regionsProvider);
    final communes =
        _regionId == null ? null : ref.watch(communesProvider(_regionId!));
    final actions = ref.watch(passportActionsProvider);
    final region =
        regions.asData?.value.where((r) => r.id == _regionId).firstOrNull;
    final commune =
        communes?.asData?.value.where((c) => c.id == _communeId).firstOrNull;
    return FormScaffold(
      title: 'Localisation',
      formKey: _formKey,
      submitLabel: 'Enregistrer',
      onSubmit: _submit,
      isLoading: actions.isLoading,
      error: actions.hasError ? actions.error : null,
      children: [
        Text(
          'Votre commune aide les organisations proches de vous à vous trouver.',
          style: Theme.of(context)
              .textTheme
              .bodyMedium
              ?.copyWith(color: AppColors.muted),
        ),
        ChoiceSheetField<Region>(
          label: 'Région',
          options: regions.asData?.value ?? const [],
          labelOf: (r) => r.nom,
          selected: region,
          isLoading: regions.isLoading,
          errorText: regions.hasError ? failureMessage(regions.error!) : null,
          onChanged: (r) => setState(() {
            _regionId = r.id;
            _communeId = null;
          }),
        ),
        ChoiceSheetField<Commune>(
          label: 'Commune',
          placeholder:
              _regionId == null ? 'Choisissez d\'abord une région' : 'Choisir',
          enabled: _regionId != null,
          options: communes?.asData?.value ?? const [],
          labelOf: (c) => c.nom,
          selected: commune,
          isLoading: communes?.isLoading ?? false,
          errorText: communes != null && communes.hasError
              ? failureMessage(communes.error!)
              : null,
          onChanged: (c) => setState(() => _communeId = c.id),
        ),
      ],
    );
  }
}
