import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/choice_sheet_field.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/features/auth/domain/auth_repository.dart';
import 'package:mlc_mobile/features/auth/presentation/auth_providers.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/presentation/passport_providers.dart';
import 'package:mlc_mobile/l10n/strings.dart';

class RegisterScreen extends ConsumerStatefulWidget {
  const RegisterScreen({super.key});
  @override
  ConsumerState<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends ConsumerState<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nom = TextEditingController();
  final _prenom = TextEditingController();
  final _tel = TextEditingController();
  final _email = TextEditingController();
  final _pwd = TextEditingController();
  final _pwd2 = TextEditingController();
  String? _regionId;
  String? _communeId;

  @override
  void dispose() {
    for (final c in [_nom, _prenom, _tel, _email, _pwd, _pwd2]) {
      c.dispose();
    }
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final ok =
        await ref.read(authControllerProvider.notifier).register(RegisterData(
              nom: _nom.text.trim(),
              prenom: _prenom.text.trim(),
              telephone: _tel.text.replaceAll(RegExp(r'[\s\-.]'), ''),
              email: _email.text.trim().isEmpty ? null : _email.text.trim(),
              password: _pwd.text,
              communeId: _communeId,
            ));
    // Succès : le routeur redirige vers /home. On ferme l'écran s'il reste dans la pile.
    if (ok && mounted && Navigator.of(context).canPop()) {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final auth = ref.watch(authControllerProvider);
    final busy = auth.isLoading;
    final regions = ref.watch(regionsProvider);
    final communes =
        _regionId == null ? null : ref.watch(communesProvider(_regionId!));
    final region =
        regions.asData?.value.where((r) => r.id == _regionId).firstOrNull;
    final commune =
        communes?.asData?.value.where((c) => c.id == _communeId).firstOrNull;
    return FormScaffold(
      title: S.register,
      formKey: _formKey,
      submitLabel: S.register,
      onSubmit: _submit,
      isLoading: busy,
      error: auth.hasError ? auth.error : null,
      children: [
        AppTextField(
            label: 'Nom',
            controller: _nom,
            enabled: !busy,
            textInputAction: TextInputAction.next,
            validator: (v) => Validators.required(v, field: 'Le nom')),
        AppTextField(
            label: 'Prénom',
            controller: _prenom,
            enabled: !busy,
            textInputAction: TextInputAction.next,
            validator: (v) => Validators.required(v, field: 'Le prénom')),
        AppTextField(
            label: 'Téléphone',
            controller: _tel,
            enabled: !busy,
            keyboardType: TextInputType.phone,
            textInputAction: TextInputAction.next,
            validator: Validators.phone),
        AppTextField(
            label: 'Email (facultatif)',
            controller: _email,
            enabled: !busy,
            keyboardType: TextInputType.emailAddress,
            textInputAction: TextInputAction.next,
            validator: Validators.emailOptional),
        ChoiceSheetField<Region>(
          label: 'Région (facultatif)',
          options: regions.asData?.value ?? const [],
          labelOf: (r) => r.nom,
          selected: region,
          enabled: !busy,
          isLoading: regions.isLoading,
          errorText: regions.hasError ? failureMessage(regions.error!) : null,
          onChanged: (r) => setState(() {
            _regionId = r.id;
            _communeId = null;
          }),
        ),
        ChoiceSheetField<Commune>(
          label: 'Commune (facultatif)',
          placeholder:
              _regionId == null ? "Choisissez d'abord une région" : 'Choisir',
          enabled: !busy && _regionId != null,
          options: communes?.asData?.value ?? const [],
          labelOf: (c) => c.nom,
          selected: commune,
          isLoading: communes?.isLoading ?? false,
          errorText: communes != null && communes.hasError
              ? failureMessage(communes.error!)
              : null,
          onChanged: (c) => setState(() => _communeId = c.id),
        ),
        AppTextField(
            label: 'Mot de passe',
            controller: _pwd,
            obscure: true,
            enabled: !busy,
            textInputAction: TextInputAction.next,
            validator: Validators.password),
        AppTextField(
            label: 'Confirmer le mot de passe',
            controller: _pwd2,
            obscure: true,
            enabled: !busy,
            textInputAction: TextInputAction.done,
            validator: (v) => Validators.confirmPassword(v, _pwd.text)),
      ],
    );
  }
}
